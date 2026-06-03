package com.verbally.app.overlay

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal class RecordingWaveformView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(OverlayColorDefaults.ACTIVE_WAVEFORM_COLOR_RES)
        style = Paint.Style.FILL
    }
    private val barBounds = RectF()
    private var phase = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1_000L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val marks = RecordingWaveformModel.marks(currentLevel, phase)
        val horizontalInset = width * RecordingWaveformModel.HORIZONTAL_INSET_FRACTION
        val contentWidth = width - horizontalInset * 2
        val step = contentWidth / (marks.size - 1).coerceAtLeast(1)
        for (index in marks.indices) {
            val mark = marks[index]
            val barWidth = max(2f, step * mark.widthScale)
            val barHeight = max(barWidth, height * mark.heightScale)
            val centerX = horizontalInset + step * index
            barBounds.set(
                centerX - barWidth / 2f,
                centerY - barHeight / 2f,
                centerX + barWidth / 2f,
                centerY + barHeight / 2f,
            )
            canvas.drawRoundRect(barBounds, barWidth / 2f, barWidth / 2f, paint)
        }
    }

    private var currentLevel = 0f

    fun setLiveLevel(level: Float) {
        currentLevel = level.coerceIn(0f, 1f)
        invalidate()
    }
}

internal class ProcessingDotsView(
    context: Context,
    @ColorInt dotColor: Int,
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dotColor
        style = Paint.Style.FILL
    }
    private var phase = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1_000L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val count = 9
        val horizontalInset = width * 0.18f
        val contentWidth = width - horizontalInset * 2
        val dotRadius = min(width / 38f, height / 11f)
        val step = contentWidth / (count - 1).coerceAtLeast(1)
        for (index in 0 until count) {
            val wave = ((phase * Math.PI * 2) - index * 0.42).toFloat()
            val alpha = (0.22f + 0.46f * ((kotlin.math.sin(wave) + 1f) / 2f))
            paint.alpha = (alpha * 255).roundToInt()
            canvas.drawCircle(horizontalInset + step * index, height / 2f, dotRadius, paint)
        }
        paint.alpha = 255
    }
}

internal class SpinnerRingView(
    context: Context,
    @ColorInt backgroundColor: Int,
    @ColorInt trackColor: Int,
    @ColorInt progressColor: Int,
) : FrameLayout(context) {
    init {
        layoutParams = LinearLayout.LayoutParams(
            OverlayVisualDefaults.ACTIVE_BUTTON_SIZE_DP.dp(context),
            OverlayVisualDefaults.ACTIVE_BUTTON_SIZE_DP.dp(context),
        )
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = OverlayVisualDefaults.ACTIVE_BUTTON_SIZE_DP.dp(context) / 2f
            setColor(backgroundColor)
        }
        elevation = 4f
        addView(
            RingView(context, trackColor, progressColor),
            LayoutParams(
                OverlayVisualDefaults.ACTIVE_BUTTON_ICON_SIZE_DP.dp(context),
                OverlayVisualDefaults.ACTIVE_BUTTON_ICON_SIZE_DP.dp(context),
                Gravity.CENTER,
            ),
        )
    }
}

private class RingView(
    context: Context,
    @ColorInt trackColor: Int,
    @ColorInt progressColor: Int,
) : View(context) {
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = trackColor
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = progressColor
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    private val arcBounds = RectF()
    private val animator = ObjectAnimator.ofFloat(this, View.ROTATION, 0f, 360f).apply {
        duration = 900L
        repeatCount = ValueAnimator.INFINITE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = 6.5f
        arcBounds.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint)
        canvas.drawArc(arcBounds, -70f, 120f, false, progressPaint)
    }
}

private fun Int.dp(context: Context): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        context.resources.displayMetrics,
    ).roundToInt()
