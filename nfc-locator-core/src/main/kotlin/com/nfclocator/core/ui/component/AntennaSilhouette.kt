package com.nfclocator.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.NormalizedRect

/**
 * Draws a device silhouette (vector shapes, not a photographic render - see project spec §5)
 * for [templateId] with the antenna [zone] overlaid.
 *
 * The caller controls the overall box size (e.g. `Modifier.fillMaxWidth().height(280.dp)`) -
 * this composable does not size itself via `aspectRatio`, since that modifier interacting
 * with a height-constrained parent (this screen uses a weighted layout) can shrink the box
 * far more aggressively than intended. Instead, the device silhouette's own aspect ratio is
 * fitted and centered *within* whatever box the caller gives it, entirely inside the draw
 * step - the antenna [zone]'s fractional coordinates are resolved against that fitted
 * content area, not the raw canvas size, so the marker always lines up with the silhouette
 * regardless of how much extra space the box has.
 *
 * The silhouette itself is a solid-filled shape (a real device's own back panel isn't a
 * hollow outline), colored by [silhouetteColor] - callers pick this per their own background
 * context (e.g. a dark device shape on a light card, or a light one on a hardcoded-dark card),
 * since unlike [isConfident]'s marker accent this has no single theme-correct default.
 *
 * The marker itself is a concentric-circle target - an expanding-and-fading ripple, a ring, and
 * (when confident) a solid center dot - centered within [zone] and sized as a fixed proportion
 * of the fitted silhouette's own width (matching a real device's own NFC-location indicator,
 * whose visual size doesn't vary with the specific catalog entry's zone dimensions - only its
 * *position* does).
 *
 * [isConfident] controls the marker style, which is the one rule this component must never
 * violate: a solid ring + dot for [com.nfclocator.core.domain.model.Confidence.EXACT] /
 * [com.nfclocator.core.domain.model.Confidence.APPROXIMATE], a dashed ring with no dot for
 * [com.nfclocator.core.domain.model.Confidence.GENERIC] / [com.nfclocator.core.domain.model.Confidence.UNKNOWN].
 * Callers should not pass `isConfident = true` for a low-confidence state.
 *
 * [reducedMotion] disables the ripple animation (renders the marker with a static settled glow)
 * for hosts wiring this to a system or in-app "reduce motion" accessibility setting.
 *
 * [aspectRatioOverride], when supplied (e.g. from
 * [com.nfclocator.core.domain.model.DeviceAntennaProfile.aspectRatio]), fits the silhouette to
 * the device's own real width/height ratio instead of [templateId]'s fixed per-bucket ratio -
 * null falls back to the template default.
 *
 * [showCameraBump], when true, draws a top-left corner camera-module square on top of the
 * silhouette - opt-in so only callers illustrating a device's *back* panel (e.g. My Phone's Back
 * tab) enable it, leaving every other host visually unchanged.
 *
 * [silhouetteBorderColor], when supplied, outlines the silhouette with a 1dp stroke - opt-in
 * since a flat fill with no outline is still the right look for some hosts (e.g. Tap Test's
 * "detected" success state uses a plain icon, not this component at all); most callers pass one
 * to keep the shape legible against a background close in tone to [silhouetteColor].
 */
