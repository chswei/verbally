package com.verbally.app.overlay

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.edit
import com.verbally.app.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class FloatingDictationOverlay(
    private val context: Context,
    private val onStart: () -> Unit,
    private val onCancel: () -> Unit,
    private val onConfirm: () -> Unit,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val haptics = OverlayHaptics(context)
    private val preferences = context.getSharedPreferences(POSITION_PREFS, Context.MODE_PRIVATE)
    private val positionMemory = OverlayPositionMemory(loadSavedPosition())
    private val edgeMargin = OverlayVisualDefaults.EDGE_MARGIN_DP.toPx()
    private val bubbleSize = OverlayVisualDefaults.BUBBLE_SIZE_DP.toPx()
    private val bubbleCornerRadius = OverlayVisualDefaults.BUBBLE_CORNER_RADIUS_DP.toPx().toFloat()
    private val iconSize = OverlayVisualDefaults.ICON_SIZE_DP.toPx()
    private val activeButtonSize = OverlayVisualDefaults.ACTIVE_BUTTON_SIZE_DP.toPx()
    private val activeButtonIconSize = OverlayVisualDefaults.ACTIVE_BUTTON_ICON_SIZE_DP.toPx()
    private val activeCapsuleWidth = OverlayVisualDefaults.ACTIVE_CAPSULE_WIDTH_DP.toPx()
    private val activeCapsuleHeight = OverlayVisualDefaults.ACTIVE_CAPSULE_HEIGHT_DP.toPx()
    private val activeSpacing = OverlayVisualDefaults.ACTIVE_SPACING_DP.toPx()
    private val activeCapsuleCornerRadius =
        OverlayVisualDefaults.ACTIVE_CAPSULE_CORNER_RADIUS_DP.toPx().toFloat()
    private val rootTouchListener = DragTouchListener()
    private val session = OverlaySessionStateMachine()
    private var waveformLevel = 0f
    private var rootView: FrameLayout? = null
    private var currentLayoutParams: WindowManager.LayoutParams? = null
    private var customContentDescription: String? = null
    private var recordingWaveformView: RecordingWaveformView? = null

    val isShown: Boolean
        get() = rootView != null

    fun show() {
        if (!Settings.canDrawOverlays(context) || rootView != null) return
        attachFreshRoot()
    }

    fun hide() {
        rootView?.let { runCatching { windowManager.removeViewImmediate(it) } }
        rootView = null
        currentLayoutParams = null
        customContentDescription = null
        session.forceState(OverlayUiState.READY)
    }

    fun setState(next: OverlayUiState) {
        customContentDescription = null
        session.forceState(next)
        if (rootView != null) {
            replaceAttachedRoot()
        }
    }

    fun completeProcessing(message: String? = null) {
        session.onProcessingFinished()
        customContentDescription = message
        if (rootView != null) {
            replaceAttachedRoot()
        }
    }

    fun showMessage(message: String) {
        customContentDescription = message
        rootView?.contentDescription = message
    }

    fun setWaveformLevel(level: Float) {
        waveformLevel = level.coerceIn(0f, 1f)
        recordingWaveformView?.setLiveLevel(waveformLevel)
    }

    private fun renderState(root: FrameLayout) {
        root.removeAllViews()
        val content = when (session.state) {
            OverlayUiState.READY -> createReadyBubble()
            OverlayUiState.RECORDING -> createRecordingControls()
            OverlayUiState.PROCESSING -> createProcessingControls()
        }
        root.addView(content)
        root.contentDescription = customContentDescription ?: contentDescriptionFor(session.state)
        // Re-anchor immediately so right-edge bubbles do not shift under a new touch.
        realignToRememberedEdge(root)
    }

    private fun createReadyBubble(): View =
        FrameLayout(context).apply {
            minimumWidth = bubbleSize
            minimumHeight = bubbleSize
            background = roundedBackground(
                color = color(OverlayColorDefaults.READY_BUBBLE_BACKGROUND_RES),
                cornerRadius = bubbleCornerRadius,
            )
            elevation = 8f
            isClickable = true
            isFocusable = true
            contentDescription = customContentDescription ?: contentDescriptionFor(OverlayUiState.READY)
            bindDragAndClick(this) { handleReadyTap() }
            addView(
                ImageView(context).apply {
                    setImageResource(R.drawable.ic_verbally_waveform)
                    setColorFilter(color(OverlayColorDefaults.READY_ICON_COLOR_RES))
                    scaleType = ImageView.ScaleType.CENTER
                },
                FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER),
            )
        }

    private fun createRecordingControls(): View =
        createActiveControls(
            centerView = RecordingWaveformView(context).also {
                recordingWaveformView = it
                it.setLiveLevel(waveformLevel)
            },
            trailingView = iconButton(
                backgroundColor = color(OverlayColorDefaults.ACTIVE_CONFIRM_BACKGROUND_RES),
                iconRes = R.drawable.ic_verbally_check,
                iconTint = color(OverlayColorDefaults.ACTIVE_CONFIRM_ICON_COLOR_RES),
            ),
            onLeadingTap = { handleCancelTap() },
            onTrailingTap = { handleConfirmTap() },
            centerContentDescription = context.getString(R.string.overlay_recording),
            trailingContentDescription = context.getString(R.string.overlay_send_recording),
        )

    private fun createProcessingControls(): View =
        createActiveControls(
            centerView = ProcessingDotsView(
                context,
                dotColor = color(OverlayColorDefaults.PROCESSING_ACCENT_COLOR_RES),
            ),
            trailingView = SpinnerRingView(
                context,
                backgroundColor = color(OverlayColorDefaults.PROCESSING_BACKGROUND_RES),
                trackColor = color(OverlayColorDefaults.PROCESSING_TRACK_COLOR_RES),
                progressColor = color(OverlayColorDefaults.PROCESSING_ACCENT_COLOR_RES),
            ),
            onLeadingTap = null,
            onTrailingTap = null,
            centerContentDescription = context.getString(R.string.overlay_processing_transcription),
            trailingContentDescription = context.getString(R.string.overlay_processing),
        )

    private fun createActiveControls(
        centerView: View,
        trailingView: View,
        onLeadingTap: (() -> Unit)?,
        onTrailingTap: (() -> Unit)?,
        centerContentDescription: String,
        trailingContentDescription: String,
    ): View =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = false
            isFocusable = false
            val cancelButton = iconButton(
                backgroundColor = color(OverlayColorDefaults.ACTIVE_CANCEL_BACKGROUND_RES),
                iconRes = R.drawable.ic_verbally_close,
                iconTint = color(OverlayColorDefaults.ACTIVE_CANCEL_ICON_COLOR_RES),
            ).apply {
                contentDescription = context.getString(R.string.overlay_cancel_recording)
                bindDragAndClick(this, onLeadingTap)
            }
            val centerCapsule = FrameLayout(context).apply {
                background = roundedBackground(
                    color = color(OverlayColorDefaults.ACTIVE_CENTER_BACKGROUND_RES),
                    cornerRadius = activeCapsuleCornerRadius,
                )
                contentDescription = centerContentDescription
                bindDragOnly(this)
                addView(
                    centerView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER,
                    ),
                )
            }
            val actionButton = trailingView.apply {
                contentDescription = trailingContentDescription
                bindDragAndClick(this, onTrailingTap)
            }

            addView(cancelButton)
            addView(centerCapsule, activeCenterLayoutParams())
            addView(actionButton)
        }

    private fun activeCenterLayoutParams() =
        LinearLayout.LayoutParams(activeCapsuleWidth, activeCapsuleHeight).apply {
            marginStart = activeSpacing
            marginEnd = activeSpacing
        }

    private fun iconButton(
        @ColorInt backgroundColor: Int,
        iconRes: Int,
        @ColorInt iconTint: Int?,
    ): View =
        FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(activeButtonSize, activeButtonSize)
            background = roundedBackground(backgroundColor, activeButtonSize / 2f)
            elevation = 4f
            isClickable = true
            isFocusable = true
            addView(
                ImageView(context).apply {
                    setImageResource(iconRes)
                    iconTint?.let { setColorFilter(it) }
                    scaleType = ImageView.ScaleType.CENTER
                },
                FrameLayout.LayoutParams(activeButtonIconSize, activeButtonIconSize, Gravity.CENTER),
            )
        }

    private fun handleReadyTap() {
        session.onReadyBubbleTapped()
        customContentDescription = null
        replaceAttachedRoot()
        onStart()
    }

    private fun handleCancelTap() {
        if (session.state != OverlayUiState.RECORDING) return
        onCancel()
        session.onCancelTapped()
        waveformLevel = 0f
        customContentDescription = null
        replaceAttachedRoot()
    }

    private fun handleConfirmTap() {
        if (session.state != OverlayUiState.RECORDING) return
        session.onConfirmTapped()
        customContentDescription = null
        replaceAttachedRoot()
        onConfirm()
    }

    private fun attachFreshRoot() {
        val view = buildRootView()
        val params = layoutParams(view)
        currentLayoutParams = params
        windowManager.addView(view, params)
        rootView = view
    }

    private fun replaceAttachedRoot() {
        val existing = rootView ?: return
        runCatching { windowManager.removeViewImmediate(existing) }
        attachFreshRoot()
    }

    private fun buildRootView(): FrameLayout =
        FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
            hideWithInputMethodAnimation()
            renderState(this)
        }

    private fun bindDragOnly(target: View) {
        target.setOnTouchListener(rootTouchListener)
    }

    private fun bindDragAndClick(target: View, clickAction: (() -> Unit)? = null) {
        target.setOnClickListener {
            haptics.tap()
            clickAction?.invoke()
        }
        target.setOnTouchListener(DragTouchListener())
    }

    private fun View.hideWithInputMethodAnimation() {
        setWindowInsetsAnimationCallback(
            object : WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                override fun onProgress(
                    insets: WindowInsets,
                    runningAnimations: MutableList<WindowInsetsAnimation>,
                ): WindowInsets {
                    val inputMethodAnimating = runningAnimations.any {
                        it.typeMask and WindowInsets.Type.ime() != 0
                    }
                    if (rootView === this@hideWithInputMethodAnimation &&
                        inputMethodAnimating &&
                        !insets.isVisible(WindowInsets.Type.ime())
                    ) {
                        hide()
                    }
                    return insets
                }
            },
        )
    }

    private fun roundedBackground(@ColorInt color: Int, cornerRadius: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        this.cornerRadius = cornerRadius
        setColor(color)
    }

    @ColorInt
    private fun color(@ColorRes colorRes: Int): Int = context.getColor(colorRes)

    private fun layoutParams(view: View) = WindowManager.LayoutParams(
        view.resolvedWidth(),
        view.resolvedHeight(),
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        val position = positionMemory.currentPosition(
            screenWidth = screenWidth(),
            bubbleWidth = view.resolvedWidth(),
            edgeMargin = edgeMargin,
        )
        gravity = position.gravity
        x = position.x
        y = position.y
        windowAnimations = 0
    }

    private fun updatePosition(view: View, x: Int, y: Int) {
        val params = currentLayoutParams ?: return
        params.gravity = Gravity.TOP or Gravity.START
        params.x = x.coerceAtLeast(0)
        params.y = y.coerceAtLeast(0)
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun applyAnchoredPosition(view: View, position: OverlayPosition) {
        val params = currentLayoutParams ?: return
        params.width = view.resolvedWidth()
        params.height = view.resolvedHeight()
        params.gravity = position.gravity
        params.x = position.x
        params.y = position.y
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun snapAndRememberPosition(view: View, releasedX: Int, releasedY: Int) {
        val position = positionMemory.rememberSnappedPosition(
            releasedX = releasedX,
            releasedY = releasedY,
            bubbleWidth = view.resolvedWidth(),
            screenWidth = screenWidth(),
            edgeMargin = edgeMargin,
        )
        applyAnchoredPosition(view, position)
        preferences.edit {
            putBoolean(KEY_HAS_POSITION, true)
            putString(KEY_EDGE, position.edge?.name)
            putInt(KEY_Y, position.y)
        }
    }

    private fun realignToRememberedEdge(view: View) {
        val params = currentLayoutParams ?: return
        val position = positionMemory.currentPosition(
            screenWidth = screenWidth(),
            bubbleWidth = view.resolvedWidth(),
            edgeMargin = edgeMargin,
        )
        params.width = view.resolvedWidth()
        params.height = view.resolvedHeight()
        params.gravity = position.gravity
        params.x = position.x
        params.y = position.y
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun loadSavedPosition(): OverlayPosition? {
        if (!preferences.getBoolean(KEY_HAS_POSITION, false)) return null
        val edge = preferences.getString(KEY_EDGE, null)
            ?.let { runCatching { OverlayEdge.valueOf(it) }.getOrNull() }
            ?: return null
        return OverlayPosition(
            edge = edge,
            gravity = Gravity.TOP or Gravity.START,
            x = 0,
            y = preferences.getInt(KEY_Y, 0),
        )
    }

    private fun screenWidth(): Int =
        windowManager.currentWindowMetrics.bounds.width()

    private fun View.resolvedWidth(): Int {
        measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        return measuredWidth.coerceAtLeast(minimumWidth)
    }

    private fun View.resolvedHeight(): Int {
        measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        return measuredHeight.coerceAtLeast(minimumHeight)
    }

    private fun Int.toPx(): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            context.resources.displayMetrics,
        ).roundToInt()

    private fun contentDescriptionFor(state: OverlayUiState): String = when (state) {
        OverlayUiState.READY -> context.getString(R.string.overlay_start_dictation)
        OverlayUiState.RECORDING -> context.getString(R.string.overlay_recording)
        OverlayUiState.PROCESSING -> context.getString(R.string.overlay_processing)
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var downRawX = 0f
        private var downRawY = 0f
        private var startX = 0
        private var startY = 0
        private var dragging = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val movableRoot = rootView ?: return false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    val location = IntArray(2)
                    movableRoot.getLocationOnScreen(location)
                    startX = location[0]
                    startY = location[1]
                    dragging = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downRawX
                    val deltaY = event.rawY - downRawY
                    if (!dragging && (abs(deltaX) > touchSlopPx() || abs(deltaY) > touchSlopPx())) {
                        dragging = true
                        haptics.dragStart()
                    }
                    if (dragging) {
                        updatePosition(
                            view = movableRoot,
                            x = (startX + deltaX).roundToInt(),
                            y = (startY + deltaY).roundToInt(),
                        )
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        val params = currentLayoutParams
                        if (params != null) {
                            snapAndRememberPosition(movableRoot, params.x, params.y)
                            haptics.snap()
                        }
                    } else {
                        view.performClick()
                    }
                    return true
                }

                MotionEvent.ACTION_CANCEL -> return true
            }
            return false
        }
    }

    private fun touchSlopPx(): Int =
        android.view.ViewConfiguration.get(context).scaledTouchSlop

    private class RecordingWaveformView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(OverlayColorDefaults.ACTIVE_WAVEFORM_COLOR_RES)
            style = Paint.Style.FILL
        }
        private val barBounds = RectF()
        private val amplitudes = floatArrayOf(0.14f, 0.34f, 0.56f, 0.78f, 0.78f, 0.56f, 0.34f, 0.14f)
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
            val horizontalInset = width * 0.16f
            val contentWidth = width - horizontalInset * 2
            val spacing = contentWidth / (amplitudes.size + 1f)
            val barWidth = max(3f, spacing * 0.28f)
            val silenceThreshold = 0.05f
            for (index in amplitudes.indices) {
                val wave = ((phase * Math.PI * 2) + index * 0.42).toFloat()
                val animatedScale = 0.9f + 0.1f * kotlin.math.sin(wave).let { (it + 1f) / 2f }
                val barHeight = if (currentLevel <= silenceThreshold) {
                    barWidth
                } else {
                    val liveScale = 0.14f + currentLevel * 0.86f
                    min(height * 0.6f, height * amplitudes[index] * animatedScale * liveScale)
                }
                val centerX = horizontalInset + spacing * (index + 1)
                barBounds.set(
                    centerX - barWidth / 2f,
                    centerY - barHeight / 2f,
                    centerX + barWidth / 2f,
                    centerY + barHeight / 2f,
                )
                canvas.drawRoundRect(barBounds, barWidth, barWidth, paint)
            }
        }

        private var currentLevel = 0f

        fun setLiveLevel(level: Float) {
            currentLevel = level.coerceIn(0f, 1f)
            invalidate()
        }
    }

    private class ProcessingDotsView(
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

    private class SpinnerRingView(
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

    companion object {
        private const val POSITION_PREFS = "floating_dictation_overlay_position"
        private const val KEY_HAS_POSITION = "has_position"
        private const val KEY_EDGE = "edge"
        private const val KEY_Y = "y"

        private fun Int.dp(context: Context): Int =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                toFloat(),
                context.resources.displayMetrics,
            ).roundToInt()
    }
}
