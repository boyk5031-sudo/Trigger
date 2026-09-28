package com.trigger.overlay.injection

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent

/**
 * Factory that builds valid MotionEvents for injection.
 *
 * Common failures fixed:
 *  - Method 4: Broken MotionEvent lifecycle -> we enforce DOWN -> (MOVE*) -> UP with consistent downTime
 *  - Missing source / displayId -> set to SOURCE_TOUCHSCREEN and default display
 *  - Invalid timestamps -> use uptimeMillis()
 *  - Pointer index errors -> use single pointer id 0 for tap
 */
object InputEventFactory {

    private const val DEFAULT_DISPLAY_ID = 0

    fun createTapDown(x: Float, y: Float, downTime: Long = SystemClock.uptimeMillis()): MotionEvent {
        val eventTime = downTime
        return obtainMotionEvent(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y)
    }

    fun createTapUp(x: Float, y: Float, downTime: Long, upTime: Long = SystemClock.uptimeMillis()): MotionEvent {
        return obtainMotionEvent(downTime, upTime, MotionEvent.ACTION_UP, x, y)
    }

    fun createMove(x: Float, y: Float, downTime: Long): MotionEvent {
        val eventTime = SystemClock.uptimeMillis()
        return obtainMotionEvent(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y)
    }

    private fun obtainMotionEvent(
        downTime: Long,
        eventTime: Long,
        action: Int,
        x: Float,
        y: Float,
        displayId: Int = DEFAULT_DISPLAY_ID
    ): MotionEvent {
        val props = arrayOf(
            MotionEvent.PointerProperties().apply {
                id = 0
                toolType = MotionEvent.TOOL_TYPE_FINGER
            }
        )
        val coords = arrayOf(
            MotionEvent.PointerCoords().apply {
                this.x = x
                this.y = y
                pressure = 1f
                size = 1f
            }
        )

        val event = MotionEvent.obtain(
            downTime,
            eventTime,
            action,
            1,
            props,
            coords,
            0,
            0,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_TOUCHSCREEN,
            0
        )

        // Set displayId for Android 10+ (critical for multi-display and game windows)
        try {
            // MotionEvent.setDisplayId exists from API 30, but we try reflection for lower
            val m = MotionEvent::class.java.getMethod("setDisplayId", Int::class.javaPrimitiveType)
            m.invoke(event, displayId)
        } catch (_: Exception) {
            // fallback: hidden field
            try {
                val f = MotionEvent::class.java.getDeclaredField("mDisplayId")
                f.isAccessible = true
                f.set(event, displayId)
            } catch (_: Exception) { }
        }

        return event
    }

    /**
     * Multi-pointer event builder for true multi-touch injection.
     * Used when game requires 2 fingers and trigger must not cancel existing touches.
     */
    fun createPointerEvent(
        downTime: Long,
        action: Int,
        pointers: List<Pair<Int, Pair<Float, Float>>>, // id -> (x,y)
        displayId: Int = DEFAULT_DISPLAY_ID
    ): MotionEvent {
        val eventTime = SystemClock.uptimeMillis()
        val props = pointers.map { (id, _) ->
            MotionEvent.PointerProperties().apply {
                this.id = id
                toolType = MotionEvent.TOOL_TYPE_FINGER
            }
        }.toTypedArray()
        val coords = pointers.map { (_, xy) ->
            MotionEvent.PointerCoords().apply {
                x = xy.first
                y = xy.second
                pressure = 1f
                size = 1f
            }
        }.toTypedArray()

        val event = MotionEvent.obtain(
            downTime, eventTime, action,
            pointers.size, props, coords,
            0, 0, 1f, 1f, 0, 0,
            InputDevice.SOURCE_TOUCHSCREEN, 0
        )

        try {
            MotionEvent::class.java.getMethod("setDisplayId", Int::class.javaPrimitiveType)
                .invoke(event, displayId)
        } catch (_: Exception) {}

        return event
    }
}
