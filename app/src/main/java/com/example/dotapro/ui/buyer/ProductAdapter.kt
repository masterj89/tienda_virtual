package com.example.dotapro.ui.buyer

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dotapro.data.model.Product
import com.example.dotapro.util.bindImage
import com.example.dotapro.databinding.ItemProductBinding
import com.example.dotapro.util.toCop

class ProductAdapter(
    private var items: List<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        product.bindImage(holder.b.tvEmoji, holder.b.ivImage)
        holder.b.tvName.text = product.name
        holder.b.tvPrice.text = product.price.toCop()
        holder.b.flImage.backgroundTintList = ColorStateList.valueOf(Color.parseColor(product.bgColor))
        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
