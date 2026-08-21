package com.tapsense.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** The app's privacy policy, hosted outside the app so it can be updated without a release. */
const val PRIVACY_POLICY_URL = "https://nagarjunavs.github.io/tapsense/android/privacy/"

/**
 * Opens [url] in the user's browser, or does nothing if there's no app installed that can handle it
 * (mirrors [openNfcSettingsSafely]'s no-op-on-`ActivityNotFoundException` behavior).
 */
fun Context.openUrlSafely(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        // No browser available to handle this - silently no-op rather than crash.
    }
}
