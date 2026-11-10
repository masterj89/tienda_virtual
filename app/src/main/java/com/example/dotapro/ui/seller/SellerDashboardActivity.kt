package com.example.dotapro.ui.seller

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dotapro.R
import com.example.dotapro.data.local.SessionManager
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivitySellerDashboardBinding
import com.example.dotapro.databinding.ItemOrderRowBinding
import com.example.dotapro.util.NavHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toCompactCop

class SellerDashboardActivity : AppCompatActivity() {

    private lateinit var b: ActivitySellerDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = requireUser() ?: return
        b = ActivitySellerDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setup(this, "Panel", showBack = false, badge = "VENDEDOR")
        b.tvGreeting.text = "Hola, ${user.name.split(" ").first()}"
        b.barChart.values = AppRepository.weeklySales()
        NavHelper.setup(this, b.bottomNav, user.role, R.id.nav_panel)
    }

    override fun onResume() {
        super.onResume()
        if (!::b.isInitialized) return
        val user = SessionManager.currentUser ?: return

        b.cardSales.tvLabel.text = "Ventas del mes"
        b.cardSales.tvValue.text = AppRepository.sellerSales(user.id).toCompactCop()
        b.cardOrders.tvLabel.text = "Pedidos"
        b.cardOrders.tvValue.text = AppRepository.sellerOrderCount(user.id).toString()
        b.cardProducts.tvLabel.text = "Productos"
        b.cardProducts.tvValue.text = AppRepository.productsBySeller(user.id).size.toString()
        b.cardLowStock.tvLabel.text = "Stock bajo"
        b.cardLowStock.tvValue.text = AppRepository.lowStockCount(user.id).toString()
        b.cardLowStock.tvValue.setTextColor(Color.parseColor("#E04848"))

        b.llOrders.removeAllViews()
        AppRepository.recentOrders(user.id).forEach { order ->
            val row = ItemOrderRowBinding.inflate(layoutInflater, b.llOrders, false)
            row.tvOrder.text = "#${order.id} · ${order.description}"
            row.tvStatus.text = order.status
            row.tvStatus.setBackgroundResource(
                if (order.status == "Pagado") R.drawable.bg_status_green else R.drawable.bg_status_navy
            )
            b.llOrders.addView(row.root)
        }
    }
}
