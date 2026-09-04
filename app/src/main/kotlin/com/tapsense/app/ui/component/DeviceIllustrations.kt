package com.tapsense.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A plain solid-filled phone silhouette for decorative (non-data-driven) contexts - My Phone's
 * Front tab, which has no real [com.nfclocator.core.domain.model.NormalizedRect] antenna zone to
 * draw `nfc-locator-core`'s `AntennaSilhouette` against (unlike the Back tab, and unlike
 * Onboarding, which now shows the real per-device `AntennaMarker`).
 *
 * [cameraBump] draws a top-left corner camera-module square, matching a device's *back* panel
 * (see My Phone's Back tab). [screenInset] draws an inset "screen" rectangle with a top-center
 * notch instead, matching a device's *front* (My Phone's Front tab) - the two are mutually
 * exclusive decorations for the same body shape, letting a caller show either without the other.
 *
 * [borderColor], when supplied, outlines the body with a 1dp stroke - most back-panel callers
 * pass one so the shape stays legible against a background close in tone to [color].
 */
@Composable
fun PhoneSilhouette(
    color: Color,
    modifier: Modifier = Modifier,
    cameraBump: Boolean = false,
    bumpColor: Color = color,
    screenInset: Boolean = false,
    insetColor: Color = color,
    notchColor: Color = color,
    borderColor: Color? = null,
) {
    Canvas(modifier = modifier) {
        val bodyCornerRadius = CornerRadius(size.width * 0.22f)
        drawRoundRect(color = color, cornerRadius = bodyCornerRadius)
        if (borderColor != null) {
            drawRoundRect(color = borderColor, cornerRadius = bodyCornerRadius, style = Stroke(1.dp.toPx()))
        }
        if (cameraBump) {
            val bumpWidth = size.width * 0.1987f
            val bumpHeight = size.height * 0.0917f
            drawRoundRect(
                color = bumpColor,
                topLeft = Offset(size.width * 0.1154f, size.height * 0.0621f),
                size = Size(bumpWidth, bumpHeight),
                cornerRadius = CornerRadius(bumpWidth * 0.3548f),
            )
        }
        if (screenInset) {
            val insetLeft = size.width * 0.0641f
            val insetTop = size.height * 0.0296f
            drawRoundRect(
                color = insetColor,
                topLeft = Offset(insetLeft, insetTop),
                size = Size(size.width - insetLeft * 2f, size.height - insetTop * 2f),
                cornerRadius = CornerRadius(size.width * 0.2179f),
            )
            val notchWidth = size.width * 0.2821f
            val notchHeight = size.height * 0.0355f
            drawRoundRect(
                color = notchColor,
                topLeft = Offset((size.width - notchWidth) / 2f, size.height * 0.0473f),
                size = Size(notchWidth, notchHeight),
                cornerRadius = CornerRadius(notchHeight / 2f),
            )
        }
    }
}

/**
 * A generic circular reader/terminal device - the design shows this beside the phone on
 * Onboarding's "tap with confidence" page and on Tap Guide, representing the tag/lock/terminal
 * the phone is tapping against (never the tap-zone marker's own aqua accent, since this is
 * hardware, not guidance).
 */
@Composable
fun ReaderDeviceIllustration(outerColor: Color, innerColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = outerColor, radius = size.minDimension / 2f, center = center)
        drawCircle(color = innerColor, radius = size.minDimension * 0.31f, center = center)
    }
}
