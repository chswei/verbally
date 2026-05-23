package com.verbally.app.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.verbally.app.R
import kotlin.math.abs
import kotlin.math.roundToInt

enum class OverlayState {
    IDLE,
    RECORDING,
    PROCESSING,
    SUCCESS,
    ERROR,
}

class FloatingDictationOverlay(
    private val context: Context,
    private val onStart: () -> Unit,
    private val onCancel: () -> Unit,
    private val onConfirm: () -> Unit,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val preferences = context.getSharedPreferences(POSITION_PREFS, Context.MODE_PRIVATE)
    private val positionMemory = OverlayPositionMemory(loadSavedPosition())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val edgeMargin = OverlayVisualDefaults.EDGE_MARGIN_DP.toPx()
    private val bubbleSize = OverlayVisualDefaults.BUBBLE_SIZE_DP.toPx()
    private val bubbleCornerRadius = OverlayVisualDefaults.BUBBLE_CORNER_RADIUS_DP.toPx().toFloat()
    private val iconSize = OverlayVisualDefaults.ICON_SIZE_DP.toPx()
    private var bubble: FrameLayout? = null
    private var iconView: ImageView? = null
    private var currentLayoutParams: WindowManager.LayoutParams? = null
    private var state = OverlayState.IDLE

    val isShown: Boolean
        get() = bubble != null

    fun show() {
        if (!Settings.canDrawOverlays(context) || bubble != null) return
        val view = FrameLayout(context).apply {
            minimumWidth = bubbleSize
            minimumHeight = bubbleSize
            background = bubbleBackground()
            elevation = 8f
            isClickable = true
            contentDescription = contentDescriptionFor(state)
            setOnClickListener { handleClick() }
            setOnLongClickListener {
                if (state == OverlayState.RECORDING) {
                    onCancel()
                    setState(OverlayState.IDLE)
                    true
                } else {
                    false
                }
            }
            setOnTouchListener(DragTouchListener())
        }
        iconView = ImageView(context).apply {
            setImageResource(iconFor(state))
            scaleType = ImageView.ScaleType.CENTER
        }
        view.addView(
            iconView,
            FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER),
        )
        view.hideWithInputMethodAnimation()
        val params = layoutParams(view)
        currentLayoutParams = params
        windowManager.addView(view, params)
        bubble = view
    }

    fun hide() {
        bubble?.let { runCatching { windowManager.removeViewImmediate(it) } }
        bubble = null
        iconView = null
        currentLayoutParams = null
        state = OverlayState.IDLE
    }

    fun setState(next: OverlayState) {
        state = next
        bubble?.contentDescription = contentDescriptionFor(next)
        iconView?.setImageResource(iconFor(next))
    }

    fun showMessage(message: String) {
        bubble?.contentDescription = message
    }

    private fun handleClick() {
        when (state) {
            OverlayState.IDLE, OverlayState.SUCCESS, OverlayState.ERROR -> {
                onStart()
                setState(OverlayState.RECORDING)
            }
            OverlayState.RECORDING -> {
                setState(OverlayState.PROCESSING)
                onConfirm()
            }
            OverlayState.PROCESSING -> Unit
        }
    }

    private fun View.hideWithInputMethodAnimation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        setWindowInsetsAnimationCallback(
            object : WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                override fun onProgress(
                    insets: WindowInsets,
                    runningAnimations: MutableList<WindowInsetsAnimation>,
                ): WindowInsets {
                    val inputMethodAnimating = runningAnimations.any {
                        it.typeMask and WindowInsets.Type.ime() != 0
                    }
                    if (bubble === this@hideWithInputMethodAnimation &&
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

    private fun bubbleBackground() = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = bubbleCornerRadius
        setColor(Color.parseColor("#14233A"))
    }

    private fun layoutParams(view: View) = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
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

    private fun snapAndRememberPosition(view: View, releasedX: Int, releasedY: Int) {
        val position = positionMemory.rememberSnappedPosition(
            releasedX = releasedX,
            releasedY = releasedY,
            bubbleWidth = view.resolvedWidth(),
            screenWidth = screenWidth(),
            edgeMargin = edgeMargin,
        )
        updatePosition(view = view, x = position.x, y = position.y)
        preferences.edit()
            .putBoolean(KEY_HAS_POSITION, true)
            .putString(KEY_EDGE, position.edge?.name)
            .putInt(KEY_Y, position.y)
            .apply()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            @Suppress("DEPRECATION")
            context.resources.displayMetrics.widthPixels
        }

    private fun View.resolvedWidth(): Int {
        if (width > 0) return width
        measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        return measuredWidth.coerceAtLeast(minimumWidth)
    }

    private fun Int.toPx(): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            context.resources.displayMetrics,
        ).roundToInt()

    private inner class DragTouchListener : View.OnTouchListener {
        private var downRawX = 0f
        private var downRawY = 0f
        private var startX = 0
        private var startY = 0
        private var dragging = false
        private var longPressHandled = false
        private val longPressRunnable = Runnable {
            longPressHandled = bubble?.performLongClick() == true
        }

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    val location = IntArray(2)
                    view.getLocationOnScreen(location)
                    startX = location[0]
                    startY = location[1]
                    dragging = false
                    longPressHandled = false
                    view.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downRawX
                    val deltaY = event.rawY - downRawY
                    if (!dragging && (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop)) {
                        dragging = true
                        view.removeCallbacks(longPressRunnable)
                    }
                    if (dragging) {
                        updatePosition(
                            view = view,
                            x = (startX + deltaX).roundToInt(),
                            y = (startY + deltaY).roundToInt(),
                        )
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    view.removeCallbacks(longPressRunnable)
                    if (dragging) {
                        val params = currentLayoutParams
                        if (params != null) {
                            snapAndRememberPosition(view = view, releasedX = params.x, releasedY = params.y)
                        }
                    } else if (!longPressHandled) {
                        view.performClick()
                    }
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.removeCallbacks(longPressRunnable)
                    return true
                }
            }
            return true
        }
    }

    private fun iconFor(state: OverlayState): Int = when (state) {
        OverlayState.IDLE -> R.drawable.ic_verbally_waveform
        OverlayState.RECORDING -> R.drawable.ic_verbally_check
        OverlayState.PROCESSING -> R.drawable.ic_verbally_dots
        OverlayState.SUCCESS -> R.drawable.ic_verbally_check
        OverlayState.ERROR -> R.drawable.ic_verbally_retry
    }

    private fun contentDescriptionFor(state: OverlayState): String = when (state) {
        OverlayState.IDLE -> "開始聽寫"
        OverlayState.RECORDING -> "確認聽寫"
        OverlayState.PROCESSING -> "處理中"
        OverlayState.SUCCESS -> "聽寫完成"
        OverlayState.ERROR -> "重試聽寫"
    }

    companion object {
        private const val POSITION_PREFS = "floating_dictation_overlay_position"
        private const val KEY_HAS_POSITION = "has_position"
        private const val KEY_EDGE = "edge"
        private const val KEY_Y = "y"
    }
}
