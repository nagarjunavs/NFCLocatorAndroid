package com.tapsense.app.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tapsense.app.R
import com.tapsense.app.ui.component.TapSenseLogo

/**
 * Bottom navigation: Home / My Phone / a center Tap Guide action / Settings, matching the
 * design. Tap Guide is a full-screen flow pushed onto the back stack (see
 * [TapSenseDestinations.TAP_GUIDE]), not a persistent tab whose content stays mounted - it
 * doesn't have "state to preserve" the way Home/My Phone/Settings do.
 *
 * The background is a plain [Modifier.background], not a [Surface] - `Surface` clips its
 * content to its own measured (pre-offset) bounds, which would flatten the top of
 * [TapGuideFabItem]'s circle where it's meant to float above the bar via a negative offset.
 * A background modifier paints color without clipping, so the circle draws in full.
 */
@Composable
fun TapSenseBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onMyPhoneClick: () -> Unit,
    onTapGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                // Top padding must be >= TapGuideFabItem's upward offset (14dp) so the FAB's
                // circle lands flush with this container's own top edge instead of poking
                // above it - matches the design's footer, whose top padding exactly matches
                // its FAB's translateY(-14px).
                .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BottomBarItem(
                icon = { color -> HomeIcon(color) },
                label = stringResource(R.string.nav_home),
                selected = currentRoute == TapSenseDestinations.HOME,
                onClick = onHomeClick,
            )
            BottomBarItem(
                icon = { color -> MyPhoneIcon(color) },
                label = stringResource(R.string.nav_my_phone),
                selected = currentRoute == TapSenseDestinations.MY_PHONE,
                onClick = onMyPhoneClick,
            )
            TapGuideFabItem(onClick = onTapGuideClick)
            BottomBarItem(
                icon = { color -> SettingsIcon(color) },
                label = stringResource(R.string.nav_settings),
                selected = currentRoute == TapSenseDestinations.SETTINGS,
                onClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun BottomBarItem(icon: @Composable (Color) -> Unit, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(4.dp)
            .semantics { contentDescription = label },
    ) {
        Box(modifier = Modifier.padding(4.dp)) {
            icon(color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/** House outline + door, matching the design's current Home tab glyph. */
@Composable
private fun HomeIcon(color: Color, modifier: Modifier = Modifier) {
    val vector = remember(color) {
        ImageVector.Builder(
            name = "HomeTabIcon",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = addPathNodes("M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"),
                stroke = SolidColor(color),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            addPath(
                pathData = addPathNodes("M9 22L9 12L15 12L15 22"),
                stroke = SolidColor(color),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
    Image(imageVector = vector, contentDescription = null, modifier = modifier.size(20.dp))
}

/** Phone outline with a bottom home-button line, matching the design's current My Phone tab glyph. */
@Composable
private fun MyPhoneIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        // Design viewBox is 0..20, so this scale is a 1:1 passthrough at the default 20dp size -
        // kept explicit (rather than hardcoding px) so the glyph stays correct if resized.
        val scale = size.width / 20f
        val strokeWidth = 1.6f * scale
        drawRoundRect(
            color = color,
            topLeft = Offset(5f * scale, 1.5f * scale),
            size = Size(10f * scale, 17f * scale),
            cornerRadius = CornerRadius(2.5f * scale),
            style = Stroke(strokeWidth),
        )
        drawLine(
            color = color,
            start = Offset(8.3f * scale, 16f * scale),
            end = Offset(11.7f * scale, 16f * scale),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

/** Gear + center dot, matching the design's current Settings tab glyph. */
@Composable
private fun SettingsIcon(color: Color, modifier: Modifier = Modifier) {
    val vector = remember(color) {
        ImageVector.Builder(
            name = "SettingsTabIcon",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = addPathNodes(
                    "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 " +
                        "1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33" +
                        "l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 " +
                        "0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l" +
                        ".06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 " +
                        "0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 " +
                        "1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z",
                ),
                stroke = SolidColor(color),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
    Box(modifier = modifier.size(20.dp)) {
        Image(imageVector = vector, contentDescription = null, modifier = Modifier.fillMaxSize())
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = size.width / 24f
            drawCircle(color = color, radius = 3f * scale, center = Offset(12f * scale, 12f * scale), style = Stroke(1.8f * scale))
        }
    }
}

@Composable
private fun TapGuideFabItem(onClick: () -> Unit) {
    val label = stringResource(R.string.nav_tap_guide)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(52.dp)
                .semantics { contentDescription = label },
            onClick = onClick,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                TapSenseLogo(color = MaterialTheme.colorScheme.onPrimary, size = 24.dp, singleRing = true)
            }
        }
        // The Surface's -14dp offset only shifts where it's *painted* - it doesn't move this
        // Text, which would otherwise land 14dp below the circle (vs. the other tabs' 4dp
        // icon-to-label gap). Pull it up by the same amount, minus the 4dp gap we actually want.
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.offset(y = (-10).dp),
        )
    }
}
