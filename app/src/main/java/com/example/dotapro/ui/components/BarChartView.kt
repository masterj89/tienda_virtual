package com.example.dotapro.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/** Gráfico de barras simple: la última barra se resalta en naranja. */
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var values: List<Int> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7A90")
        textSize = resources.displayMetrics.density * 11
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val max = values.maxOrNull()?.toFloat() ?: return
        val innerWidth = (width - paddingLeft - paddingRight).toFloat()
        val labelHeight = textPaint.textSize * 1.8f
        val chartHeight = height - paddingTop - paddingBottom - labelHeight
        val slot = innerWidth / values.size
        val barWidth = slot * 0.62f

        values.forEachIndexed { index, value ->
            val barHeight = chartHeight * (value / max)
            val left = paddingLeft + index * slot + (slot - barWidth) / 2
            val bottom = paddingTop + chartHeight
            barPaint.color = if (index == values.lastIndex) Color.parseColor("#F28C28") else Color.parseColor("#DCE6F5")
            canvas.drawRoundRect(left, bottom - barHeight, left + barWidth, bottom, 8f, 8f, barPaint)
            canvas.drawText("S${index + 1}", paddingLeft + index * slot + slot / 2, bottom + labelHeight * 0.75f, textPaint)
        }
    }
}