@Composable
fun AntennaSilhouette(
    templateId: String,
    zone: NormalizedRect,
    isConfident: Boolean,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    silhouetteColor: Color = MaterialTheme.colorScheme.outline,
    aspectRatioOverride: Float? = null,
    showCameraBump: Boolean = false,
    cameraBumpColor: Color = silhouetteColor,
    silhouetteBorderColor: Color? = null,
) {
    val markerColor = if (isConfident) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val shape = silhouetteShapeFor(templateId)

    // Expanding ripple: scales MARKER_RIPPLE_MIN_SCALE..MARKER_RIPPLE_MAX_SCALE while fading out,
    // one-directional (RepeatMode.Restart) - mirrors TapSenseLogo's pulse exactly, since both
    // draw the same "tap zone" ripple language.
    val ripple = if (reducedMotion) {
        null
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "antenna_pulse")
        val animatedRipple by infiniteTransition.animateFloat(
            initialValue = MARKER_RIPPLE_MIN_SCALE,
            targetValue = MARKER_RIPPLE_MAX_SCALE,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "antenna_pulse_ripple",
        )
        animatedRipple
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val contentSize = fitWithinBounds(size, aspectRatioOverride ?: shape.aspectRatio)
        val offsetX = (size.width - contentSize.width) / 2f
        val offsetY = (size.height - contentSize.height) / 2f

        translate(left = offsetX, top = offsetY) {
            shape.draw(this, contentSize, silhouetteColor, silhouetteBorderColor)
            if (showCameraBump) {
                drawCameraBump(contentSize, cameraBumpColor)
            }

            val zoneCenter = Offset(
                (zone.x + zone.width / 2f) * contentSize.width,
                (zone.y + zone.height / 2f) * contentSize.height,
            )
            val maxRadius = markerRadiusFor(contentSize)

            if (ripple != null) {
                val rippleAlpha = ((MARKER_RIPPLE_MAX_SCALE - ripple) / (MARKER_RIPPLE_MAX_SCALE - MARKER_RIPPLE_MIN_SCALE) * MARKER_RIPPLE_PEAK_ALPHA)
                    .coerceIn(0f, MARKER_RIPPLE_PEAK_ALPHA)
                drawCircle(
                    color = markerColor.copy(alpha = rippleAlpha),
                    radius = maxRadius * ripple,
                    center = zoneCenter,
                )
            } else {
                drawCircle(color = markerColor.copy(alpha = 0.28f), radius = maxRadius, center = zoneCenter)
            }

            val ringRadius = maxRadius * 0.57f
            val ringStroke = maxRadius * 0.06f
            if (isConfident) {
                drawCircle(color = markerColor, radius = ringRadius, center = zoneCenter, style = Stroke(ringStroke))
                drawCircle(color = markerColor, radius = maxRadius * 0.2f, center = zoneCenter)
            } else {
                drawCircle(
                    color = markerColor,
                    radius = ringRadius,
                    center = zoneCenter,
                    style = Stroke(width = ringStroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))),
                )
            }
        }
    }
}

private const val MARKER_RIPPLE_MIN_SCALE = 0.6f
// Design's tsPulse keyframe animates scale 0.6->1.9 (not a normalized 0..1 range) - the radius
// draw must use `ripple` directly, not `ripple / MARKER_RIPPLE_MAX_SCALE`, or the peak radius
// caps at exactly maxRadius instead of the design's ~1.9x overshoot past the ring.
private const val MARKER_RIPPLE_MAX_SCALE = 1.9f
// Design's tsPulse keyframe peaks at opacity 0.55 - matching that (not the dimmer 0.4 this
// used before) makes the ripple read as noticeably bigger/more present, independent of radius.
private const val MARKER_RIPPLE_PEAK_ALPHA = 0.5f

/** Top-left corner camera-module square, matching a real device's back panel - see [AntennaSilhouette]'s `showCameraBump`. */
private fun DrawScope.drawCameraBump(contentSize: Size, color: Color) {
    val bumpWidth = contentSize.width * 0.1987f
    val bumpHeight = contentSize.height * 0.0917f
    drawRoundRect(
        color = color,
        topLeft = Offset(contentSize.width * 0.1154f, contentSize.height * 0.0621f),
        size = Size(bumpWidth, bumpHeight),
        cornerRadius = CornerRadius(bumpWidth * 0.3548f),
    )
}

/** Marker radius as a fixed proportion of the fitted silhouette's width, exposed for [GuidedSweepAnimation]. */
internal fun markerRadiusFor(contentSize: Size): Float = contentSize.width * 0.38f

/** The [templateId]'s device-outline aspect ratio, exposed for [GuidedSweepAnimation] to stay aligned with [AntennaSilhouette]. */
internal fun silhouetteAspectRatioFor(templateId: String): Float = silhouetteShapeFor(templateId).aspectRatio

