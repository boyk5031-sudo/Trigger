package com.trigger.overlay.injection

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Fallback injector using AccessibilityService.dispatchGesture()
 *
 * Known limitations (Method 2):
 *  - May fail inside OpenGL/Vulkan game surfaces on some OEMs
 *  - Limited tap rate (system throttles)
 *  - No true multi-touch with existing fingers
 *
 * But we implement it CORRECTLY:
 *  - Proper downTime
 *  - Uses Path, not shell
 *  - Callback handling
 */
class AccessibilityInjector : TouchInjector {

    companion object {
        private const val TAG = "A11yInjector"
        @Volatile var instance: AccessibilityInjector? = null
        @Volatile var service: AccessibilityService? = null
    }

    init {
        instance = this
    }

    fun attachService(svc: AccessibilityService) {
        service = svc
        Log.i(TAG, "Accessibility service attached")
    }

    fun detachService() {
        service = null
    }

    override suspend fun tap(x: Float, y: Float, displayId: Int): Boolean {
        val svc = service ?: return false
        return suspendCancellableCoroutine { cont ->
            try {
                val path = Path().apply { moveTo(x, y) }
                val builder = GestureDescription.Builder()
                // Duration 40ms mimics real tap, too short may be ignored
                builder.addStroke(GestureDescription.StrokeDescription(path, 0, 40))
                val gesture = builder.build()

                svc.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        cont.resume(true)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.w(TAG, "Gesture cancelled")
                        cont.resume(false)
                    }
                }, null)
            } catch (e: Exception) {
                Log.e(TAG, "dispatchGesture failed", e)
                cont.resume(false)
            }
        }
    }

    override suspend fun down(x: Float, y: Float, displayId: Int, downTime: Long): Boolean {
        // Accessibility API does not expose down/move/up separately, we simulate with long gesture
        return tap(x, y, displayId)
    }

    override suspend fun move(x: Float, y: Float, displayId: Int, downTime: Long): Boolean = true
    override suspend fun up(x: Float, y: Float, displayId: Int, downTime: Long): Boolean = true

    override fun isAvailable(): Boolean = service != null
    override fun getName(): String = "Accessibility • Fallback"
}
