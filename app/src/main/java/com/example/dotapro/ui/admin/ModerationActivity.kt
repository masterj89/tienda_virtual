package com.example.dotapro.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dotapro.R
import com.example.dotapro.data.model.Product
import com.example.dotapro.data.model.ProductStatus
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityModerationBinding
import com.example.dotapro.util.NavHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toCop
import com.example.dotapro.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class ModerationActivity : AppCompatActivity() {

    private lateinit var b: ActivityModerationBinding
    private lateinit var adapter: ModerationAdapter
    private var status = ProductStatus.PENDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val admin = requireUser() ?: return
        b = ActivityModerationBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setup(this, "Moderación", showBack = false, badge = "ADMIN")
        NavHelper.setup(this, b.bottomNav, admin.role, R.id.nav_admin_productos)

        listOf("Pendientes", "Activos", "Inactivos").forEach { title ->
            b.tabs.addTab(b.tabs.newTab().setText(title))
        }
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                status = when (tab?.position) {
                    1 -> ProductStatus.ACTIVE
                    2 -> ProductStatus.INACTIVE
                    else -> ProductStatus.PENDING
                }
                refresh()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        adapter = ModerationAdapter(
            emptyList(), status,
            onPositive = { product ->
                AppRepository.setProductStatus(product.id, ProductStatus.ACTIVE)
                toast("Producto activo")
                refresh()
            },
            onNegative = { product ->
                AppRepository.setProductStatus(product.id, ProductStatus.INACTIVE)
                toast("Producto inactivo")
                refresh()
            },
            onSheet = { showSheet(it) }
        )
        b.rvProducts.layoutManager = LinearLayoutManager(this)
        b.rvProducts.adapter = adapter
        refresh()
    }

    private fun refresh() {
        val products = AppRepository.productsByStatus(status)
        adapter.submit(products, status)
        b.tvEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showSheet(p: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle(p.name)
            .setMessage(
                "Vendedor: ${p.sellerName}\nCategoría: ${p.category}\nPrecio: ${p.price.toCop()}\n" +
                    "Stock: ${p.stock}\nTallas: ${p.sizes.joinToString(", ")}\n\n${p.description}"
            )
            .setPositiveButton("Cerrar", null)
            .show()
    }
}
