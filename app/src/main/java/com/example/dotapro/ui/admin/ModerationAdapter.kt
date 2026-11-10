package com.example.dotapro.ui.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dotapro.data.model.Product
import com.example.dotapro.data.model.ProductStatus
import com.example.dotapro.util.bindImage
import com.example.dotapro.databinding.ItemModerationBinding
import com.example.dotapro.util.toCop

class ModerationAdapter(
    private var items: List<Product>,
    private var status: ProductStatus,
    private val onPositive: (Product) -> Unit,   // aprobar / reactivar
    private val onNegative: (Product) -> Unit,   // rechazar / desactivar
    private val onSheet: (Product) -> Unit
) : RecyclerView.Adapter<ModerationAdapter.ViewHolder>() {

    class ViewHolder(val b: ItemModerationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemModerationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = items[position]
        val b = holder.b
        p.bindImage(b.tvEmoji, b.ivImage)
        b.flImage.backgroundTintList = ColorStateList.valueOf(Color.parseColor(p.bgColor))
        b.tvName.text = p.name
        b.tvSeller.text = "${p.sellerName} · ${p.price.toCop()}"

        when (status) {
            ProductStatus.PENDING -> {
                b.btnApprove.visibility = View.VISIBLE
                b.btnReject.visibility = View.VISIBLE
                b.btnApprove.text = "Aprobar"
                b.btnReject.text = "Rechazar"
            }
            ProductStatus.ACTIVE -> {
                b.btnApprove.visibility = View.GONE
                b.btnReject.visibility = View.VISIBLE
                b.btnReject.text = "Desactivar"
            }
            ProductStatus.INACTIVE -> {
                b.btnApprove.visibility = View.VISIBLE
                b.btnReject.visibility = View.GONE
                b.btnApprove.text = "Reactivar"
            }
        }
        b.btnApprove.setOnClickListener { onPositive(p) }
        b.btnReject.setOnClickListener { onNegative(p) }
        b.tvSheet.setOnClickListener { onSheet(p) }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<Product>, newStatus: ProductStatus) {
        items = newItems
        status = newStatus
        notifyDataSetChanged()
    }
}
