package com.tapsense.app.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The TapSense mark: concentric rings around a solid dot - the same shape language as the
 * tap-zone marker itself (`nfc-locator-core`'s `AntennaSilhouette`), used for the splash screen,
 * onboarding, and the in-app wordmark lockup.
 */
@Composable
fun TapSenseLogo(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    pulsing: Boolean = false,
    reducedMotion: Boolean = false,
    singleRing: Boolean = false,
) {
    val pulse = if (pulsing && !reducedMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
        val animated by infiniteTransition.animateFloat(
            initialValue = LOGO_RIPPLE_MIN_SCALE,
            targetValue = LOGO_RIPPLE_MAX_SCALE,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "logo_pulse_scale",
        )
        animated
    } else {
        null
    }

    Canvas(modifier = modifier.size(size)) {
        val center = androidx.compose.ui.geometry.Offset(this.size.width / 2f, this.size.height / 2f)
        val maxRadius = this.size.minDimension / 2f

        if (pulse != null) {
            val pulseAlpha = (LOGO_RIPPLE_MAX_SCALE - pulse) / (LOGO_RIPPLE_MAX_SCALE - LOGO_RIPPLE_MIN_SCALE) * LOGO_RIPPLE_PEAK_ALPHA
            drawCircle(color = color.copy(alpha = pulseAlpha.coerceIn(0f, LOGO_RIPPLE_PEAK_ALPHA)), radius = maxRadius * pulse, center = center)
        }

        if (!singleRing) {
            drawCircle(color = color, radius = maxRadius * 0.92f, center = center, style = Stroke(width = maxRadius * 0.06f))
        }
        drawCircle(color = color, radius = maxRadius * 0.56f, center = center, style = Stroke(width = maxRadius * 0.06f))
        drawCircle(color = color, radius = maxRadius * 0.21f, center = center)
    }
}

private const val LOGO_RIPPLE_MIN_SCALE = 0.6f
// Design's tsPulse keyframe animates scale 0.6->1.9 (not a normalized 0..1 range) - the radius
// draw must use `pulse` directly, not `pulse / LOGO_RIPPLE_MAX_SCALE`, or the peak radius caps
// at exactly maxRadius instead of the design's ~1.9x overshoot past the rings.
private const val LOGO_RIPPLE_MAX_SCALE = 1.9f
// Matches AntennaSilhouette's MARKER_RIPPLE_PEAK_ALPHA - design's tsPulse keyframe peaks at 0.55.
private const val LOGO_RIPPLE_PEAK_ALPHA = 0.5f
