package com.nfclocator.core.data.android14

import android.nfc.NfcAntennaInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.toSilhouetteTemplateId
import com.nfclocator.core.domain.source.AntennaLocationSource
import javax.inject.Inject

private const val TAG = "Android14AntennaInfoSource"

/**
 * Layer 1 of the resolver chain (highest priority): the OS/hardware-reported antenna
 * position from `NfcAdapter#getNfcAntennaInfo()` (Android 14+ only). When it returns a
 * plausible reading this is the only source that earns [Confidence.EXACT], since it is
 * measured on the exact physical unit rather than looked up by model name.
 *
 * Every call is guarded: the API can be unsupported on this OEM build (null), throw, or
 * report implausible values (0x0 device bounds, an antenna outside the device's own bounds).
 * Any of those cases returns null so the chain falls through to the catalog layers.
 */
internal class Android14AntennaInfoSource @Inject constructor(
    private val provider: NfcAntennaInfoProvider,
    private val logger: NfcLocatorLogger,
) : AntennaLocationSource {

    override suspend fun resolve(signals: DeviceIdentitySignals): DeviceAntennaProfile? {
        if (!signals.isAndroid14ApiAvailable || Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return null
        }
        val info = provider.getAntennaInfo() ?: return null
        return buildProfile(info, signals)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun buildProfile(info: NfcAntennaInfo, signals: DeviceIdentitySignals): DeviceAntennaProfile? {
        if (info.availableNfcAntennas.isEmpty()) {
            logger.d(TAG, "NfcAntennaInfo reported zero antennas")
            return null
        }

        // A foldable may expose more than one physical antenna; pick the one that best
        // matches the reported fold state rather than always defaulting to index 0.
        val antennaIndex = when (signals.foldState) {
            FoldState.FOLDED -> 0
            FoldState.UNFOLDED -> info.availableNfcAntennas.lastIndex
            FoldState.NOT_APPLICABLE -> 0
        }

        val zone = info.toNormalizedZoneOrNull(antennaIndex)
        if (zone == null) {
            logger.w(TAG, "Rejected implausible NfcAntennaInfo reading")
            return null
        }

        return DeviceAntennaProfile(
            manufacturer = signals.fingerprint.manufacturer,
            model = signals.fingerprint.model,
            formFactor = signals.formFactor,
            silhouetteTemplateId = signals.formFactor.toSilhouetteTemplateId(signals.foldState),
            antennaZone = zone,
            confidence = Confidence.EXACT,
            source = DataSource.ANDROID14_API,
            catalogVersion = 0,
            lastVerifiedAt = null,
            // Real, exact, per-unit dimensions - already validated non-zero by toNormalizedZoneOrNull.
            aspectRatio = info.deviceWidth.toFloat() / info.deviceHeight.toFloat(),
        )
    }
}
