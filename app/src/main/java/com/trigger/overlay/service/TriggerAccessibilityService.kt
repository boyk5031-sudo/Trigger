package com.trigger.overlay.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.trigger.overlay.injection.AccessibilityInjector

class TriggerAccessibilityService : AccessibilityService() {

    private val injector = AccessibilityInjector()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("TriggerA11y", "Service connected")
        injector.attachService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        injector.detachService()
    }
}
