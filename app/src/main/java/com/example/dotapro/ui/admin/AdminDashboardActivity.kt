package com.example.dotapro.ui.admin

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dotapro.R
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityAdminDashboardBinding
import com.example.dotapro.databinding.ItemAttentionBinding
import com.example.dotapro.databinding.ItemCategoryBarBinding
import com.example.dotapro.util.NavHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toast

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var b: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = requireUser() ?: return
        b = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setup(this, "Administración", showBack = false, badge = "ADMIN")
        NavHelper.setup(this, b.bottomNav, user.role, R.id.nav_admin_panel)
        renderCategories()
    }

    override fun onResume() {
        super.onResume()
        if (!::b.isInitialized) return
        b.cardUsers.tvLabel.text = "Usuarios"
        b.cardUsers.tvValue.text = AppRepository.userCount().toString()
        b.cardSellers.tvLabel.text = "Vendedores"
        b.cardSellers.tvValue.text = AppRepository.sellerCount().toString()
        b.cardPending.tvLabel.text = "Pendientes"
        b.cardPending.tvValue.text = AppRepository.pendingCount().toString()
        b.cardPending.tvValue.setTextColor(Color.parseColor("#E04848"))
        renderAttention()
    }

    private fun renderCategories() {
        b.llCategories.removeAllViews()
        var first = true
        AppRepository.salesByCategory().forEach { (category, percent) ->
            val row = ItemCategoryBarBinding.inflate(layoutInflater, b.llCategories, false)
            row.tvCategory.text = category
            row.progress.progress = percent
            if (first) row.progress.setIndicatorColor(Color.parseColor("#F28C28"))
            first = false
            b.llCategories.addView(row.root)
        }
    }

    private fun renderAttention() {
        b.llAttention.removeAllViews()
        val pending = AppRepository.pendingCount()
        addAttention("$pending productos por aprobar", "Revisar", "#E04848") {
            startActivity(Intent(this, ModerationActivity::class.java))
        }
        // Datos de ejemplo: aún no existen solicitudes ni reportes en el repositorio
        addAttention("3 solicitudes de vendedor", "Revisar", "#F28C28") {
            startActivity(Intent(this, UsersActivity::class.java))
        }
        addAttention("2 usuarios reportados", "Ver", "#F28C28") {
            toast("Pendiente: módulo de reportes")
        }
    }

    private fun addAttention(text: String, action: String, dotColor: String, onClick: () -> Unit) {
        val row = ItemAttentionBinding.inflate(layoutInflater, b.llAttention, false)
        row.tvText.text = text
        row.tvAction.text = action
        row.vDot.backgroundTintList = ColorStateList.valueOf(Color.parseColor(dotColor))
        row.tvAction.setOnClickListener { onClick() }
        b.llAttention.addView(row.root)
    }
}