/** Largest [Size] with the given width/height [ratio] that fits inside [bounds], exposed for [GuidedSweepAnimation]. */
internal fun fitWithinBounds(bounds: Size, ratio: Float): Size {
    val heightForFullWidth = bounds.width / ratio
    return if (heightForFullWidth <= bounds.height) {
        Size(bounds.width, heightForFullWidth)
    } else {
        Size(bounds.height * ratio, bounds.height)
    }
}

private fun silhouetteShapeFor(templateId: String): SilhouetteShape = when (templateId) {
    DeviceAntennaProfile.TEMPLATE_FOLD_BOOK_OPEN -> SilhouetteShape.FoldBookOpen
    DeviceAntennaProfile.TEMPLATE_FOLD_BOOK_CLOSED -> SilhouetteShape.Bar
    DeviceAntennaProfile.TEMPLATE_FOLD_FLIP_OPEN -> SilhouetteShape.Bar
    DeviceAntennaProfile.TEMPLATE_FOLD_FLIP_CLOSED -> SilhouetteShape.Square
    DeviceAntennaProfile.TEMPLATE_TABLET -> SilhouetteShape.Tablet
    else -> SilhouetteShape.Bar
}

/** Compose-`dp`-free shape definitions - solid-filled, matching a real device's back panel. */
private sealed class SilhouetteShape(val aspectRatio: Float) {
    abstract fun draw(scope: DrawScope, contentSize: Size, color: Color, borderColor: Color?)

    /** Strokes the same rounded rect [cornerRadius] used for the fill, 1dp wide, when [borderColor] is non-null. */
    protected fun DrawScope.drawBorder(contentSize: Size, cornerRadius: CornerRadius, borderColor: Color?) {
        if (borderColor != null) {
            drawRoundRect(color = borderColor, size = contentSize, cornerRadius = cornerRadius, style = Stroke(1.dp.toPx()))
        }
    }

    data object Bar : SilhouetteShape(aspectRatio = 0.5f) {
        override fun draw(scope: DrawScope, contentSize: Size, color: Color, borderColor: Color?) {
            with(scope) {
                val cornerRadius = CornerRadius(contentSize.width * 0.18f)
                drawRoundRect(color = color, size = contentSize, cornerRadius = cornerRadius)
                drawBorder(contentSize, cornerRadius, borderColor)
            }
        }
    }

    data object Square : SilhouetteShape(aspectRatio = 0.85f) {
        override fun draw(scope: DrawScope, contentSize: Size, color: Color, borderColor: Color?) {
            with(scope) {
                val cornerRadius = CornerRadius(contentSize.width * 0.22f)
                drawRoundRect(color = color, size = contentSize, cornerRadius = cornerRadius)
                drawBorder(contentSize, cornerRadius, borderColor)
            }
        }
    }

    data object Tablet : SilhouetteShape(aspectRatio = 0.72f) {
        override fun draw(scope: DrawScope, contentSize: Size, color: Color, borderColor: Color?) {
            with(scope) {
                val cornerRadius = CornerRadius(contentSize.width * 0.1f)
                drawRoundRect(color = color, size = contentSize, cornerRadius = cornerRadius)
                drawBorder(contentSize, cornerRadius, borderColor)
            }
        }
    }

    /** Two panels joined at a hinge line, representing an unfolded book-style foldable. */
    data object FoldBookOpen : SilhouetteShape(aspectRatio = 1.1f) {
        override fun draw(scope: DrawScope, contentSize: Size, color: Color, borderColor: Color?) {
            with(scope) {
                val cornerRadius = CornerRadius(contentSize.width * 0.08f)
                drawRoundRect(color = color, size = contentSize, cornerRadius = cornerRadius)
                drawBorder(contentSize, cornerRadius, borderColor)
                // Hinge seam down the middle, subtle against either a light or dark fill.
                drawLine(
                    color = Color.Black.copy(alpha = 0.18f),
                    start = Offset(contentSize.width / 2f, contentSize.height * 0.04f),
                    end = Offset(contentSize.width / 2f, contentSize.height * 0.96f),
                    strokeWidth = contentSize.width * 0.012f,
                )
            }
        }
    }
}
