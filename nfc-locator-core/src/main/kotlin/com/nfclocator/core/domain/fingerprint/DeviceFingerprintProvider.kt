package com.nfclocator.core.domain.fingerprint

import com.nfclocator.core.domain.model.DeviceFingerprint

/** Builds the current device's [DeviceFingerprint]. Kept as an interface so tests can supply fixed values. */
fun interface DeviceFingerprintProvider {
    fun current(): DeviceFingerprint
}
