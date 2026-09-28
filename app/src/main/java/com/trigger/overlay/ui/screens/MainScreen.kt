package com.trigger.overlay.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trigger.overlay.injection.CoordinateConverter
import com.trigger.overlay.service.FloatingTriggerService
import com.trigger.overlay.ui.components.*
import com.trigger.overlay.util.PermissionUtil
import com.trigger.overlay.util.ShizukuUtil
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("trigger_prefs", Context.MODE_PRIVATE)

    var overlayPermission by remember { mutableStateOf(PermissionUtil.hasOverlayPermission(context)) }
    var accessibilityEnabled by remember { mutableStateOf(PermissionUtil.isAccessibilityEnabled(context)) }
    var shizukuAvailable by remember { mutableStateOf(ShizukuUtil.isShizukuAvailable()) }
    var shizukuPermission by remember { mutableStateOf(ShizukuUtil.hasShizukuPermission()) }
    var isOverlayActive by remember { mutableStateOf(false) }

    // Config states
    var targetXPercent by remember { mutableFloatStateOf(prefs.getFloat("target_x", 0.85f)) }
    var targetYPercent by remember { mutableFloatStateOf(prefs.getFloat("target_y", 0.5f)) }
    var overlaySize by remember { mutableFloatStateOf(prefs.getInt("overlay_size", 120).toFloat()) }
    var tapInterval by remember { mutableFloatStateOf(prefs.getLong("interval", 50L).toFloat()) }
    var rapidFire by remember { mutableStateOf(prefs.getBoolean("rapid", false)) }
    var opacity by remember { mutableFloatStateOf(prefs.getFloat("opacity", 0.9f)) }

    // Refresh permissions periodically
    LaunchedEffect(Unit) {
        while (true) {
            overlayPermission = PermissionUtil.hasOverlayPermission(context)
            accessibilityEnabled = PermissionUtil.isAccessibilityEnabled(context)
            shizukuAvailable = ShizukuUtil.isShizukuAvailable()
            shizukuPermission = ShizukuUtil.hasShizukuPermission()
            delay(1500)
        }
    }

    fun saveConfig() {
        prefs.edit()
            .putFloat("target_x", targetXPercent)
            .putFloat("target_y", targetYPercent)
            .putInt("overlay_size", overlaySize.toInt())
            .putLong("interval", tapInterval.toLong())
            .putBoolean("rapid", rapidFire)
            .putFloat("opacity", opacity)
            .apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // iOS Large Title
            IOSLargeTitle(
                title = "Trigger",
                subtitle = "Zero-latency game touch injection • iOS Design"
            )

            // Hero card with start button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF007AFF))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Games, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Floating Trigger", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(
                                if (isOverlayActive) "Active • Tap overlay to fire" else "Ready to launch",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IOSButton(
                            text = if (isOverlayActive) "Stop" else "Start Trigger",
                            onClick = {
                                if (!overlayPermission) {
                                    PermissionUtil.requestOverlayPermission(context)
                                    return@IOSButton
                                }
                                if (isOverlayActive) {
                                    FloatingTriggerService.stop(context)
                                    isOverlayActive = false
                                } else {
                                    FloatingTriggerService.start(context)
                                    isOverlayActive = true
                                    saveConfig()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isFilled = false
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = if (shizukuAvailable && shizukuPermission) "⚡ <5ms" else "🐢 fallback",
                                color = Color(0xFF007AFF),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Diagnosis section
            IOSSection(
                title = "Diagnostics • Root Cause Fix",
                footer = "Shizuku shell injection bypasses Method 1-5 failures. Accessibility is fallback only."
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IOSStatusCard(
                        title = "Overlay Permission",
                        status = if (overlayPermission) "Granted • TYPE_APPLICATION_OVERLAY" else "Required for floating trigger",
                        isOk = overlayPermission,
                        onClick = { PermissionUtil.requestOverlayPermission(context) }
                    )
                    IOSStatusCard(
                        title = "Shizuku • Shell UID 2000",
                        status = when {
                            !shizukuAvailable -> "Not running • Install Shizuku app"
                            !shizukuPermission -> "Permission needed • Tap to grant"
                            else -> "Ready • InputManager.injectInputEvent • <5ms latency"
                        },
                        isOk = shizukuAvailable && shizukuPermission,
                        onClick = {
                            if (!shizukuAvailable) {
                                Toast.makeText(context, "Install Shizuku from GitHub / Play Store", Toast.LENGTH_LONG).show()
                            } else if (!shizukuPermission) {
                                ShizukuUtil.requestPermission()
                            }
                        }
                    )
                    IOSStatusCard(
                        title = "Accessibility • Fallback",
                        status = if (accessibilityEnabled) "Enabled • dispatchGesture fallback" else "Optional • Enable for fallback mode",
                        isOk = accessibilityEnabled,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        }
                    )
                    IOSStatusCard(
                        title = "Coordinate System",
                        status = CoordinateConverter.debugLog(context).replace("\n", " • "),
                        isOk = true
                    )
                }
            }

            // Target config - iOS style sliders
            IOSSection(
                title = "Target Position",
                footer = "Percentage-based • Works across DPI, rotation, notch. This fixes Method 3 misalignment."
            ) {
                IOSSlider(
                    value = targetXPercent,
                    onValueChange = { targetXPercent = it; saveConfig() },
                    label = "Horizontal",
                    valueLabel = "${(targetXPercent * 100).toInt()}%"
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                IOSSlider(
                    value = targetYPercent,
                    onValueChange = { targetYPercent = it; saveConfig() },
                    label = "Vertical",
                    valueLabel = "${(targetYPercent * 100).toInt()}%"
                )
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp)) {
                    Text(
                        text = "Tap Preview: Will inject at ${CoordinateConverter.percentToPhysical(context, targetXPercent, targetYPercent).let { "${it.x.toInt()}, ${it.y.toInt()}px" }} on ${CoordinateConverter.getDisplayInfo(context).let { "${it.realWidth}x${it.realHeight}" }}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IOSSection(
                title = "Trigger Appearance • iOS AssistiveTouch",
                footer = "Frosted glass outer, blue dot inner. Draggable, snap-to-edge, spring animation."
            ) {
                IOSSlider(
                    value = overlaySize,
                    onValueChange = { overlaySize = it; saveConfig() },
                    valueRange = 80f..200f,
                    label = "Size",
                    valueLabel = "${overlaySize.toInt()}dp"
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                IOSSlider(
                    value = opacity,
                    onValueChange = { opacity = it; saveConfig() },
                    label = "Opacity",
                    valueLabel = "${(opacity * 100).toInt()}%"
                )
            }

            IOSSection(
                title = "Fire Mode",
                footer = if (rapidFire) "Hold trigger = rapid fire • Release = stop. Zero blocking of game touches." else "Tap trigger = single tap • Hold for options."
            ) {
                IOSListRow(
                    icon = Icons.Default.Bolt,
                    title = "Rapid Fire",
                    subtitle = if (rapidFire) "On • ${tapInterval.toInt()}ms interval" else "Off • Single tap",
                    trailing = {
                        IOSToggle(checked = rapidFire, onCheckedChange = { rapidFire = it; saveConfig() })
                    },
                    showSeparator = rapidFire
                )
                if (rapidFire) {
                    IOSSlider(
                        value = tapInterval,
                        onValueChange = { tapInterval = it; saveConfig() },
                        valueRange = 16f..500f,
                        label = "Interval",
                        valueLabel = "${tapInterval.toInt()}ms"
                    )
                }
            }

            IOSSection(
                title = "Technical Details • Why Old Methods Fail",
                footer = "Refactored solution uses cached InputManager binder via Shizuku shell."
            ) {
                IOSListRow(
                    icon = Icons.Default.BugReport,
                    iconTint = Color(0xFFFF3B30),
                    title = "Method 1: input tap via exec()",
                    subtitle = "Spawns JVM process per tap • 200-1000ms latency • Fixed by Binder IPC",
                    showSeparator = true
                )
                IOSListRow(
                    icon = Icons.Default.Gamepad,
                    iconTint = Color(0xFFFF9500),
                    title = "Method 2: dispatchGesture()",
                    subtitle = "Fails on OpenGL/Vulkan surfaces • Throttled • Fallback only",
                    showSeparator = true
                )
                IOSListRow(
                    icon = Icons.Default.AspectRatio,
                    iconTint = Color(0xFF5856D6),
                    title = "Method 3: DPI misalignment",
                    subtitle = "Fixed by percentToPhysical + realMetrics + rotation handling",
                    showSeparator = true
                )
                IOSListRow(
                    icon = Icons.Default.TouchApp,
                    iconTint = Color(0xFF007AFF),
                    title = "Method 4: MotionEvent lifecycle",
                    subtitle = "Fixed by proper downTime, ACTION_DOWN→UP, source TOUCHSCREEN",
                    showSeparator = true
                )
                IOSListRow(
                    icon = Icons.Default.Security,
                    iconTint = Color(0xFF8E8E93),
                    title = "Method 5: Hidden API block",
                    subtitle = "Fixed by running reflection in shell UID 2000 via Shizuku",
                    showSeparator = false
                )
            }

            Spacer(Modifier.height(32.dp))

            // Footer
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Trigger • iOS Design • Zero Latency • Game Compatible", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
