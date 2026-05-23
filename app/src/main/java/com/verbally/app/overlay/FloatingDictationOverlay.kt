package com.verbally.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.widget.Button
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
    private var button: Button? = null
    private var currentLayoutParams: WindowManager.LayoutParams? = null
    private var state = OverlayState.IDLE

    val isShown: Boolean
        get() = button != null

    fun show() {
        if (!Settings.canDrawOverlays(context) || button != null) return
        val view = Button(context).apply {
            minWidth = 56
            minHeight = 56
            text = labelFor(state)
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
        view.hideWithInputMethodAnimation()
        val params = layoutParams()
        currentLayoutParams = params
        windowManager.addView(view, params)
        button = view
    }

    fun hide() {
        button?.let { runCatching { windowManager.removeViewImmediate(it) } }
        button = null
        currentLayoutParams = null
        state = OverlayState.IDLE
    }

    fun setState(next: OverlayState) {
        state = next
        button?.text = labelFor(next)
    }

    fun showMessage(message: String) {
        button?.text = message.take(10)
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

    private fun Button.hideWithInputMethodAnimation() {
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
                    if (button === this@hideWithInputMethodAnimation &&
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

    private fun layoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        val position = positionMemory.currentPosition()
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

    private fun rememberPosition(x: Int, y: Int) {
        positionMemory.rememberMovedPosition(x = x, y = y)
        preferences.edit()
            .putBoolean(KEY_HAS_POSITION, true)
            .putInt(KEY_X, x)
            .putInt(KEY_Y, y)
            .apply()
    }

    private fun loadSavedPosition(): OverlayPosition? {
        if (!preferences.getBoolean(KEY_HAS_POSITION, false)) return null
        return OverlayPosition(
            gravity = Gravity.TOP or Gravity.START,
            x = preferences.getInt(KEY_X, 24),
            y = preferences.getInt(KEY_Y, 0),
        )
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var downRawX = 0f
        private var downRawY = 0f
        private var startX = 0
        private var startY = 0
        private var dragging = false
        private var longPressHandled = false
        private val longPressRunnable = Runnable {
            longPressHandled = button?.performLongClick() == true
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
                            rememberPosition(x = params.x, y = params.y)
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

    private fun labelFor(state: OverlayState): String = when (state) {
        OverlayState.IDLE -> "聽寫"
        OverlayState.RECORDING -> "✓"
        OverlayState.PROCESSING -> "..."
        OverlayState.SUCCESS -> "完成"
        OverlayState.ERROR -> "重試"
    }

    companion object {
        private const val POSITION_PREFS = "floating_dictation_overlay_position"
        private const val KEY_HAS_POSITION = "has_position"
        private const val KEY_X = "x"
        private const val KEY_Y = "y"
    }
}
