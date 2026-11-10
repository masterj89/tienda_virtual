package com.example.dotapro.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dotapro.R
import com.example.dotapro.data.local.SessionManager
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityMyProductsBinding
import com.example.dotapro.util.NavHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MyProductsActivity : AppCompatActivity() {

    private lateinit var b: ActivityMyProductsBinding
    private lateinit var adapter: MyProductAdapter
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = requireUser() ?: return
        b = ActivityMyProductsBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setup(this, "Mis productos", showBack = false, badge = "VENDEDOR")
        NavHelper.setup(this, b.bottomNav, user.role, R.id.nav_mis_productos)

        adapter = MyProductAdapter(
            emptyList(),
            onEdit = { product ->
                startActivity(
                    Intent(this, NewProductActivity::class.java)
                        .putExtra(NewProductActivity.EXTRA_PRODUCT_ID, product.id)
                )
            },
            onDelete = { product ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Borrar producto")
                    .setMessage("¿Seguro que deseas borrar \"${product.name}\"?")
                    .setPositiveButton("Borrar") { _, _ ->
                        AppRepository.deleteProduct(product.id)
                        toast("Producto eliminado")
                        refresh()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        b.rvProducts.layoutManager = LinearLayoutManager(this)
        b.rvProducts.adapter = adapter

        b.search.etSearch.hint = "Buscar en mi catálogo"
        b.search.etSearch.doAfterTextChanged {
            query = it?.toString().orEmpty().trim()
            refresh()
        }
        b.fabAdd.setOnClickListener { startActivity(Intent(this, NewProductActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        if (::b.isInitialized) refresh()
    }

    private fun refresh() {
        val user = SessionManager.currentUser ?: return
        val products = AppRepository.productsBySeller(user.id, query)
        adapter.submit(products)
        b.tvEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
    }
}
