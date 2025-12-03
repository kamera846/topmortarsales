package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.topmortar.topmortarsales.R

class MyCircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = context.getColor(R.color.text_black)
        textSize = 42f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    var progress = 0f
        set(value) {
            field = value
            invalidate()
        }

    var text = "0"
        set(value) {
            field = value
            invalidate()
        }

    var trackColor: Int = context.getColor(R.color.darkLight)
        set(value) {
            field = value
            trackPaint.color = value
            invalidate()
        }

    var progressColor: Int = context.getColor(R.color.primary_200)
        set(value) {
            field = value
            progressPaint.color = value
            invalidate()
        }

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MyCircularProgressView)

            trackColor = a.getColor(
                R.styleable.MyCircularProgressView_trackColor,
                trackColor
            )

            progressColor = a.getColor(
                R.styleable.MyCircularProgressView_progressColor,
                progressColor
            )

            progress = a.getFloat(
                R.styleable.MyCircularProgressView_progressValue,
                0f
            )

            text = a.getString(
                R.styleable.MyCircularProgressView_centerText
            ) ?: text

            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = 120
        val width = resolveSize(defaultSize, widthMeasureSpec)
        val height = resolveSize(defaultSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = width / 2f - 20
        val centerX = width / 2f
        val centerY = height / 2f

        canvas.drawCircle(centerX, centerY, radius, trackPaint)

        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rect, -90f, (progress / 100f) * 360f, false, progressPaint)

        canvas.drawText(text, centerX, centerY + 15f, textPaint)
    }
}