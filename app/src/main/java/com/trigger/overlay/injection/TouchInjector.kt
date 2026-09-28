package com.trigger.overlay.injection

import android.content.Context
import android.graphics.PointF
import android.os.SystemClock
import android.util.Log

/**
 * Unified injector interface - abstracts Shizuku vs Accessibility vs Fallback
 */
interface TouchInjector {
    suspend fun tap(x: Float, y: Float, displayId: Int = 0): Boolean
    suspend fun down(x: Float, y: Float, displayId: Int = 0, downTime: Long = SystemClock.uptimeMillis()): Boolean
    suspend fun move(x: Float, y: Float, displayId: Int = 0, downTime: Long): Boolean
    suspend fun up(x: Float, y: Float, displayId: Int = 0, downTime: Long): Boolean
    fun isAvailable(): Boolean
    fun getName(): String
}

/**
 * Manager that picks best available injector
 */
class InjectorManager(private val context: Context) {

    private var shizukuInjector: ShizukuInjector? = null
    private var accessibilityInjector: AccessibilityInjector? = null

    fun getBestInjector(): TouchInjector {
        shizukuInjector?.let {
            if (it.isAvailable()) return it
        }
        accessibilityInjector?.let {
            if (it.isAvailable()) return it
        }
        // Return shizuku as default (will report unavailable)
        return shizukuInjector ?: ShizukuInjector(context).also { shizukuInjector = it }
    }

    fun setAccessibilityInjector(injector: AccessibilityInjector) {
        accessibilityInjector = injector
    }

    fun getShizukuInjector(): ShizukuInjector {
        if (shizukuInjector == null) shizukuInjector = ShizukuInjector(context)
        return shizukuInjector!!
    }

    /**
     * Helper: tap using percentage coordinates (recommended for games)
     */
    suspend fun tapPercent(xPercent: Float, yPercent: Float): Boolean {
        val point = CoordinateConverter.percentToPhysical(context, xPercent, yPercent)
        val displayId = CoordinateConverter.getDisplayId(context)
        return getBestInjector().tap(point.x, point.y, displayId)
    }

    suspend fun tapPoint(point: PointF): Boolean {
        val displayId = CoordinateConverter.getDisplayId(context)
        return getBestInjector().tap(point.x, point.y, displayId)
    }
}
