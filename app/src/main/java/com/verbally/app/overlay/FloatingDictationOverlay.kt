package com.verbally.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button

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
    private var button: Button? = null
    private var state = OverlayState.IDLE

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
        }
        windowManager.addView(view, layoutParams())
        button = view
    }

    fun hide() {
        button?.let { runCatching { windowManager.removeView(it) } }
        button = null
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

    private fun layoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.END or Gravity.CENTER_VERTICAL
        x = 24
        y = 0
    }

    private fun labelFor(state: OverlayState): String = when (state) {
        OverlayState.IDLE -> "聽寫"
        OverlayState.RECORDING -> "✓"
        OverlayState.PROCESSING -> "..."
        OverlayState.SUCCESS -> "完成"
        OverlayState.ERROR -> "重試"
    }
}
