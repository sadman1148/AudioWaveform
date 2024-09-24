package com.mpower.dtp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class AudioWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private var waveformData: FloatArray = floatArrayOf()
    private val gradientColors = intArrayOf(
        Color.YELLOW,
        Color.RED
    )
    private lateinit var gradient: LinearGradient

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gradient = LinearGradient(
            0f, height.toFloat(), 0f, 0f,
            gradientColors,
            null,
            Shader.TileMode.CLAMP
        )
    }

    fun updateWaveform(data: FloatArray) {
        waveformData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (waveformData.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2

        val barWidth = width / waveformData.size
        val gap = barWidth * 0.1f // 10% gap between bars

        paint.shader = gradient

        for (i in waveformData.indices) {
            val amplitude = waveformData[i] * height / 2
            val left = i * barWidth + gap / 2
            val top = centerY - amplitude
            val right = (i + 1) * barWidth - gap / 2
            val bottom = centerY + amplitude

            canvas.drawRect(left, top, right, bottom, paint)
        }
    }
}