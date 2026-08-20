package com.nfclocator.core.data.android14

import android.nfc.NfcAntennaInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.nfclocator.core.domain.model.NormalizedRect

/** How large (as a fraction of device width/height) the drawn antenna zone is around a point-reading. */
private const val ANTENNA_MARKER_FRACTION = 0.12f

/**
 * Converts a single OEM-reported antenna position (device-space, arbitrary linear unit -
 * typically mm) into normalized 0f..1f coordinates, or null if the reading is implausible.
 *
 * Guards against exactly the failure modes OEM implementations are known to hit: zero/negative
 * device bounds, an antenna location outside the device's own reported bounds, and NaN/Infinity
 * from bad math on the OEM side.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal fun NfcAntennaInfo.toNormalizedZoneOrNull(antennaIndex: Int): NormalizedRect? {
    val width = deviceWidth
    val height = deviceHeight
    if (width <= 0 || height <= 0) return null

    val antenna = availableNfcAntennas.getOrNull(antennaIndex) ?: return null
    val x = antenna.locationX
    val y = antenna.locationY

    // Reject out-of-bounds or degenerate (0,0 with tiny device, a common OEM stub bug) readings.
    if (x < 0 || y < 0 || x > width || y > height) return null

    val fx = x.toFloat() / width.toFloat()
    val fy = y.toFloat() / height.toFloat()
    if (!fx.isFinite() || !fy.isFinite()) return null

    // NfcAntennaInfo reports locationX/locationY in the device's front (screen-facing)
    // coordinate frame, but every silhouette this library draws represents the phone's BACK
    // panel (see NormalizedRect's KDoc) - flipping the device over to view the back swaps
    // left/right, so the front-frame X must be mirrored before it's used as a back-relative
    // coordinate. Y is unaffected (flipping around the vertical axis doesn't move top/bottom).
    return NormalizedRect.centeredSquare(
        centerX = (1f - fx).coerceIn(0f, 1f),
        centerY = fy.coerceIn(0f, 1f),
        side = ANTENNA_MARKER_FRACTION,
    )
}
