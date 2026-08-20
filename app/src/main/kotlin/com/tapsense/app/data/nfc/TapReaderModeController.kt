package com.tapsense.app.data.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or
    NfcAdapter.FLAG_READER_NFC_B or
    NfcAdapter.FLAG_READER_NFC_F or
    NfcAdapter.FLAG_READER_NFC_V or
    NfcAdapter.FLAG_READER_NFC_BARCODE

/**
 * Thin OS boundary around `NfcAdapter#enableReaderMode()`/`disableReaderMode()` for the Tap
 * Test screen - the actual real-tag-detection path, not a simulated timer. Guarded the same
 * defensive way as `SystemNfcAntennaInfoProvider` in nfc-locator-core (null adapter, disabled
 * adapter, and any OEM exception are all treated as "can't detect," never a crash), since reader
 * mode is exactly the kind of OEM-variable API that warrants it.
 */
@Singleton
class TapReaderModeController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Starts listening for any NFC tag/reader tap. Returns false if reader mode couldn't start. */
    fun start(activity: Activity, onTagDetected: () -> Unit): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context) ?: return false
        if (!adapter.isEnabled) return false
        return try {
            adapter.enableReaderMode(activity, { _ -> onTagDetected() }, READER_FLAGS, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun stop(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(context) ?: return
        runCatching { adapter.disableReaderMode(activity) }
    }
}
