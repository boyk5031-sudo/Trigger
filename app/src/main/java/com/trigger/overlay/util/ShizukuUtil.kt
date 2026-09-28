package com.trigger.overlay.util

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuUtil {

    fun isShizukuAvailable(): Boolean {
        return try { Shizuku.pingBinder() } catch (_: Exception) { false }
    }

    fun hasShizukuPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) { false }
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(1001)
        } catch (_: Exception) {}
    }

    fun addPermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try { Shizuku.addRequestPermissionResultListener(listener) } catch (_: Exception) {}
    }

    fun removePermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try { Shizuku.removeRequestPermissionResultListener(listener) } catch (_: Exception) {}
    }
}
