package com.example.dotapro.ui.seller

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dotapro.R
import com.example.dotapro.data.model.Product
import com.example.dotapro.data.model.ProductStatus
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityNewProductBinding
import com.example.dotapro.util.LocationHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toast
import com.google.android.material.chip.Chip

/** Crea un producto nuevo o edita uno existente (si llega EXTRA_PRODUCT_ID). */
class NewProductActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }

    private lateinit var b: ActivityNewProductBinding
    private var editing: Product? = null

    private val categories = listOf("Uniformes", "Calzado", "EPP")
    private val clothingSizes = listOf("S", "M", "L", "XL", "XXL")
    private val shoeSizes = listOf("38", "39", "40", "41", "42", "43")
    private val selectedSizes = mutableSetOf<String>()

    // Cámara y galería (no requieren permisos con estos contratos)
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            b.ivPhoto.setImageBitmap(bitmap)
            showPhoto()
        }
    }
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            b.ivPhoto.setImageURI(uri)
            showPhoto()
        }
    }
    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) useGps() else toast("Permiso de ubicación denegado")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = requireUser() ?: return
        b = ActivityNewProductBinding.inflate(layoutInflater)
        setContentView(b.root)

        editing = AppRepository.productById(intent.getIntExtra(EXTRA_PRODUCT_ID, -1))
        b.toolbar.setup(this, if (editing == null) "Nuevo producto" else "Editar producto", showBack = true)

        b.spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        b.tvWarehouse.text = "Bodega: ${user.address.ifBlank { "toca \"Usar GPS\"" }}"

        editing?.let { product ->
            b.etName.setText(product.name)
            b.etPrice.setText(product.price.toString())
            b.etStock.setText(product.stock.toString())
            b.etDescription.setText(product.description)
            b.tvWarehouse.text = product.warehouse
            b.spCategory.setSelection(categories.indexOf(product.category).coerceAtLeast(0))
            selectedSizes.addAll(product.sizes)
        }

        b.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                renderSizes(categories[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        b.tvTakePhoto.setOnClickListener { takePhoto.launch(null) }
        b.tvGallery.setOnClickListener { pickImage.launch("image/*") }
        b.tvUseGps.setOnClickListener {
            if (LocationHelper.hasPermission(this)) useGps()
            else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        b.btnSave.setOnClickListener { save() }
        b.tvCancel.setOnClickListener { finish() }
    }

    private fun showPhoto() {
        b.ivPhoto.visibility = View.VISIBLE
        b.ivPhotoHint.visibility = View.GONE
        // TODO: subir la imagen al almacenamiento (Firebase Storage / API) y guardar su URL en el producto
    }

    private fun useGps() {
        LocationHelper.fetchAddress(this) { address ->
            if (address == null) toast("No se pudo obtener la ubicación. Activa el GPS")
            else b.tvWarehouse.text = "Bodega: $address"
        }
    }

    /** Las tallas cambian según la categoría (calzado usa números). */
    private fun renderSizes(category: String) {
        val options = when (category) {
            "Calzado" -> shoeSizes
            "EPP" -> listOf("Única") + clothingSizes
            else -> clothingSizes
        }
        selectedSizes.retainAll(options.toSet())
        b.chipSizes.removeAllViews()
        options.forEach { size ->
            val chip = layoutInflater.inflate(R.layout.item_size_chip, b.chipSizes, false) as Chip
            chip.text = size
            chip.isChecked = size in selectedSizes
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) selectedSizes.add(size) else selectedSizes.remove(size)
            }
            b.chipSizes.addView(chip)
        }
    }

    private fun save() {
        val user = requireUser() ?: return
        val name = b.etName.text.toString().trim()
        val price = b.etPrice.text.toString().toIntOrNull()
        val stock = b.etStock.text.toString().toIntOrNull()
        val description = b.etDescription.text.toString().trim()
        val category = categories[b.spCategory.selectedItemPosition]

        when {
            name.isEmpty() -> { b.etName.error = "Ingresa el nombre"; return }
            price == null || price <= 0 -> { b.etPrice.error = "Precio no válido"; return }
            stock == null || stock < 0 -> { b.etStock.error = "Stock no válido"; return }
            selectedSizes.isEmpty() -> { toast("Selecciona al menos una talla"); return }
        }

        val (emoji, color) = when (category) {
            "Calzado" -> "🥾" to "#FDE6CF"
            "EPP" -> "⛑️" to "#FDE6CF"
            else -> "👕" to "#DCE6F5"
        }
        val sizes = selectedSizes.toList()
        val warehouse = b.tvWarehouse.text.toString()

        val current = editing
        if (current != null) {
            current.name = name
            current.price = price!!
            current.stock = stock!!
            current.category = category
            current.description = description
            current.sizes = sizes
            current.warehouse = warehouse
            current.emoji = emoji
            current.bgColor = color
            AppRepository.saveProduct(current)
            toast("Producto actualizado")
        } else {
            AppRepository.saveProduct(
                Product(
                    id = AppRepository.newProductId(), name = name, category = category, price = price!!,
                    stock = stock!!, emoji = emoji, bgColor = color, sellerId = user.id, sellerName = user.name,
                    description = description, sizes = sizes, status = ProductStatus.PENDING, warehouse = warehouse
                )
            )
            toast("Producto enviado a moderación")
        }
        finish()
    }
}
