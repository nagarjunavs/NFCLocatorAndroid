package com.nfclocator.core.domain.logging

/**
 * Logging sink the host app supplies (bound via Hilt in the host's own DI graph).
 *
 * The library never bundles a concrete logging framework (Timber, android.util.Log, etc.) -
 * it only calls through this interface so logs land in whatever pipeline the host already
 * ships to (e.g. remote crash/log aggregation).
 */
interface NfcLocatorLogger {
    fun d(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
