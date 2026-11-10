package com.example.dotapro.ui.seller

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dotapro.R
import com.example.dotapro.data.model.Product
import com.example.dotapro.data.model.ProductStatus
import com.example.dotapro.util.bindImage
import com.example.dotapro.databinding.ItemMyProductBinding
import com.example.dotapro.util.toCop

class MyProductAdapter(
    private var items: List<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<MyProductAdapter.ViewHolder>() {

    class ViewHolder(val b: ItemMyProductBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMyProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = items[position]
        val b = holder.b
        p.bindImage(b.tvEmoji, b.ivImage)
        b.flImage.backgroundTintList = ColorStateList.valueOf(Color.parseColor(p.bgColor))
        b.tvName.text = p.name
        b.tvInfo.text = "Stock ${p.stock} · ${p.price.toCop()}"
        b.tvStatus.text = p.status.label
        b.tvStatus.setBackgroundResource(
            when (p.status) {
                ProductStatus.ACTIVE -> R.drawable.bg_status_green
                ProductStatus.PENDING -> R.drawable.bg_status_orange
                ProductStatus.INACTIVE -> R.drawable.bg_status_gray
            }
        )
        b.tvEdit.setOnClickListener { onEdit(p) }
        b.tvDelete.setOnClickListener { onDelete(p) }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
