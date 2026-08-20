package com.nfclocator.core.domain.model

/**
 * A rectangle expressed as fractions (0f..1f) of a silhouette template's bounding box,
 * not pixels - so one catalog record renders correctly on any screen density/size.
 *
 * Convention every producer of a [NormalizedRect] antenna zone must follow: `x`/`y` are
 * relative to the phone's **back** panel, as viewed with the back facing the viewer, phone
 * held upright in portrait (top edge up) - matching how every silhouette in this library is
 * drawn, since the NFC antenna itself sits under the back cover. A source whose raw data is
 * in a different frame (e.g. the OS-reported front/screen-facing coordinates in
 * `Android14AntennaInfoMapper`) must convert to this convention before constructing one.
 */
data class NormalizedRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
) {
    init {
        require(x in 0f..1f) { "x must be within 0f..1f, was $x" }
        require(y in 0f..1f) { "y must be within 0f..1f, was $y" }
        require(width in 0f..1f) { "width must be within 0f..1f, was $width" }
        require(height in 0f..1f) { "height must be within 0f..1f, was $height" }
        require(x + width <= 1f + EPSILON) { "x + width exceeds 1f: x=$x width=$width" }
        require(y + height <= 1f + EPSILON) { "y + height exceeds 1f: y=$y height=$height" }
    }

    val centerX: Float get() = x + width / 2f
    val centerY: Float get() = y + height / 2f

    companion object {
        private const val EPSILON = 0.001f

        /** A small square zone centered on ([centerX], [centerY]) with the given [side] length. */
        fun centeredSquare(centerX: Float, centerY: Float, side: Float): NormalizedRect {
            val half = side / 2f
            return NormalizedRect(
                x = (centerX - half).coerceIn(0f, 1f - side),
                y = (centerY - half).coerceIn(0f, 1f - side),
                width = side,
                height = side,
            )
        }
    }
}
