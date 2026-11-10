package com.example.dotapro.ui.buyer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.dotapro.R
import com.example.dotapro.data.model.Product
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.util.bindImage
import com.example.dotapro.databinding.ActivityProductDetailBinding
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toCop
import com.example.dotapro.util.toast
import com.google.android.material.chip.Chip

class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }

    private lateinit var b: ActivityProductDetailBinding
    private lateinit var product: Product
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireUser() ?: return
        val found = AppRepository.productById(intent.getIntExtra(EXTRA_PRODUCT_ID, -1))
        if (found == null) {
            finish()
            return
        }
        product = found

        b = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setup(this, "Detalle", showBack = true)

        b.flImage.setBackgroundColor(Color.parseColor(product.bgColor))
        product.bindImage(b.tvEmoji, b.ivImage)
        b.tvName.text = product.name
        b.tvPrice.text = product.price.toCop()
        b.tvSeller.text = "Vendido por: ${product.sellerName}"
        b.tvDescription.text = product.description
        b.tvStock.text = if (product.stock > 0) "Disponibles: ${product.stock} unidades" else "Sin existencias"

        val sizes = product.sizes.ifEmpty { listOf("Única") }
        sizes.forEachIndexed { index, size ->
            val chip = layoutInflater.inflate(R.layout.item_size_chip, b.chipSizes, false) as Chip
            chip.id = View.generateViewId()
            chip.text = size
            chip.isChecked = index == 0
            b.chipSizes.addView(chip)
        }

        b.btnMinus.setOnClickListener {
            if (quantity > 1) quantity--
            b.tvQty.text = quantity.toString()
        }
        b.btnPlus.setOnClickListener {
            if (quantity < product.stock) quantity++ else toast("No hay más unidades disponibles")
            b.tvQty.text = quantity.toString()
        }
        b.btnAdd.setOnClickListener {
            if (addToCart()) toast("Agregado al carrito")
        }
        b.btnBuyNow.setOnClickListener {
            if (addToCart()) startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    private fun addToCart(): Boolean {
        if (product.stock <= 0) {
            toast("Producto sin existencias")
            return false
        }
        val checkedId = b.chipSizes.checkedChipId
        val size = b.chipSizes.findViewById<Chip>(checkedId)?.text?.toString() ?: "Única"
        AppRepository.addToCart(product, size, quantity)
        return true
    }
}
