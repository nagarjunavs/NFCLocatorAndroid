package com.nfclocator.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nfclocator.core.R
import com.nfclocator.core.domain.model.NormalizedRect

/**
 * A moving highlight that sweeps across the [zone] of the device silhouette, prompting the
 * user to physically move their phone across the reader rather than trust a fixed point -
 * this is what stands in for a marker whenever confidence is too low to show one confidently
 * (spec §3.4 / §7).
 *
 * Layered on top of [AntennaSilhouette] rather than replacing it, so the dashed low-confidence
 * zone and the moving sweep highlight are both visible together.
 *
 * [reducedMotion] freezes both the outline pulse and the sweep highlight at a static position,
 * for hosts wiring this to a system or in-app "reduce motion" accessibility setting.
 */
@Composable
fun GuidedSweepAnimation(
    templateId: String,
    zone: NormalizedRect,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    silhouetteColor: Color = MaterialTheme.colorScheme.outline,
    aspectRatioOverride: Float? = null,
    showCameraBump: Boolean = false,
    cameraBumpColor: Color = silhouetteColor,
    silhouetteBorderColor: Color? = null,
) {
    val sweepDescription = stringResource(R.string.nfc_locator_sweep_content_description)

    Box(modifier = modifier.semantics { contentDescription = sweepDescription }) {
        AntennaSilhouette(
            templateId = templateId,
            zone = zone,
            isConfident = false,
            modifier = Modifier.fillMaxSize(),
            reducedMotion = reducedMotion,
            silhouetteColor = silhouetteColor,
            aspectRatioOverride = aspectRatioOverride,
            showCameraBump = showCameraBump,
            cameraBumpColor = cameraBumpColor,
            silhouetteBorderColor = silhouetteBorderColor,
        )
        SweepHighlight(templateId = templateId, zone = zone, reducedMotion = reducedMotion, aspectRatioOverride = aspectRatioOverride)
    }
}

@Composable
private fun SweepHighlight(templateId: String, zone: NormalizedRect, reducedMotion: Boolean, aspectRatioOverride: Float? = null) {
    val highlightColor = MaterialTheme.colorScheme.tertiary
    val progress = if (reducedMotion) {
        0.5f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "sweep_position")
        val animatedProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "sweep_progress",
        )
        animatedProgress
    }

    // Mirrors AntennaSilhouette's own fit-and-center logic so the moving highlight stays
    // aligned with the marker drawn on top of the device outline underneath it.
    Canvas(modifier = Modifier.fillMaxSize()) {
        val contentSize = fitWithinBounds(size, aspectRatioOverride ?: silhouetteAspectRatioFor(templateId))
        val offsetX = (size.width - contentSize.width) / 2f
        val offsetY = (size.height - contentSize.height) / 2f

        translate(left = offsetX, top = offsetY) {
            val left = zone.x * contentSize.width
            val right = (zone.x + zone.width) * contentSize.width
            val centerY = (zone.y + zone.height / 2f) * contentSize.height
            val x = left + (right - left) * progress
            val radius = markerRadiusFor(contentSize)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(highlightColor.copy(alpha = 0.55f), highlightColor.copy(alpha = 0f)),
                    center = Offset(x, centerY),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(x, centerY),
            )
        }
    }
}
