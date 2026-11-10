package com.example.dotapro.ui.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dotapro.data.model.User
import com.example.dotapro.databinding.ItemUserBinding

class UserAdapter(
    private var items: List<User>,
    private val onEdit: (User) -> Unit,
    private val onToggle: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val avatarColors = listOf("#DCE6F5", "#FDE6CF", "#D9F0E3")

    class ViewHolder(val b: ItemUserBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]
        val b = holder.b
        b.tvAvatar.text = user.name.take(1).uppercase()
        b.tvAvatar.backgroundTintList = ColorStateList.valueOf(Color.parseColor(avatarColors[user.id % avatarColors.size]))
        b.tvName.text = user.name
        b.tvEmail.text = user.email
        b.tvRole.text = user.role.label
        b.vStatus.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(if (user.active) "#2E9F6B" else "#E04848"))
        b.tvToggle.text = if (user.active) "Suspender" else "Activar"
        b.tvToggle.setTextColor(Color.parseColor(if (user.active) "#E04848" else "#2E9F6B"))
        b.tvEdit.setOnClickListener { onEdit(user) }
        b.tvToggle.setOnClickListener { onToggle(user) }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<User>) {
        items = newItems
        notifyDataSetChanged()
    }
}
