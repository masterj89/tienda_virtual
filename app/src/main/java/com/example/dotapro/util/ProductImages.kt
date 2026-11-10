package com.example.dotapro.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.dotapro.data.model.Product

/**
 * Muestra la imagen local del producto (res/drawable/<imageName>.png|jpg|webp).
 * Si no hay imagen o el archivo no existe, muestra el emoji como respaldo.
 */
fun Product.bindImage(emojiView: TextView, imageView: ImageView) {
    val resId = if (imageName.isBlank()) 0 else
        imageView.resources.getIdentifier(imageName, "drawable", imageView.context.packageName)

    if (resId != 0) {
        imageView.setImageResource(resId)
        imageView.visibility = View.VISIBLE
        emojiView.visibility = View.GONE
    } else {
        imageView.visibility = View.GONE
        emojiView.visibility = View.VISIBLE
        emojiView.text = emoji
    }
}
