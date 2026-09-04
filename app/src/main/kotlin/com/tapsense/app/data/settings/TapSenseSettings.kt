package com.tapsense.app.data.settings

import com.nfclocator.core.domain.model.FormFactor

/**
 * Everything persisted across app restarts. [selectedPhoneManufacturer]/[selectedPhoneModel]
 * being null means "auto-detect the phone this app is actually running on" - the default,
 * primary behavior (see [com.tapsense.app.device.DeviceIdentitySignalsProvider]). A non-null
 * triple means the user explicitly overrode that via the optional phone-selection screen.
 * [selectedPhoneFormFactor] is persisted alongside the pick (rather than re-derived from the
 * catalog on every launch) so resolution doesn't depend on that catalog entry still existing
 * unchanged later.
 */
data class TapSenseSettings(
    val onboardingCompleted: Boolean = false,
    val selectedPhoneManufacturer: String? = null,
    val selectedPhoneModel: String? = null,
    val selectedPhoneFormFactor: FormFactor? = null,
    val hapticsEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val tapTestSuccessCount: Int = 0,
    val reviewFlowRequested: Boolean = false,
) {
    val hasManualPhoneOverride: Boolean
        get() = selectedPhoneManufacturer != null && selectedPhoneModel != null
}
