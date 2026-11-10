package com.example.dotapro.util

import android.app.Activity
import android.content.Intent
import com.example.dotapro.R
import com.example.dotapro.data.model.Role
import com.example.dotapro.ui.admin.AdminDashboardActivity
import com.example.dotapro.ui.admin.ModerationActivity
import com.example.dotapro.ui.admin.UsersActivity
import com.example.dotapro.ui.buyer.CartActivity
import com.example.dotapro.ui.buyer.HomeActivity
import com.example.dotapro.ui.profile.ProfileActivity
import com.example.dotapro.ui.seller.MyProductsActivity
import com.example.dotapro.ui.seller.SellerDashboardActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/** Configura la barra de navegación inferior según el rol del usuario. */
object NavHelper {

    const val EXTRA_FOCUS_SEARCH = "focus_search"

    fun setup(activity: Activity, nav: BottomNavigationView, role: Role, selectedId: Int) {
        nav.menu.clear()
        nav.inflateMenu(
            when (role) {
                Role.BUYER -> R.menu.menu_nav_buyer
                Role.SELLER -> R.menu.menu_nav_seller
                Role.ADMIN -> R.menu.menu_nav_admin
            }
        )
        nav.selectedItemId = selectedId

        nav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedId) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_inicio -> go(activity, HomeActivity::class.java)
                R.id.nav_buscar -> go(activity, HomeActivity::class.java, focusSearch = true)
                R.id.nav_carrito -> activity.startActivity(Intent(activity, CartActivity::class.java))
                R.id.nav_perfil, R.id.nav_ajustes -> go(activity, ProfileActivity::class.java)
                R.id.nav_panel -> go(activity, SellerDashboardActivity::class.java)
                R.id.nav_mis_productos -> go(activity, MyProductsActivity::class.java)
                R.id.nav_pedidos -> activity.toast("Pedidos: próximamente")
                R.id.nav_admin_panel -> go(activity, AdminDashboardActivity::class.java)
                R.id.nav_usuarios -> go(activity, UsersActivity::class.java)
                R.id.nav_admin_productos -> go(activity, ModerationActivity::class.java)
            }
            false   // la pestaña seleccionada de ESTA pantalla no cambia
        }
    }

    private fun go(activity: Activity, target: Class<*>, focusSearch: Boolean = false) {
        activity.startActivity(
            Intent(activity, target)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_FOCUS_SEARCH, focusSearch)
        )
    }
}
