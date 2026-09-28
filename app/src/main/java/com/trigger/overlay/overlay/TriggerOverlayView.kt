package com.trigger.overlay.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import kotlin.math.abs

/**
 * iOS AssistiveTouch-inspired floating trigger
 *
 * Design:
 *  - Frosted glass outer circle (60% white blur)
 *  - Inner dot with iOS blue, with subtle shadow
 *  - Spring animation on tap (scale)
 *  - Draggable with momentum
 *  - Long press for rapid fire
 */
class TriggerOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    interface OnTriggerListener {
        fun onTap()
        fun onHoldStart()
        fun onHoldEnd()
        fun onPositionChanged(x: Int, y: Int)
    }

    private var listener: OnTriggerListener? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var layoutParams: WindowManager.LayoutParams? = null

    private val outerView: View
    private val innerView: View
    private val container: FrameLayout

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var isHolding = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downTime = 0L
    private val longPressTimeout = 300L

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    init {
        // Container with iOS blur effect simulation
        container = FrameLayout(context).apply {
            layoutParams = LayoutParams(140, 140).apply {
                gravity = Gravity.CENTER
            }
        }

        outerView = View(context).apply {
            layoutParams = LayoutParams(120, 120).apply { gravity = Gravity.CENTER }
            background = createOuterDrawable()
            // Shadow for iOS depth
            elevation = 12f
        }

        innerView = View(context).apply {
            layoutParams = LayoutParams(56, 56).apply { gravity = Gravity.CENTER }
            background = createInnerDrawable()
            elevation = 6f
        }

        container.addView(outerView)
        container.addView(innerView)
        addView(container)

        // Make touch area larger than visual
        setPadding(20, 20, 20, 20)
        isClickable = true
        isFocusable = false
    }

    private fun createOuterDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            // iOS frosted glass: semi-transparent white with blur border
            setColor(Color.argb(180, 255, 255, 255))
            setStroke(1, Color.argb(60, 0, 0, 0))
        }
    }

    private fun createInnerDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            // iOS blue dot
            setColor(Color.parseColor("#007AFF"))
            // Inner glow
        }
    }

    fun setSize(dp: Int) {
        val px = (dp * resources.displayMetrics.density).toInt()
        val lp = container.layoutParams
        lp.width = px
        lp.height = px
        container.layoutParams = lp

        val outerSize = (px * 0.85f).toInt()
        outerView.layoutParams = LayoutParams(outerSize, outerSize).apply { gravity = Gravity.CENTER }

        val innerSize = (px * 0.4f).toInt()
        innerView.layoutParams = LayoutParams(innerSize, innerSize).apply { gravity = Gravity.CENTER }
    }

    fun setOnTriggerListener(l: OnTriggerListener) {
        listener = l
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            layoutParams = parent.let { null } // Will be set via windowManager
            // Retrieve actual params from windowManager is not directly possible, we store via tag
        } catch (_: Exception) {}
    }

    private var checkLongPress: Runnable? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = (layoutParams?.x ?: 0).also { if (layoutParams == null) { retrieveLayoutParams() } }
                initialY = layoutParams?.y ?: 0
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                downTime = System.currentTimeMillis()
                isDragging = false
                isHolding = false

                animatePressDown()

                checkLongPress = Runnable {
                    if (!isDragging) {
                        isHolding = true
                        haptic(HapticType.MEDIUM)
                        listener?.onHoldStart()
                        animateHold()
                    }
                }
                postDelayed(checkLongPress, longPressTimeout)

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY

                if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                    isDragging = true
                    removeCallbacks(checkLongPress)
                    haptic(HapticType.LIGHT)
                }

                if (isDragging) {
                    try {
                        val params = getWindowLayoutParams()
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager.updateViewLayout(this, params)
                        layoutParams = params
                    } catch (e: Exception) {
                        // fallback
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(checkLongPress)

                if (isHolding) {
                    listener?.onHoldEnd()
                    animatePressUp()
                    isHolding = false
                } else if (!isDragging) {
                    // Tap
                    haptic(HapticType.LIGHT)
                    animateTap {
                        listener?.onTap()
                    }
                } else {
                    // Drag ended
                    animatePressUp()
                    try {
                        val params = getWindowLayoutParams()
                        listener?.onPositionChanged(params.x, params.y)
                        // Snap to edge like iOS AssistiveTouch
                        snapToEdge(params)
                    } catch (_: Exception) {}
                }

                isDragging = false
                return true
            }
        }
        return false
    }

    private fun retrieveLayoutParams() {
        try {
            // Try to get layoutParams from view's layoutParams if it's WindowManager.LayoutParams
            val lp = layoutParams
            if (lp == null) {
                // Search via reflection in parent
                val field = View::class.java.getDeclaredField("mLayoutParams")
                field.isAccessible = true
                val params = field.get(this) as? WindowManager.LayoutParams
                layoutParams = params
            }
        } catch (_: Exception) {}
    }

    private fun getWindowLayoutParams(): WindowManager.LayoutParams {
        layoutParams?.let { return it }
        // If not cached, try to retrieve via windowManager
        return try {
            val field = View::class.java.getDeclaredField("mLayoutParams")
            field.isAccessible = true
            field.get(this) as WindowManager.LayoutParams
        } catch (e: Exception) {
            WindowManager.LayoutParams().apply {
                x = initialX
                y = initialY
            }
        }
    }

    private fun snapToEdge(params: WindowManager.LayoutParams) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val middle = screenWidth / 2
        val targetX = if (params.x < middle) 20 else screenWidth - width - 20

        ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 300
            interpolator = android.view.animation.OvershootInterpolator(0.8f)
            addUpdateListener {
                params.x = it.animatedValue as Int
                try { windowManager.updateViewLayout(this@TriggerOverlayView, params) } catch (_: Exception) {}
            }
            start()
        }
    }

    // --- iOS style animations ---

    private fun animatePressDown() {
        container.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
        outerView.animate().alpha(0.8f).setDuration(100).start()
    }

    private fun animatePressUp() {
        container.animate().scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(android.view.animation.OvershootInterpolator()).start()
        outerView.animate().alpha(1f).setDuration(200).start()
    }

    private fun animateTap(onEnd: () -> Unit) {
        container.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).withEndAction {
            container.animate().scaleX(1f).scaleY(1f).setDuration(250).setInterpolator(android.view.animation.BounceInterpolator()).start()
            innerView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(80).withEndAction {
                innerView.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                onEnd()
            }.start()
        }.start()
    }

    private fun animateHold() {
        innerView.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#FF3B30")) // red when holding
        }
        ValueAnimator.ofFloat(1f, 1.1f, 1f).apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                val v = it.animatedValue as Float
                innerView.scaleX = v
                innerView.scaleY = v
            }
            start()
        }
    }

    enum class HapticType { LIGHT, MEDIUM }

    private fun haptic(type: HapticType) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.MEDIUM -> VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (_: Exception) {}
    }
}
