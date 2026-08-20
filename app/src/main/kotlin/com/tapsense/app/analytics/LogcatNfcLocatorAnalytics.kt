package com.tapsense.app.analytics

import android.util.Log
import com.nfclocator.core.domain.analytics.NfcLocatorAnalytics
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import javax.inject.Inject

private const val TAG = "NfcLocatorAnalytics"

/**
 * Demo implementation that just logs events. A real host app would forward these into its
 * existing analytics pipeline (the whole point of [NfcLocatorAnalytics] being an interface
 * the library never implements itself).
 */
class LogcatNfcLocatorAnalytics @Inject constructor() : NfcLocatorAnalytics {

    override fun guidanceShown(confidence: Confidence, source: DataSource, formFactor: String) {
        Log.i(TAG, "guidance_shown confidence=$confidence source=$source formFactor=$formFactor")
    }

    override fun guidanceDismissed(confidence: Confidence, timeVisibleMs: Long) {
        Log.i(TAG, "guidance_dismissed confidence=$confidence timeVisibleMs=$timeVisibleMs")
    }

    override fun unknownDeviceDetected(manufacturer: String, formFactorGuess: String) {
        Log.i(TAG, "unknown_device_detected manufacturer=$manufacturer formFactorGuess=$formFactorGuess")
    }

    override fun catalogMatchFound(confidence: Confidence, source: DataSource, catalogVersion: Int) {
        Log.i(TAG, "catalog_match_found confidence=$confidence source=$source catalogVersion=$catalogVersion")
    }

    override fun android14AntennaDetected(antennaCount: Int) {
        Log.i(TAG, "android14_antenna_detected antennaCount=$antennaCount")
    }

    override fun retryGuidanceShown(attemptNumber: Int, confidence: Confidence) {
        Log.i(TAG, "retry_guidance_shown attemptNumber=$attemptNumber confidence=$confidence")
    }
}
