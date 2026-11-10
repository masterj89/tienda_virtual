package com.example.dotapro.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityCartBinding
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toCop
import com.example.dotapro.util.toast

class CartActivity : AppCompatActivity() {

    private lateinit var b: ActivityCartBinding
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireUser() ?: return
        b = ActivityCartBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = CartAdapter(AppRepository.cart) { refresh() }
        b.rvCart.layoutManager = LinearLayoutManager(this)
        b.rvCart.adapter = adapter

        b.btnCheckout.setOnClickListener {
            if (AppRepository.cart.isEmpty()) toast("Tu carrito está vacío")
            else startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (::b.isInitialized) refresh()
    }

    private fun refresh() {
        adapter.notifyDataSetChanged()
        val empty = AppRepository.cart.isEmpty()
        b.toolbar.setup(this, "Carrito (${AppRepository.cartCount()})", showBack = true)
        b.tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        b.rvCart.visibility = if (empty) View.GONE else View.VISIBLE
        b.tvSubtotal.text = AppRepository.subtotal().toCop()
        b.tvIva.text = AppRepository.iva().toCop()
        b.tvShipping.text = AppRepository.shipping().toCop()
        b.tvTotal.text = AppRepository.total().toCop()
    }
}
