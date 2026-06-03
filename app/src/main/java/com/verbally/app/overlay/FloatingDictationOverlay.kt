package com.verbally.app.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
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
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
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
    private val motionFrameSize = OverlayVisualDefaults.MOTION_FRAME_SIZE_DP.toPx()
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
    private val activeFrameWidth = OverlayVisualDefaults.ACTIVE_FRAME_WIDTH_DP.toPx()
    private val rootTouchListener = DragTouchListener()
    private val session = OverlaySessionStateMachine()
    private var waveformLevel = 0f
    private var rootView: FrameLayout? = null
    private var currentLayoutParams: WindowManager.LayoutParams? = null
    private var customContentDescription: String? = null
    private var repairTarget: RuntimeRepairTarget? = null
    private var repairMessage: String? = null
    private var recordingWaveformView: RecordingWaveformView? = null
    private var overlayVisible = false
    private var layoutAnimator: ValueAnimator? = null
    private var renderedWindowWidth = 0
    private var renderedWindowHeight = 0
    private var renderedContentGravity = Gravity.END
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
        get() = overlayVisible

    val currentState: OverlayUiState
        get() = session.state

    fun show() {
        if (!Settings.canDrawOverlays(context)) {
            removeAttachedRoot()
            return
        }
        if (rootView == null) {
            attachFreshRoot()
        } else {
            setOverlayVisible(true, animate = true)
        }
    }

    fun hide() {
        val existing = rootView ?: run {
            overlayVisible = false
            return
        }
        setOverlayVisible(false, animate = true)
        recordingWaveformView = null
        waveformLevel = 0f
        customContentDescription = null
        repairTarget = null
        repairMessage = null
        val previous = session.state
        session.forceState(OverlayUiState.READY)
        if (previous != OverlayUiState.READY) {
            refreshAttachedRoot(previousState = previous)
        } else {
            existing.contentDescription = contentDescriptionFor(OverlayUiState.READY)
        }
    }

    fun dispose() {
        removeAttachedRoot()
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
            setOverlayVisible(true, animate = true)
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

    private fun renderState(root: FrameLayout, previousState: OverlayUiState? = null) {
        recordingWaveformView = null
        val renderedState = session.state
        val content = when (session.state) {
            OverlayUiState.READY -> createReadyBubble()
            OverlayUiState.RECORDING -> createRecordingControls()
            OverlayUiState.PROCESSING -> createProcessingControls()
            OverlayUiState.REPAIR -> createRepairBubble()
        }
        val contentWidth = content.resolvedWidth()
        val contentHeight = content.resolvedHeight()
        val params = currentLayoutParams
        val currentGravity = params?.gravity ?: positionMemory.currentPosition(
            screenWidth = screenWidth(),
            screenHeight = screenHeight(),
            bubbleWidth = contentWidth,
            bubbleHeight = contentHeight,
            edgeMargin = edgeMargin,
        ).gravity
        val width = windowWidthForState(
            state = renderedState,
            currentGravity = currentGravity,
            contentWidth = contentWidth,
        )
        val height = contentHeight
        renderedWindowWidth = width
        renderedWindowHeight = height
        val deferContentSwap = rootView === root &&
            params != null &&
            OverlayContentSwapPolicy.deferUntilAfterLayout(
                currentGravity = params.gravity,
                currentWidth = params.width,
                currentHeight = params.height,
                nextWidth = width,
                nextHeight = height,
            )
        root.contentDescription = customContentDescription ?: contentDescriptionFor(session.state)
        val contentGravity = contentGravityForFrame(currentGravity)
        renderedContentGravity = contentGravity
        val animateAnchoredIntro = params != null &&
            OverlayContentSwapPolicy.animateAnchoredIntro(
                previousState = previousState,
                nextState = renderedState,
                currentGravity = params.gravity,
            )
        if (deferContentSwap) {
            animateToRememberedEdge(root, width, height)
            root.post {
                if (rootView === root && session.state == renderedState) {
                    root.removeAllViews()
                    addStateContent(root, content, width, height, contentWidth, contentHeight, contentGravity)
                    if (animateAnchoredIntro) {
                        animateActiveContentFromBubbleAnchor(content, contentGravity)
                    }
                }
            }
        } else {
            root.removeAllViews()
            addStateContent(root, content, width, height, contentWidth, contentHeight, contentGravity)
            if (animateAnchoredIntro) {
                animateActiveContentFromBubbleAnchor(content, contentGravity)
            }
            if (rootView === root) {
                animateToRememberedEdge(root, width, height)
            }
        }
    }

    private fun windowWidthForState(
        state: OverlayUiState,
        currentGravity: Int,
        contentWidth: Int,
    ): Int =
        if (OverlayContentSwapPolicy.useStableEdgeFrame(state, currentGravity)) {
            activeFrameWidth.coerceAtLeast(contentWidth)
        } else {
            contentWidth
        }

    private fun contentGravityForFrame(currentGravity: Int): Int =
        if (OverlayContentSwapPolicy.alignContentToStart(currentGravity)) {
            Gravity.START or Gravity.CENTER_VERTICAL
        } else {
            Gravity.END or Gravity.CENTER_VERTICAL
        }

    private fun addStateContent(
        root: FrameLayout,
        content: View,
        width: Int,
        height: Int,
        contentWidth: Int,
        contentHeight: Int,
        contentGravity: Int,
    ) {
        root.minimumWidth = width
        root.minimumHeight = height
        root.addView(
            content,
            FrameLayout.LayoutParams(contentWidth, contentHeight, contentGravity),
        )
    }

    private fun animateActiveContentFromBubbleAnchor(content: View, contentGravity: Int) {
        val motionFrame = content as? FrameLayout ?: return
        motionFrame.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (motionFrame.viewTreeObserver.isAlive) {
                        motionFrame.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    val row = motionFrame.getChildAt(0) as? LinearLayout ?: return true
                    prepareActiveContentAtBubbleAnchor(row, contentGravity)
                    motionFrame.post { animateActiveContentIntoPlace(row) }
                    return true
                }
            },
        )
    }

    private fun prepareActiveContentAtBubbleAnchor(row: LinearLayout, contentGravity: Int) {
        if (row.childCount < ACTIVE_CONTROL_CHILD_COUNT) return
        val leading = row.getChildAt(ACTIVE_CONTROL_LEADING_INDEX)
        val center = row.getChildAt(ACTIVE_CONTROL_CENTER_INDEX)
        val trailing = row.getChildAt(ACTIVE_CONTROL_TRAILING_INDEX)
        val revealFromLeading = contentGravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == Gravity.START
        if (revealFromLeading) {
            val leadingCenter = leading.left + leading.width / 2f
            val trailingStartOffset = leadingCenter - (trailing.left + trailing.width / 2f)
            val centerStartOffset = leading.right - center.left.toFloat()
            trailing.alpha = 0f
            trailing.translationX = trailingStartOffset
            center.translationX = centerStartOffset
            center.pivotX = 0f
        } else {
            val trailingCenter = trailing.left + trailing.width / 2f
            val leadingStartOffset = trailingCenter - (leading.left + leading.width / 2f)
            val centerStartOffset = trailing.left - center.right.toFloat()
            leading.alpha = 0f
            leading.translationX = leadingStartOffset
            center.translationX = centerStartOffset
            center.pivotX = center.width.toFloat()
        }
        center.alpha = 0f
        center.scaleX = ACTIVE_CENTER_REVEAL_START_SCALE
    }

    private fun animateActiveContentIntoPlace(row: LinearLayout) {
        if (row.childCount < ACTIVE_CONTROL_CHILD_COUNT) return
        val leading = row.getChildAt(ACTIVE_CONTROL_LEADING_INDEX)
        val center = row.getChildAt(ACTIVE_CONTROL_CENTER_INDEX)
        val trailing = row.getChildAt(ACTIVE_CONTROL_TRAILING_INDEX)
        val interpolator = DecelerateInterpolator()

        leading.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(OverlayVisualDefaults.ACTIVE_REVEAL_ANIMATION_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
        trailing.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(OverlayVisualDefaults.ACTIVE_REVEAL_ANIMATION_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
        center.animate()
            .alpha(1f)
            .translationX(0f)
            .scaleX(1f)
            .setDuration(OverlayVisualDefaults.ACTIVE_REVEAL_ANIMATION_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
    }

    private fun createReadyBubble(): View =
        createMotionFrame(width = motionFrameSize, height = motionFrameSize) {
            FrameLayout(context).apply {
                minimumWidth = bubbleSize
                minimumHeight = bubbleSize
                background = roundedBackground(
                    color = color(OverlayColorDefaults.READY_BUBBLE_BACKGROUND_RES),
                    cornerRadius = bubbleCornerRadius,
                )
                elevation = 12f
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
        }

    private fun createRecordingControls(): View =
        createMotionFrame(width = activeFrameWidth, height = motionFrameSize) {
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
        }

    private fun createProcessingControls(): View =
        createMotionFrame(width = activeFrameWidth, height = motionFrameSize) {
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
        }

    private fun createRepairBubble(): View =
        createMotionFrame(width = motionFrameSize, height = motionFrameSize) {
            FrameLayout(context).apply {
                minimumWidth = bubbleSize
                minimumHeight = bubbleSize
                background = roundedBackground(
                    color = context.getColor(android.R.color.white),
                    cornerRadius = bubbleCornerRadius,
                )
                elevation = 12f
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
        }

    private fun createMotionFrame(width: Int, height: Int, childFactory: () -> View): View =
        FrameLayout(context).apply {
            minimumWidth = width
            minimumHeight = height
            clipChildren = false
            clipToPadding = false
            addView(
                childFactory(),
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ),
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
            elevation = 7f
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
        setOverlayVisible(true, animate = false)
    }

    private fun refreshAttachedRoot(previousState: OverlayUiState? = null) {
        val existing = rootView ?: return
        renderState(existing, previousState)
    }

    private fun updateAttachedRoot(previous: OverlayUiState, next: OverlayUiState) {
        when (OverlayRootTransitionPolicy.mode(previous, next)) {
            OverlayRootUpdateMode.REFRESH_IN_PLACE -> refreshAttachedRoot(previousState = previous)
        }
    }

    private fun removeAttachedRoot() {
        val existing = rootView ?: return
        layoutAnimator?.cancel()
        layoutAnimator = null
        existing.animate().cancel()
        runCatching { windowManager.removeViewImmediate(existing) }
        rootView = null
        currentLayoutParams = null
        overlayVisible = false
        recordingWaveformView = null
        waveformLevel = 0f
        customContentDescription = null
        repairTarget = null
        repairMessage = null
        session.forceState(OverlayUiState.READY)
    }

    private fun buildRootView(): FrameLayout =
        FrameLayout(context).apply {
            clipChildren = true
            clipToPadding = true
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
        val width = renderedWindowWidth.takeIf { it > 0 } ?: view.resolvedWidth()
        val height = renderedWindowHeight.takeIf { it > 0 } ?: view.resolvedHeight()
        val baseFlags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        return WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            OverlayWindowVisibility.flagsFor(baseFlags, visible = true),
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

    private fun setOverlayVisible(visible: Boolean, animate: Boolean) {
        val root = rootView ?: run {
            overlayVisible = false
            return
        }
        overlayVisible = visible
        val params = currentLayoutParams
        if (params != null) {
            val nextFlags = OverlayWindowVisibility.flagsFor(params.flags, visible)
            if (params.flags != nextFlags) {
                params.flags = nextFlags
                runCatching { windowManager.updateViewLayout(root, params) }
            }
        }
        root.animate().cancel()
        val targetAlpha = OverlayWindowVisibility.alphaFor(visible)
        if (animate) {
            root.animate()
                .alpha(targetAlpha)
                .setDuration(
                    if (visible) 120L else 80L,
                )
                .start()
        } else {
            root.alpha = targetAlpha
        }
    }

    private fun applyAnchoredPosition(view: View, position: OverlayPosition) {
        layoutAnimator?.cancel()
        layoutAnimator = null
        val params = currentLayoutParams ?: return
        val width = windowWidthForState(
            state = session.state,
            currentGravity = position.gravity,
            contentWidth = naturalContentWidthForState(session.state),
        )
        val height = naturalContentHeightForState(session.state)
        renderedWindowWidth = width
        renderedWindowHeight = height
        params.width = width
        params.height = height
        params.gravity = position.gravity
        params.x = position.x
        params.y = position.y
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun updatePosition(view: View, x: Int, y: Int) {
        layoutAnimator?.cancel()
        layoutAnimator = null
        val params = currentLayoutParams ?: return
        val windowWidth = params.width.takeIf { it > 0 } ?: view.resolvedWidth()
        val visibleWidth = naturalContentWidthForState(session.state)
        params.gravity = Gravity.TOP or Gravity.START
        params.x = OverlayDragGeometry.boundedWindowXForVisibleControl(
            proposedWindowX = x,
            windowWidth = windowWidth,
            visibleWidth = visibleWidth,
            screenWidth = screenWidth(),
            visibleOffset = visibleContentOffset(windowWidth, visibleWidth),
        )
        params.y = y.coerceAtLeast(0)
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun snapAndRememberPosition(view: View, releasedX: Int, releasedY: Int) {
        val windowWidth = currentLayoutParams?.width?.takeIf { it > 0 } ?: view.resolvedWidth()
        val windowHeight = currentLayoutParams?.height?.takeIf { it > 0 } ?: view.resolvedHeight()
        val snapWidth = naturalContentWidthForState(session.state)
        val snapX = OverlayDragGeometry.visibleReleasedX(
            releasedWindowX = releasedX,
            windowWidth = windowWidth,
            visibleWidth = snapWidth,
            visibleOffset = visibleContentOffset(windowWidth, snapWidth),
        )
        val position = positionMemory.rememberSnappedPosition(
            releasedX = snapX,
            releasedY = releasedY,
            bubbleWidth = snapWidth,
            screenWidth = screenWidth(),
            edgeMargin = edgeMargin,
            screenHeight = screenHeight(),
            bubbleHeight = naturalContentHeightForState(session.state).coerceAtMost(windowHeight),
        )
        val previousContentGravity = renderedContentGravity
        applyAnchoredPosition(view, position)
        val nextContentGravity = contentGravityForFrame(position.gravity)
        if (view is FrameLayout && previousContentGravity != nextContentGravity) {
            renderState(view, previousState = session.state)
        }
        preferences.edit {
            putBoolean(KEY_HAS_POSITION, true)
            putString(KEY_EDGE, position.edge?.name)
            putInt(KEY_Y, position.y)
        }
    }

    private fun visibleContentOffset(windowWidth: Int, visibleWidth: Int): Int {
        val contentAlignedStart =
            renderedContentGravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == Gravity.START
        return if (contentAlignedStart) {
            0
        } else {
            (windowWidth - visibleWidth).coerceAtLeast(0)
        }
    }

    private fun realignToRememberedEdge(view: View) {
        val gravity = positionMemory.currentPosition().gravity
        val width = windowWidthForState(
            state = session.state,
            currentGravity = gravity,
            contentWidth = naturalContentWidthForState(session.state),
        )
        val height = naturalContentHeightForState(session.state)
        realignToRememberedEdge(view, width, height)
    }

    private fun realignToRememberedEdge(view: View, width: Int, height: Int) {
        layoutAnimator?.cancel()
        layoutAnimator = null
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

    private fun animateToRememberedEdge(view: View, width: Int, height: Int) {
        val position = positionMemory.currentPosition(
            screenWidth = screenWidth(),
            screenHeight = screenHeight(),
            bubbleWidth = width,
            bubbleHeight = height,
            edgeMargin = edgeMargin,
        )
        animateLayout(view, position, width, height)
    }

    private fun animateLayout(
        view: View,
        position: OverlayPosition,
        width: Int,
        height: Int,
    ) {
        val params = currentLayoutParams ?: return
        val startWidth = params.width.takeIf { it > 0 } ?: width
        val startHeight = params.height.takeIf { it > 0 } ?: height
        val startX = params.x
        val startY = params.y
        if (
            startWidth == width &&
            startHeight == height &&
            startX == position.x &&
            startY == position.y &&
            params.gravity == position.gravity
        ) {
            params.width = width
            params.height = height
            params.gravity = position.gravity
            params.x = position.x
            params.y = position.y
            runCatching { windowManager.updateViewLayout(view, params) }
            return
        }
        layoutAnimator?.cancel()
        if (OverlayVisualDefaults.MOTION_ANIMATION_DURATION_MS <= 0L) {
            params.width = width
            params.height = height
            params.gravity = position.gravity
            params.x = position.x
            params.y = position.y
            runCatching { windowManager.updateViewLayout(view, params) }
            return
        }
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = OverlayVisualDefaults.MOTION_ANIMATION_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val fraction = it.animatedValue as Float
                params.width = lerp(startWidth, width, fraction)
                params.height = lerp(startHeight, height, fraction)
                params.gravity = position.gravity
                params.x = lerp(startX, position.x, fraction)
                params.y = lerp(startY, position.y, fraction)
                runCatching { windowManager.updateViewLayout(view, params) }
            }
            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (layoutAnimator === animation) {
                            layoutAnimator = null
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        if (layoutAnimator === animation) {
                            layoutAnimator = null
                        }
                    }
                },
            )
        }
        layoutAnimator = animator
        animator.start()
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

    private fun lerp(start: Int, end: Int, fraction: Float): Int =
        (start + (end - start) * fraction).roundToInt()

    private fun contentDescriptionFor(state: OverlayUiState): String = when (state) {
        OverlayUiState.READY -> context.getString(R.string.overlay_start_dictation)
        OverlayUiState.RECORDING -> context.getString(R.string.overlay_recording)
        OverlayUiState.PROCESSING -> context.getString(R.string.overlay_processing)
        OverlayUiState.REPAIR -> repairMessage ?: context.getString(R.string.overlay_repair_required)
    }

    private fun naturalContentWidthForState(state: OverlayUiState): Int = when (state) {
        OverlayUiState.READY,
        OverlayUiState.REPAIR,
        -> motionFrameSize
        OverlayUiState.RECORDING,
        OverlayUiState.PROCESSING,
        -> activeFrameWidth
    }

    private fun naturalContentHeightForState(state: OverlayUiState): Int = motionFrameSize

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
        private const val ACTIVE_CONTROL_CHILD_COUNT = 3
        private const val ACTIVE_CONTROL_LEADING_INDEX = 0
        private const val ACTIVE_CONTROL_CENTER_INDEX = 1
        private const val ACTIVE_CONTROL_TRAILING_INDEX = 2
        private const val ACTIVE_CENTER_REVEAL_START_SCALE = 0.18f
    }
}
