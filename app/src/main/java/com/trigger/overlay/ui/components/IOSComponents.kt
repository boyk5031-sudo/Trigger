package com.trigger.overlay.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS-style UI components
 */

@Composable
fun IOSSection(
    title: String,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.08).sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(12.dp), clip = false)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }

        footer?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun IOSListRow(
    icon: ImageVector? = null,
    iconTint: Color = Color(0xFF007AFF),
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    showChevron: Boolean = false,
    showSeparator: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = it, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp), color = MaterialTheme.colorScheme.onSurface)
                subtitle?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            trailing?.invoke()

            if (showChevron) {
                Spacer(Modifier.width(8.dp))
                Text(text = "›", fontSize = 20.sp, color = Color(0xFFC7C7CC), fontWeight = FontWeight.Light)
            }
        }

        if (showSeparator) {
            Divider(
                modifier = Modifier.padding(start = if (icon != null) 56.dp else 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun IOSToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // iOS switch colors: green when on, light gray when off
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF34C759),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE9E9EB),
            uncheckedBorderColor = Color(0xFFE9E9EB)
        )
    )
}

@Composable
fun IOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    label: String? = null,
    valueLabel: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (label != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp))
                valueLabel?.let { Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp) }
            }
            Spacer(Modifier.height(8.dp))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF007AFF),
                inactiveTrackColor = Color(0xFFE5E5EA)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun IOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    isFilled: Boolean = true
) {
    val bgColor = when {
        isDestructive && isFilled -> Color(0xFFFF3B30)
        isDestructive && !isFilled -> Color.Transparent
        isFilled -> Color(0xFF007AFF)
        else -> Color(0xFFE5E5EA)
    }
    val textColor = when {
        isDestructive && isFilled -> Color.White
        isDestructive && !isFilled -> Color(0xFFFF3B30)
        isFilled -> Color.White
        else -> Color(0xFF007AFF)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
    }
}

@Composable
fun IOSStatusCard(
    title: String,
    status: String,
    isOk: Boolean,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val bg = if (isOk) Color(0xFF34C759).copy(alpha = 0.12f) else Color(0xFFFF3B30).copy(alpha = 0.12f)
    val dotColor = if (isOk) Color(0xFF34C759) else Color(0xFFFF3B30)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = status, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) Text(text = "›", fontSize = 20.sp, color = Color(0xFFC7C7CC))
    }
}

@Composable
fun IOSLargeTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 34.sp), color = MaterialTheme.colorScheme.onBackground)
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
