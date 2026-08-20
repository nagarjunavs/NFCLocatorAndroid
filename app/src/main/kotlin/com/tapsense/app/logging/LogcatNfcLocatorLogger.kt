package com.tapsense.app.logging

import android.util.Log
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import javax.inject.Inject

/**
 * Demo implementation routing to `android.util.Log`. A real host app would instead forward
 * these calls into whatever logging framework it already ships (Timber, a remote log
 * aggregator, etc) - this class exists purely so the sample app has something concrete to
 * inject.
 */
class LogcatNfcLocatorLogger @Inject constructor() : NfcLocatorLogger {
    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
