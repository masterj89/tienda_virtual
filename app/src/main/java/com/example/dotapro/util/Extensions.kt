package com.example.dotapro.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.example.dotapro.data.local.SessionManager
import com.example.dotapro.data.model.User
import com.example.dotapro.databinding.IncludeToolbarBinding
import com.example.dotapro.ui.auth.LoginActivity
import java.text.NumberFormat
import java.util.Locale

/** 59900 -> "$ 59.900" */
fun Int.toCop(): String = "$ " + NumberFormat.getInstance(Locale("es", "CO")).format(this)

/** 4.200.000 -> "$ 4,2 M" */
fun Int.toCompactCop(): String =
    if (this >= 1_000_000) String.format(Locale("es", "CO"), "$ %.1f M", this / 1_000_000.0) else toCop()

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

/** Devuelve el usuario en sesión; si no hay (p. ej. el proceso se reinició) vuelve al login. */
fun Activity.requireUser(): User? {
    val user = SessionManager.currentUser
    if (user == null) {
        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
    return user
}

/** Configura el encabezado azul reutilizable (include_toolbar.xml). */
fun IncludeToolbarBinding.setup(activity: Activity, title: String, showBack: Boolean, badge: String? = null) {
    tvTitle.text = title
    ivBack.visibility = if (showBack) View.VISIBLE else View.GONE
    ivBack.setOnClickListener { activity.finish() }
    if (badge != null) {
        tvBadge.text = badge
        tvBadge.visibility = View.VISIBLE
    }
}
