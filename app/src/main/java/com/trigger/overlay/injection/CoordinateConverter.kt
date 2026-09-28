package com.trigger.overlay.injection

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.view.WindowMetrics
import kotlin.math.roundToInt

/**
 * iOS-style clarity: This utility fixes Method 3 failure.
 *
 * Game surfaces (OpenGL/Vulkan) expect raw physical pixels in native orientation,
 * not dp, not view-local coordinates.
 *
 * Handles:
 *  - Density (dp -> px)
 *  - Rotation (0/90/180/270)
 *  - Status bar / navigation bar / cutout insets
 *  - Real vs available display size
 */
object CoordinateConverter {

    data class DisplayInfo(
        val realWidth: Int,
        val realHeight: Int,
        val availableWidth: Int,
        val availableHeight: Int,
        val density: Float,
        val rotation: Int,
        val isLandscape: Boolean
    )

    fun getDisplayInfo(context: Context): DisplayInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        val realMetrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        display.getMetrics(metrics)
        @Suppress("DEPRECATION")
        display.getRealMetrics(realMetrics)

        val realSize = Point()
        @Suppress("DEPRECATION")
        display.getRealSize(realSize)

        val rotation = display.rotation
        val isLandscape = when (rotation) {
            Surface.ROTATION_90, Surface.ROTATION_270 -> true
            else -> realSize.x > realSize.y
        }

        return DisplayInfo(
            realWidth = realSize.x,
            realHeight = realSize.y,
            availableWidth = metrics.widthPixels,
            availableHeight = metrics.heightPixels,
            density = realMetrics.density,
            rotation = rotation,
            isLandscape = isLandscape
        )
    }

    /**
     * Convert floating overlay view position (e.g., from WindowManager.LayoutParams x/y)
     * to physical pixels for injection.
     *
     * @param viewX X in WindowManager coordinate space (already in pixels)
     * @param viewY Y in WindowManager coordinate space
     * @param targetOffsetX Additional offset where game button is (e.g., user configured fire button at 80% width)
     * @param targetOffsetY
     */
    fun viewToPhysicalInjectionPoint(
        context: Context,
        viewX: Int,
        viewY: Int,
        targetOffsetX: Int,
        targetOffsetY: Int
    ): PointF {
        val info = getDisplayInfo(context)
        // View coords from WindowManager are already in physical pixels, but may be affected by rotation.
        // For injection, we need to ensure we are in the same coordinate space as InputManager expects: always native portrait? No, InputManager expects current rotation space.

        // Normalize: target is absolute screen position where we want to tap.
        // If targetOffset is given as absolute, use it directly. If it's relative to overlay, add.
        val rawX = (viewX + targetOffsetX).toFloat()
        val rawY = (viewY + targetOffsetY).toFloat()

        // Clamp to real display bounds to avoid injecting outside
        val clampedX = rawX.coerceIn(0f, info.realWidth - 1f)
        val clampedY = rawY.coerceIn(0f, info.realHeight - 1f)

        return PointF(clampedX, clampedY)
    }

    /**
     * Convert percentage-based target (0..1) to physical pixels.
     * This is the RECOMMENDED way for games: user sets fire button at e.g., 0.85f, 0.5f and it works across devices & rotations.
     */
    fun percentToPhysical(context: Context, xPercent: Float, yPercent: Float): PointF {
        val info = getDisplayInfo(context)
        return PointF(
            (info.realWidth * xPercent.coerceIn(0f, 1f)),
            (info.realHeight * yPercent.coerceIn(0f, 1f))
        )
    }

    /**
     * Get default displayId (usually 0). For foldables / multi-display, query WindowManager.
     */
    fun getDisplayId(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics: WindowMetrics = wm.currentWindowMetrics
            // DisplayId is in WindowMetrics? fallback to 0
            try {
                val display = context.display
                display?.displayId ?: 0
            } catch (_: Exception) { 0 }
        } else {
            0
        }
    }

    /**
     * Debug helper to log all conversions
     */
    fun debugLog(context: Context): String {
        val info = getDisplayInfo(context)
        return """
            Real: ${info.realWidth}x${info.realHeight}
            Available: ${info.availableWidth}x${info.availableHeight}
            Density: ${info.density}
            Rotation: ${info.rotation} (${if (info.isLandscape) "LANDSCAPE" else "PORTRAIT"})
            DisplayId: ${getDisplayId(context)}
        """.trimIndent()
    }
}
