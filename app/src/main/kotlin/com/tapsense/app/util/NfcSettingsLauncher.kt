package com.tapsense.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Opens the system NFC settings screen, or does nothing if there isn't one to open.
 *
 * `Settings.ACTION_NFC_SETTINGS` only resolves to an activity on devices that actually have NFC
 * hardware - on a device with none (no `NfcAdapter` at all), the OS has nothing to configure and
 * this intent has no target, so `startActivity` throws `ActivityNotFoundException` instead of
 * failing gracefully. Every call site should also avoid *offering* this action when
 * `NfcStateObserver.isNfcSupported` is false, but this catch is the last line of defense against
 * the crash regardless of how the call site decided to show the option.
 */
fun Context.openNfcSettingsSafely() {
    try {
        startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
    } catch (e: ActivityNotFoundException) {
        // Nothing to open on this device - silently no-op rather than crash. Call sites are
        // expected to not offer this action at all when NFC hardware isn't present.
    }
}
