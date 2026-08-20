package com.tapsense.app.device

import com.nfclocator.core.domain.model.DeviceFingerprint
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.ScreenSizeClass
import com.tapsense.app.data.settings.TapSenseSettings
import javax.inject.Inject

/**
 * Resolves which [DeviceIdentitySignals] to feed `ResolveAntennaLocationUseCase` with, given the
 * user's settings: the real running device by default ([DeviceIdentitySignalsProvider]), or a
 * synthetic fingerprint for a manually-picked phone from the optional phone-selection screen.
 *
 * A manual pick can never claim `isAndroid14ApiAvailable = true` - that API only ever reports
 * data for the physical unit the app is actually running on, never an arbitrary chosen model.
 */
class ActiveDeviceSignalsProvider @Inject constructor(
    private val autoDetectProvider: DeviceIdentitySignalsProvider,
) {
    fun signalsFor(settings: TapSenseSettings): DeviceIdentitySignals {
        val manufacturer = settings.selectedPhoneManufacturer
        val model = settings.selectedPhoneModel
        val formFactor = settings.selectedPhoneFormFactor

        if (manufacturer == null || model == null || formFactor == null) {
            return autoDetectProvider.current()
        }

        val normalizedManufacturer = DeviceFingerprint.normalize(manufacturer)
        val normalizedModel = DeviceFingerprint.normalize(model)
        return DeviceIdentitySignals(
            fingerprint = DeviceFingerprint(
                manufacturer = normalizedManufacturer,
                brand = normalizedManufacturer,
                model = normalizedModel,
                device = normalizedModel,
                product = normalizedModel,
                sku = null,
            ),
            formFactor = formFactor,
            foldState = FoldState.NOT_APPLICABLE,
            screenSizeClass = ScreenSizeClass.COMPACT,
            isAndroid14ApiAvailable = false,
        )
    }
}
