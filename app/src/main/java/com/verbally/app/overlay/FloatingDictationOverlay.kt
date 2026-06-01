package com.verbally.app.overlay

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
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
import com.verbally.app.system.RuntimeRepairTarget
import kotlin.math.abs
import kotlin.math.roundToInt

class FloatingDictationOverlay(
    private val context: Context,
    private val onStart: () -> Unit,
    private val onCancel: () -> Unit,
    private val onConfirm: () -> Unit,
    private val onRepair: (RuntimeRepairTarget) -> Unit = {},
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
    private var repairTarget: RuntimeRepairTarget? = null
    private var repairMessage: String? = null
    private var recordingWaveformView: RecordingWaveformView? = null
    private val configurationCallbacks = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {
            rootView?.post {
                rootView?.let(::realignToRememberedEdge)
            }
        }

        override fun onLowMemory() = Unit
    }

    init {
        context.registerComponentCallbacks(configurationCallbacks)
    }

    val isShown: Boolean
        get() = rootView != null

    val currentState: OverlayUiState
        get() = session.state

    fun show() {
        if (!Settings.canDrawOverlays(context) || rootView != null) return
        attachFreshRoot()
    }

    fun hide() {
        rootView?.let { runCatching { windowManager.removeViewImmediate(it) } }
        rootView = null
        currentLayoutParams = null
        customContentDescription = null
        repairTarget = null
        repairMessage = null
        session.forceState(OverlayUiState.READY)
    }

    fun dispose() {
        hide()
        runCatching { context.unregisterComponentCallbacks(configurationCallbacks) }
    }

    fun setState(next: OverlayUiState) {
        customContentDescription = null
        val previous = session.state
        session.forceState(next)
        if (rootView != null) {
            updateAttachedRoot(previous, session.state)
        }
    }

    fun showRepair(target: RuntimeRepairTarget, message: String) {
        repairTarget = target
        repairMessage = message
        customContentDescription = message
        session.forceState(OverlayUiState.REPAIR)
        if (rootView == null) {
            show()
        } else {
            refreshAttachedRoot()
        }
    }

    fun completeProcessing(message: String? = null) {
        val previous = session.state
        session.onProcessingFinished()
        customContentDescription = message
        if (rootView != null) {
            updateAttachedRoot(previous, session.state)
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
        recordingWaveformView = null
        val content = when (session.state) {
            OverlayUiState.READY -> createReadyBubble()
            OverlayUiState.RECORDING -> createRecordingControls()
            OverlayUiState.PROCESSING -> createProcessingControls()
            OverlayUiState.REPAIR -> createRepairBubble()
        }
        if (rootView === root) {
            val width = content.resolvedWidth()
            val height = content.resolvedHeight()
            root.removeAllViews()
            // In-place transitions resize while empty so active-state swaps do not briefly
            // draw new controls inside stale window bounds.
            realignToRememberedEdge(root, width, height)
        } else {
            root.removeAllViews()
        }
        root.addView(content)
        root.contentDescription = customContentDescription ?: contentDescriptionFor(session.state)
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

    private fun createRepairBubble(): View =
        FrameLayout(context).apply {
            minimumWidth = bubbleSize
            minimumHeight = bubbleSize
            background = roundedBackground(
                color = context.getColor(android.R.color.white),
                cornerRadius = bubbleCornerRadius,
            )
            elevation = 8f
            isClickable = true
            isFocusable = true
            contentDescription = repairMessage ?: context.getString(R.string.overlay_repair_required)
            bindDragAndClick(this) {
                repairTarget?.let(onRepair)
            }
            addView(
                ImageView(context).apply {
                    setImageResource(R.drawable.ic_verbally_warning)
                    setColorFilter(context.getColor(android.R.color.holo_orange_dark))
                    scaleType = ImageView.ScaleType.CENTER
                },
                FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER),
            )
        }

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
        val previous = session.state
        session.onReadyBubbleTapped()
        customContentDescription = null
        updateAttachedRoot(previous, session.state)
        onStart()
    }

    private fun handleCancelTap() {
        if (session.state != OverlayUiState.RECORDING) return
        val previous = session.state
        onCancel()
        session.onCancelTapped()
        waveformLevel = 0f
        customContentDescription = null
        updateAttachedRoot(previous, session.state)
    }

    private fun handleConfirmTap() {
        if (session.state != OverlayUiState.RECORDING) return
        val previous = session.state
        session.onConfirmTapped()
        customContentDescription = null
        updateAttachedRoot(previous, session.state)
        onConfirm()
    }

    private fun attachFreshRoot() {
        val view = buildRootView()
        val params = layoutParams(view)
        currentLayoutParams = params
        windowManager.addView(view, params)
        rootView = view
    }

    private fun refreshAttachedRoot() {
        val existing = rootView ?: return
        renderState(existing)
    }

    private fun updateAttachedRoot(previous: OverlayUiState, next: OverlayUiState) {
        when (OverlayRootTransitionPolicy.mode(previous, next)) {
            OverlayRootUpdateMode.REBUILD_WINDOW -> replaceAttachedRoot()
            OverlayRootUpdateMode.REFRESH_IN_PLACE -> refreshAttachedRoot()
        }
    }

    private fun replaceAttachedRoot() {
        val existing = rootView ?: return
        runCatching { windowManager.removeViewImmediate(existing) }
        rootView = null
        currentLayoutParams = null
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

    private fun layoutParams(view: View): WindowManager.LayoutParams {
        val width = view.resolvedWidth()
        val height = view.resolvedHeight()
        return WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            val position = positionMemory.currentPosition(
                screenWidth = screenWidth(),
                screenHeight = screenHeight(),
                bubbleWidth = width,
                bubbleHeight = height,
                edgeMargin = edgeMargin,
            )
            gravity = position.gravity
            x = position.x
            y = position.y
            windowAnimations = 0
        }
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

    private fun updatePosition(view: View, x: Int, y: Int) {
        val params = currentLayoutParams ?: return
        params.gravity = Gravity.TOP or Gravity.START
        params.x = x.coerceAtLeast(0)
        params.y = y.coerceAtLeast(0)
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun snapAndRememberPosition(view: View, releasedX: Int, releasedY: Int) {
        val width = view.resolvedWidth()
        val height = view.resolvedHeight()
        val position = positionMemory.rememberSnappedPosition(
            releasedX = releasedX,
            releasedY = releasedY,
            bubbleWidth = width,
            screenWidth = screenWidth(),
            edgeMargin = edgeMargin,
            screenHeight = screenHeight(),
            bubbleHeight = height,
        )
        applyAnchoredPosition(view, position)
        preferences.edit {
            putBoolean(KEY_HAS_POSITION, true)
            putString(KEY_EDGE, position.edge?.name)
            putInt(KEY_Y, position.y)
        }
    }

    private fun realignToRememberedEdge(view: View) {
        val width = view.resolvedWidth()
        val height = view.resolvedHeight()
        realignToRememberedEdge(view, width, height)
    }

    private fun realignToRememberedEdge(view: View, width: Int, height: Int) {
        val params = currentLayoutParams ?: return
        val position = positionMemory.currentPosition(
            screenWidth = screenWidth(),
            screenHeight = screenHeight(),
            bubbleWidth = width,
            bubbleHeight = height,
            edgeMargin = edgeMargin,
        )
        params.width = width
        params.height = height
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

    private fun screenHeight(): Int =
        windowManager.currentWindowMetrics.bounds.height()

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
        OverlayUiState.REPAIR -> repairMessage ?: context.getString(R.string.overlay_repair_required)
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


    companion object {
        private const val POSITION_PREFS = "floating_dictation_overlay_position"
        private const val KEY_HAS_POSITION = "has_position"
        private const val KEY_EDGE = "edge"
        private const val KEY_Y = "y"
    }
}
