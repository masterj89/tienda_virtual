package com.example.dotapro.ui.components

import android.content.Context
import android.content.DialogInterface
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import com.example.dotapro.data.model.Role
import com.example.dotapro.data.model.User
import com.example.dotapro.databinding.DialogUserBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Diálogo para crear (user == null) o editar un usuario. Se usa en registro y en administración. */
object UserDialog {

    fun show(
        context: Context,
        user: User?,
        allowAdmin: Boolean,
        onSave: (name: String, email: String, role: Role, password: String) -> Unit
    ) {
        val b = DialogUserBinding.inflate(LayoutInflater.from(context))
        val roles = if (allowAdmin) Role.values().toList() else listOf(Role.BUYER, Role.SELLER)
        b.spRole.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, roles.map { it.label })

        if (user != null) {
            b.etName.setText(user.name)
            b.etEmail.setText(user.email)
            b.spRole.setSelection(roles.indexOf(user.role).coerceAtLeast(0))
            b.tilPassword.visibility = View.GONE
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(if (user == null) "Nuevo usuario" else "Editar usuario")
            .setView(b.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val name = b.etName.text.toString().trim()
                val email = b.etEmail.text.toString().trim()
                val password = b.etPassword.text.toString()

                when {
                    name.isEmpty() -> { b.etName.error = "Ingresa el nombre" }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { b.etEmail.error = "Correo no válido" }
                    user == null && password.length < 6 -> { b.etPassword.error = "Mínimo 6 caracteres" }
                    else -> {
                        onSave(name, email, roles[b.spRole.selectedItemPosition], password)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }
}
