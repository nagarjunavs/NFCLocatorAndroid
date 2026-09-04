package com.tapsense.app.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.tapsense.app.BuildConfig

private const val TAG = "InAppReview"

/**
 * Fires the Play In-App Review flow from [this] activity. Fire-and-forget by design, per Google's
 * own guidance: the API never reveals whether the dialog was actually shown or whether the user
 * reviewed (it applies its own undisclosed quota - most calls are silent no-ops), and a failed
 * request must never change the app's normal flow. So there is nothing meaningful to return to
 * the caller either way; this only decides *when to ask*, not whether the OS agrees to show it.
 * Both outcomes are still logged (never surfaced to the user) so a real failure is diagnosable
 * instead of a silent black box - see the "Common gotchas" table at
 * https://developer.android.com/guide/playcore/in-app-review/test.
 *
 * Call sites are responsible for their own "don't ask too often" gating (see
 * [com.tapsense.app.data.settings.TapSenseSettingsRepository.recordTapTestSuccessAndCheckReviewEligibility])
 * - unlike [openPlayStoreListingSafely], which a user can trigger from Settings as often as they like.
 *
 * The real [ReviewManagerFactory]-created manager only ever succeeds for a build installed
 * *through* Google Play (an internal test track, closed/open testing, or production, with that
 * Google account as the Play Store's primary account and no existing review) - never a debug
 * build run from Android Studio, `adb install`, or even a sideloaded release APK; on any of those
 * `requestReviewFlow()` fails every time, regardless of how many times this is called or how the
 * app decided to call it. [FakeReviewManager] can't render the real dialog either (it only fakes
 * a successful `Task` result, per Google's docs), but swapping to it for [BuildConfig.DEBUG]
 * lets the logs below confirm the trigger/eligibility logic itself reached `launchReviewFlow`
 * correctly, isolating "nothing shows" during local testing to that Play-install requirement
 * rather than a bug in this app's own gating.
 */
fun Activity.requestInAppReviewSafely() {
    val manager: ReviewManager = if (BuildConfig.DEBUG) FakeReviewManager(this) else ReviewManagerFactory.create(this)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.i(TAG, "requestReviewFlow succeeded, launching review flow")
            manager.launchReviewFlow(this, task.result)
        } else {
            // Expected on any build not installed through Play (see the doc link above) - never
            // surfaced to the user or allowed to change the app's flow, only logged.
            Log.w(TAG, "requestReviewFlow failed - not installed through Play, or quota declined", task.exception)
        }
    }
}
