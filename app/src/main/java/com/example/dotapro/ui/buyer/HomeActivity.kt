package com.example.dotapro.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dotapro.R
import com.example.dotapro.data.local.SessionManager
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityHomeBinding
import com.example.dotapro.util.NavHelper
import com.example.dotapro.util.requireUser

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding
    private lateinit var adapter: ProductAdapter
    private var selectedCategory = "Todos"
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = requireUser() ?: return
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = ProductAdapter(emptyList()) { product ->
            startActivity(
                Intent(this, ProductDetailActivity::class.java)
                    .putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
            )
        }
        b.rvProducts.layoutManager = GridLayoutManager(this, 2)
        b.rvProducts.adapter = adapter

        b.search.etSearch.hint = "Buscar uniformes, calzado, EPP..."
        b.search.etSearch.doAfterTextChanged {
            query = it?.toString().orEmpty().trim()
            refresh()
        }
        b.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            selectedCategory = when (ids.firstOrNull()) {
                R.id.chipUniformes -> "Uniformes"
                R.id.chipCalzado -> "Calzado"
                R.id.chipEpp -> "EPP"
                else -> "Todos"
            }
            refresh()
        }
        b.tvVerTodo.setOnClickListener {
            b.search.etSearch.text.clear()
            b.chipGroup.check(R.id.chipTodos)
        }
        b.flCart.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }

        NavHelper.setup(this, b.bottomNav, user.role, R.id.nav_inicio)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!::b.isInitialized) return
        val user = SessionManager.currentUser ?: return
        b.tvAddress.text = user.address.ifBlank { "Agrega tu dirección en el perfil" }
        val count = AppRepository.cartCount()
        b.tvCartBadge.text = count.toString()
        b.tvCartBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        refresh()
    }

    private fun handleIntent(intent: Intent) {
        if (intent.getBooleanExtra(NavHelper.EXTRA_FOCUS_SEARCH, false)) {
            b.search.etSearch.requestFocus()
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(b.search.etSearch, InputMethodManager.SHOW_IMPLICIT)
            intent.removeExtra(NavHelper.EXTRA_FOCUS_SEARCH)
        }
    }

    private fun refresh() {
        val products = AppRepository.activeProducts(query, selectedCategory)
        adapter.submit(products)
        b.tvEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
    }
}
