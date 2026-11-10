package com.example.dotapro.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import com.example.dotapro.R
import com.example.dotapro.data.local.SessionManager
import com.example.dotapro.data.model.Role
import com.example.dotapro.data.model.User
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityProfileBinding
import com.example.dotapro.ui.auth.LoginActivity
import com.example.dotapro.util.NavHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Perfil compartido por los tres roles (en el admin aparece como "Ajustes"). */
class ProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileBinding
    private lateinit var user: User

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            b.ivAvatar.imageTintList = null
            b.ivAvatar.setPadding(0, 0, 0, 0)
            b.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            b.ivAvatar.setImageBitmap(bitmap)
            // TODO: subir la foto al almacenamiento (Firebase Storage / API)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = requireUser() ?: return
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        val isAdmin = user.role == Role.ADMIN
        b.toolbar.setup(this, if (isAdmin) "Ajustes" else "Mi perfil", showBack = false)
        NavHelper.setup(this, b.bottomNav, user.role, if (isAdmin) R.id.nav_ajustes else R.id.nav_perfil)

        fillForm()

        b.ivCamera.setOnClickListener { takePhoto.launch(null) }

        b.swBiometric.isChecked = SessionManager.biometricEnabled
        b.swBiometric.setOnCheckedChangeListener { button, checked ->
            if (checked &&
                BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS
            ) {
                button.isChecked = false
                toast("Tu dispositivo no tiene huella disponible o registrada")
            } else {
                SessionManager.biometricEnabled = checked
            }
        }

        b.btnSave.setOnClickListener { saveChanges() }
        b.tvLogout.setOnClickListener { logout() }
        b.tvDelete.setOnClickListener { confirmDelete() }
    }

    private fun fillForm() {
        b.tvName.text = user.name
        b.tvSubtitle.text = "${user.email} · ${user.role.label}"
        b.etName.setText(user.name)
        b.etPhone.setText(user.phone)
        b.etAddress.setText(user.address)
    }

    private fun saveChanges() {
        val name = b.etName.text.toString().trim()
        if (name.isEmpty()) {
            b.etName.error = "Ingresa tu nombre"
            return
        }
        AppRepository.updateProfile(user, name, b.etPhone.text.toString().trim(), b.etAddress.text.toString().trim())
        fillForm()
        toast("Cambios guardados")
    }

    private fun logout() {
        SessionManager.logout()
        goToLogin()
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar cuenta")
            .setMessage("Esta acción no se puede deshacer. ¿Deseas continuar?")
            .setPositiveButton("Eliminar") { _, _ ->
                AppRepository.deleteUser(user.id)
                SessionManager.logout()
                goToLogin()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
