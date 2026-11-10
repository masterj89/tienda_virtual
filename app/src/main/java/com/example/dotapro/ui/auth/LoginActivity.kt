package com.example.dotapro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.dotapro.data.local.SessionManager
import com.example.dotapro.data.model.Role
import com.example.dotapro.data.model.User
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityLoginBinding
import com.example.dotapro.ui.admin.AdminDashboardActivity
import com.example.dotapro.ui.buyer.HomeActivity
import com.example.dotapro.ui.components.UserDialog
import com.example.dotapro.ui.seller.SellerDashboardActivity
import com.example.dotapro.util.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.setOnClickListener { attemptLogin() }
        b.btnGoogle.setOnClickListener {
            // TODO: Credential Manager + Firebase Auth (Google Sign-In)
            toast("Inicio con Google: pendiente de conectar")
        }
        b.btnFingerprint.setOnClickListener { loginWithFingerprint() }
        b.tvRegister.setOnClickListener { showRegisterDialog() }
    }

    // ---------- Correo y contraseña ----------
    private fun attemptLogin() {
        val email = b.etEmail.text.toString().trim()
        val password = b.etPassword.text.toString()
        b.tilEmail.error = null
        b.tilPassword.error = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            b.tilEmail.error = "Correo no válido"
            return
        }
        if (password.length < 6) {
            b.tilPassword.error = "Mínimo 6 caracteres"
            return
        }

        // TODO: reemplazar por la API de autenticación (Firebase Auth, backend propio...)
        val user = AppRepository.login(email, password)
        when {
            user == null -> b.tilPassword.error = "Correo o contraseña incorrectos"
            !user.active -> toast("Tu cuenta está suspendida. Contacta al administrador")
            else -> openHome(user)
        }
    }

    // ---------- Registro ----------
    private fun showRegisterDialog() {
        UserDialog.show(this, null, allowAdmin = false) { name, email, role, password ->
            if (AppRepository.addUser(name, email, role, password) == null) {
                toast("Ese correo ya está registrado")
            } else {
                b.etEmail.setText(email)
                toast("Cuenta creada. Ya puedes ingresar")
            }
        }
    }

    // ---------- Huella ----------
    private fun loginWithFingerprint() {
        val email = SessionManager.lastEmail
        if (!SessionManager.biometricEnabled || email == null) {
            toast("Ingresa con tu clave y activa \"Ingreso con huella\" en tu perfil")
            return
        }
        when (BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                toast("No hay huellas registradas en el dispositivo")
                return
            }
            else -> {
                toast("Huella no disponible en este dispositivo")
                return
            }
        }

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val user = AppRepository.findByEmail(email)
                    if (user != null && user.active) openHome(user) else toast("Cuenta no disponible")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    toast(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    toast("Huella no reconocida")
                }
            })

        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Ingresar a DotaPro")
                .setSubtitle("Usa tu huella dactilar")
                .setNegativeButtonText("Cancelar")
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build()
        )
    }

    // ---------- Navegación por rol ----------
    private fun openHome(user: User) {
        SessionManager.login(user)
        val target = when (user.role) {
            Role.BUYER -> HomeActivity::class.java
            Role.SELLER -> SellerDashboardActivity::class.java
            Role.ADMIN -> AdminDashboardActivity::class.java
        }
        startActivity(Intent(this, target))
        finish()
    }
}
