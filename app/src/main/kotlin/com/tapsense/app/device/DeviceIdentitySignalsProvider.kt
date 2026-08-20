package com.tapsense.app.device

import android.content.Context
import android.os.Build
import androidx.window.layout.FoldingFeature
import com.nfclocator.core.domain.fingerprint.DeviceFingerprintProvider
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.ScreenSizeClass
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Builds the real [DeviceIdentitySignals] for this host app. This is exactly the kind of
 * "inject, don't own" seam the library leaves to the host: form factor / fold state come
 * from whatever the host already has - here, a `smallestScreenWidthDp` heuristic for
 * BAR/TABLET, refined with a real `androidx.window` [FoldingFeature] reading (via
 * [FoldStateSignals], kept fresh by `TapSenseApp`'s `WindowInfoTracker` collection) whenever
 * one is currently reported.
 *
 * Known limitation, not solved here: when a book-style foldable is closed to its cover
 * display, `androidx.window` reports no `FoldingFeature` at all (only the active display's
 * own layout), which is indistinguishable from a plain bar phone by this signal alone - no
 * public API exposes "closed foldable" as a distinct state. [FoldState.FOLDED] is therefore
 * never assigned here; only [FoldState.UNFOLDED] (a real hinge currently observed) or
 * [FoldState.NOT_APPLICABLE] (none observed) are.
 */
class DeviceIdentitySignalsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fingerprintProvider: DeviceFingerprintProvider,
    private val foldStateSignals: FoldStateSignals,
) {
    fun current(): DeviceIdentitySignals {
        val smallestWidthDp = context.resources.configuration.smallestScreenWidthDp
        val folding = foldStateSignals.current.value
        val heuristicFormFactor = if (smallestWidthDp >= TABLET_SMALLEST_WIDTH_DP) FormFactor.TABLET else FormFactor.BAR

        val formFactor = when (folding?.orientation) {
            FoldingFeature.Orientation.VERTICAL -> FormFactor.FOLD_BOOK
            FoldingFeature.Orientation.HORIZONTAL -> FormFactor.FOLD_FLIP
            else -> heuristicFormFactor
        }
        // Any currently-reported hinge (FLAT or HALF_OPENED) means the device is actively
        // unfolded/unfolding right now - FoldState has no third "transitional" value to map
        // HALF_OPENED to separately, and both states are equally "not closed."
        val foldState = if (folding == null) FoldState.NOT_APPLICABLE else FoldState.UNFOLDED

        return DeviceIdentitySignals(
            fingerprint = fingerprintProvider.current(),
            formFactor = formFactor,
            foldState = foldState,
            screenSizeClass = when {
                smallestWidthDp >= TABLET_SMALLEST_WIDTH_DP -> ScreenSizeClass.EXPANDED
                smallestWidthDp >= MEDIUM_SMALLEST_WIDTH_DP -> ScreenSizeClass.MEDIUM
                else -> ScreenSizeClass.COMPACT
            },
            isAndroid14ApiAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        )
    }

    private companion object {
        const val MEDIUM_SMALLEST_WIDTH_DP = 480
        const val TABLET_SMALLEST_WIDTH_DP = 600
    }
}
