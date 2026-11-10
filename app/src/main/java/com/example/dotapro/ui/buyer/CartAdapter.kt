package com.example.dotapro.ui.buyer

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dotapro.data.model.CartItem
import com.example.dotapro.data.repository.AppRepository
import com.example.dotapro.util.bindImage
import com.example.dotapro.databinding.ItemCartBinding
import com.example.dotapro.util.toCop
import com.example.dotapro.util.toast

class CartAdapter(
    private val items: List<CartItem>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.b
        item.product.bindImage(b.tvEmoji, b.ivImage)
        b.flImage.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.product.bgColor))
        b.tvName.text = item.product.name
        b.tvSize.text = "Talla ${item.size}"
        b.tvPrice.text = item.product.price.toCop()
        b.tvQty.text = item.quantity.toString()

        b.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                onChanged()
            }
        }
        b.btnPlus.setOnClickListener {
            if (item.quantity < item.product.stock) {
                item.quantity++
                onChanged()
            } else {
                it.context.toast("No hay más unidades disponibles")
            }
        }
        b.tvRemove.setOnClickListener {
            AppRepository.removeFromCart(item)
            onChanged()
        }
    }

    override fun getItemCount() = items.size
}
