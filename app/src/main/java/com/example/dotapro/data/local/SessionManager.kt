package com.example.dotapro.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.dotapro.data.model.User
import com.example.dotapro.data.repository.AppRepository

/** Guarda el usuario en sesión y las preferencias locales (huella, último correo). */
object SessionManager {
    private lateinit var prefs: SharedPreferences

    var currentUser: User? = null
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences("dotapro_session", Context.MODE_PRIVATE)
    }

    var lastEmail: String?
        get() = prefs.getString("last_email", null)
        set(value) = prefs.edit().putString("last_email", value).apply()

    var biometricEnabled: Boolean
        get() = prefs.getBoolean("biometric", false)
        set(value) = prefs.edit().putBoolean("biometric", value).apply()

    fun login(user: User) {
        currentUser = user
        lastEmail = user.email
    }

    fun logout() {
        currentUser = null
        AppRepository.clearCart()
    }
}
