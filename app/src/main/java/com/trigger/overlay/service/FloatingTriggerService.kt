package com.trigger.overlay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.trigger.overlay.MainActivity
import com.trigger.overlay.injection.CoordinateConverter
import com.trigger.overlay.injection.InjectorManager
import com.trigger.overlay.overlay.TriggerOverlayView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service that hosts the floating trigger overlay.
 *
 * Multi-touch fix:
 *  - Overlay window uses FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL
 *  - Only the trigger button consumes touch; rest passes through to game
 *  - Injection runs on separate thread, not blocking UI touch stream
 */
class FloatingTriggerService : Service() {

    companion object {
        private const val TAG = "FloatingTriggerSvc"
        private const val NOTIF_ID = 1001
        private const val CHANNEL_ID = "trigger_overlay"

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, FloatingTriggerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingTriggerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: TriggerOverlayView? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var injectorManager: InjectorManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var rapidFireJob: Job? = null

    // Config (persisted)
    private var targetXPercent: Float = 0.85f
    private var targetYPercent: Float = 0.5f
    private var tapIntervalMs: Long = 50
    private var isRapidFire: Boolean = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences("trigger_prefs", MODE_PRIVATE)
        injectorManager = InjectorManager(this)

        loadConfig()
        createNotificationChannel()
        Log.i(TAG, CoordinateConverter.debugLog(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopOverlay()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIF_ID, buildNotification())
                showOverlay()
            }
        }
        return START_STICKY
    }

    private fun loadConfig() {
        targetXPercent = prefs.getFloat("target_x", 0.85f)
        targetYPercent = prefs.getFloat("target_y", 0.5f)
        tapIntervalMs = prefs.getLong("interval", 50L)
        isRapidFire = prefs.getBoolean("rapid", false)
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val savedX = prefs.getInt("overlay_x", 100)
        val savedY = prefs.getInt("overlay_y", 300)
        val size = prefs.getInt("overlay_size", 150) // dp converted later

        overlayView = TriggerOverlayView(this).apply {
            // iOS AssistiveTouch style config
            setSize(size)
            setOnTriggerListener(object : TriggerOverlayView.OnTriggerListener {
                override fun onTap() {
                    handleTriggerTap()
                }

                override fun onHoldStart() {
                    if (isRapidFire) startRapidFire()
                }

                override fun onHoldEnd() {
                    stopRapidFire()
                }

                override fun onPositionChanged(x: Int, y: Int) {
                    prefs.edit().putInt("overlay_x", x).putInt("overlay_y", y).apply()
                }
            })
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        try {
            windowManager.addView(overlayView, params)
            Log.i(TAG, "Overlay added at $savedX,$savedY")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay", e)
        }
    }

    private fun stopOverlay() {
        overlayView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        overlayView = null
        stopRapidFire()
    }

    private fun handleTriggerTap() {
        serviceScope.launch(Dispatchers.IO) {
            val point = CoordinateConverter.percentToPhysical(this@FloatingTriggerService, targetXPercent, targetYPercent)
            val displayId = CoordinateConverter.getDisplayId(this@FloatingTriggerService)
            Log.d(TAG, "Tap trigger -> inject at ${point.x},${point.y} display=$displayId")
            val success = injectorManager.tapPoint(point)
            Log.d(TAG, "Injection result: $success via ${injectorManager.getBestInjector().getName()}")
        }
    }

    private fun startRapidFire() {
        if (rapidFireJob?.isActive == true) return
        rapidFireJob = serviceScope.launch(Dispatchers.IO) {
            var downTime = SystemClock.uptimeMillis()
            val point = CoordinateConverter.percentToPhysical(this@FloatingTriggerService, targetXPercent, targetYPercent)
            val displayId = CoordinateConverter.getDisplayId(this@FloatingTriggerService)
            val injector = injectorManager.getBestInjector()
            // Initial down
            injector.down(point.x, point.y, displayId, downTime)

            while (isActive) {
                injector.move(point.x, point.y, displayId, downTime)
                // Re-tap for games that need tap, not hold
                if (!isRapidFire) {
                    // If not rapid, we hold
                    delay(16)
                } else {
                    injector.tap(point.x, point.y, displayId)
                    delay(tapIntervalMs)
                }
            }
            injector.up(point.x, point.y, displayId, downTime)
        }
    }

    private fun stopRapidFire() {
        rapidFireJob?.cancel()
        rapidFireJob = null
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Trigger Active")
            .setContentText("iOS-style overlay • Tap to configure • ${injectorManager.getBestInjector().getName()}")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pending)
            .addAction(android.R.drawable.ic_delete, "Stop", PendingIntent.getService(this, 1, Intent(this, FloatingTriggerService::class.java).apply { action = ACTION_STOP }, PendingIntent.FLAG_IMMUTABLE))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Trigger Overlay", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shows when trigger overlay is active"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopOverlay()
    }
}
