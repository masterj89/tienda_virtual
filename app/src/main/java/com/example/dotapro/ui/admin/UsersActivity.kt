package com.example.dotapro.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dotapro.R
import com.example.dotapro.data.model.Role
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.databinding.ActivityUsersBinding
import com.example.dotapro.ui.components.UserDialog
import com.example.dotapro.util.NavHelper
import com.example.dotapro.util.requireUser
import com.example.dotapro.util.setup
import com.example.dotapro.util.toast

class UsersActivity : AppCompatActivity() {

    private lateinit var b: ActivityUsersBinding
    private lateinit var adapter: UserAdapter
    private var query = ""
    private var roleFilter: Role? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val admin = requireUser() ?: return
        b = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setup(this, "Usuarios", showBack = false, badge = "ADMIN")
        NavHelper.setup(this, b.bottomNav, admin.role, R.id.nav_usuarios)

        adapter = UserAdapter(
            emptyList(),
            onEdit = { user ->
                UserDialog.show(this, user, allowAdmin = true) { name, email, role, _ ->
                    if (AppRepository.updateUser(user.id, name, email, role)) toast("Usuario actualizado")
                    else toast("Ese correo ya está en uso")
                    refresh()
                }
            },
            onToggle = { user ->
                if (user.id == admin.id) {
                    toast("No puedes suspender tu propia cuenta")
                } else {
                    AppRepository.setUserActive(user.id, !user.active)
                    refresh()
                }
            }
        )
        b.rvUsers.layoutManager = LinearLayoutManager(this)
        b.rvUsers.adapter = adapter

        b.search.etSearch.hint = "Buscar por nombre o correo"
        b.search.etSearch.doAfterTextChanged {
            query = it?.toString().orEmpty().trim()
            refresh()
        }
        b.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            roleFilter = when (ids.firstOrNull()) {
                R.id.chipBuyer -> Role.BUYER
                R.id.chipSeller -> Role.SELLER
                R.id.chipAdmin -> Role.ADMIN
                else -> null
            }
            refresh()
        }
        b.fabAdd.setOnClickListener {
            UserDialog.show(this, null, allowAdmin = true) { name, email, role, password ->
                if (AppRepository.addUser(name, email, role, password) == null) toast("Ese correo ya existe")
                else toast("Usuario creado")
                refresh()
            }
        }
        refresh()
    }

    private fun refresh() {
        adapter.submit(AppRepository.searchUsers(query, roleFilter))
    }
}
