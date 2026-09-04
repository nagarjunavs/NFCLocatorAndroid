package com.tapsense.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tapsense.app.BuildConfig

/** The app's privacy policy, hosted outside the app so it can be updated without a release. */
const val PRIVACY_POLICY_URL = "https://nagarjunavs.github.io/tapsense/android/privacy/"

/** Support inbox for tester/user feedback - the only feedback channel the app offers. */
const val SUPPORT_EMAIL = "nagarjunavs.dev@gmail.com"

/**
 * The published Play Store package id - deliberately not [BuildConfig.APPLICATION_ID]. Debug
 * builds suffix that with ".debug" (see `applicationIdSuffix` in build.gradle.kts), which has no
 * Play Store listing at all, so a debug build would otherwise send the "Rate" button to a listing
 * that doesn't exist.
 */
private const val PLAY_STORE_PACKAGE_ID = "com.tapsense.app"

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

/**
 * Opens the user's email app with a draft addressed to [SUPPORT_EMAIL], subject pre-filled with
 * the app version for easier triage. Nothing is collected or sent by the app itself - this only
 * launches an editable draft in the user's own mail client, which they can edit or discard freely
 * before choosing to send it, same as tapping a "mailto:" link on a website.
 *
 * Recipient/subject are passed as [Intent.EXTRA_EMAIL]/[Intent.EXTRA_SUBJECT] rather than encoded
 * into the `mailto:` URI itself - `Uri.parse("mailto:$email").buildUpon().appendQueryParameter(...)`
 * silently drops the address, since a bare `mailto:foo@bar.com` URI is opaque (no `//` authority)
 * and `appendQueryParameter` on an opaque `Uri.Builder` replaces the scheme-specific part instead
 * of appending to it. `data = Uri.parse("mailto:")` (scheme only) keeps intent resolution scoped
 * to mail apps only, same as before.
 */
fun Context.sendFeedbackEmailSafely() {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "TapSense feedback (${BuildConfig.VERSION_NAME})")
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No email app available to handle this - silently no-op rather than crash.
    }
}

/**
 * Opens this app's Play Store listing - used by the Settings screen's "Rate" row, a direct,
 * always-available path to leave a review (unlike [requestInAppReviewSafely], which Google's own
 * quota may silently decline to show). Prefers a `market:` URI, which the Play Store app resolves
 * directly to the review tab without a browser hop; falls back to the web listing via
 * [openUrlSafely] if the Play Store app isn't installed (e.g. some emulator images).
 */
fun Context.openPlayStoreListingSafely() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PLAY_STORE_PACKAGE_ID")))
    } catch (e: ActivityNotFoundException) {
        openUrlSafely("https://play.google.com/store/apps/details?id=$PLAY_STORE_PACKAGE_ID")
    }
}
