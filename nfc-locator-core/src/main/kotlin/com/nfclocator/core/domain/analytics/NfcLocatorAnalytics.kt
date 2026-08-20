package com.nfclocator.core.domain.analytics

import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource

/**
 * Analytics sink the host app supplies (bound via Hilt in the host's own DI graph).
 *
 * The library never bundles or calls a concrete analytics SDK - it only emits these
 * typed events. This keeps the host's existing analytics stack as the single source
 * of truth and avoids a second, competing pipeline.
 */
interface NfcLocatorAnalytics {

    /**
     * Guidance UI (marker or sweep animation) was shown to the user.
     *
     * @param confidence resolved confidence level driving which visual state was shown.
     * @param source which resolver-chain layer produced the profile.
     * @param formFactor `FormFactor.name` of the silhouette shown.
     */
    fun guidanceShown(confidence: Confidence, source: DataSource, formFactor: String)

    /**
     * The user dismissed the guidance UI (back/close) before completing an unlock.
     *
     * @param confidence confidence level of the guidance that was dismissed.
     * @param timeVisibleMs milliseconds the guidance was on screen before dismissal.
     */
    fun guidanceDismissed(confidence: Confidence, timeVisibleMs: Long)

    /**
     * Resolution fell all the way through to the heuristic layer - no device-specific
     * match existed in the Android 14 API, remote catalog, or seed catalog.
     *
     * @param manufacturer normalized manufacturer from [com.nfclocator.core.domain.model.DeviceFingerprint].
     * @param formFactorGuess `FormFactor.name` heuristically assigned.
     */
    fun unknownDeviceDetected(manufacturer: String, formFactorGuess: String)

    /**
     * A device-specific match was found in the remote or bundled seed catalog.
     *
     * @param confidence always [Confidence.APPROXIMATE] for catalog matches.
     * @param source [DataSource.REMOTE_CATALOG] or [DataSource.SEED_CATALOG].
     * @param catalogVersion version of the catalog entry that matched.
     */
    fun catalogMatchFound(confidence: Confidence, source: DataSource, catalogVersion: Int)

    /**
     * `NfcAdapter#getNfcAntennaInfo()` returned a plausible, validated antenna position
     * (Android 14+ only). Distinct from [catalogMatchFound] since this is on-device,
     * OS-reported data rather than a curated catalog lookup.
     *
     * @param antennaCount number of antennas reported for this device.
     */
    fun android14AntennaDetected(antennaCount: Int)

    /**
     * Retry guidance was shown after a simulated or real unlock/read failure, prompting
     * the user to reposition the phone.
     *
     * @param attemptNumber 1-indexed count of consecutive failures for this session.
     * @param confidence confidence level of the guidance being re-shown.
     */
    fun retryGuidanceShown(attemptNumber: Int, confidence: Confidence)
}
