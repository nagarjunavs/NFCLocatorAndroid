package com.nfclocator.core.data.android14

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcAntennaInfo
import android.os.Build
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "NfcAntennaInfoProvider"

/**
 * Thin OS boundary around `NfcAdapter#getNfcAntennaInfo()`, isolated behind an interface so
 * [Android14AntennaInfoSource] (where the real validation/mapping logic lives) is unit
 * testable without a real `NfcAdapter`, which has no public constructor.
 */
internal fun interface NfcAntennaInfoProvider {
    fun getAntennaInfo(): NfcAntennaInfo?
}

/**
 * Real implementation. Every known OEM failure mode for this API is a null return or a thrown
 * exception (never a checked/typed one) - both are caught here and treated as "unavailable"
 * so the resolver chain falls through to the next source instead of crashing the host app.
 */
internal class SystemNfcAntennaInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: NfcLocatorLogger,
) : NfcAntennaInfoProvider {

    override fun getAntennaInfo(): NfcAntennaInfo? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
        return try {
            val adapter = NfcAdapter.getDefaultAdapter(context) ?: return null
            adapter.getNfcAntennaInfo()
        } catch (e: Exception) {
            logger.w(TAG, "getNfcAntennaInfo() threw on this OEM build", e)
            null
        }
    }
}
