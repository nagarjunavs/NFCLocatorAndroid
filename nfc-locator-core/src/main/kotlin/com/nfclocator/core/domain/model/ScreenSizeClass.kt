package com.nfclocator.core.domain.model

/**
 * Coarse screen size bucket used only by [com.nfclocator.core.domain.source.GenericFallbackSource]
 * to distinguish a phone-sized silhouette from a tablet-sized one when no device-specific
 * match exists. Not a replacement for [FormFactor]; foldables report their own form factor
 * regardless of size class.
 */
enum class ScreenSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

/**
 * Everything the resolver chain needs about the current device to attempt a match.
 *
 * [formFactor] and [foldState] are supplied by the host, which is free to use whatever
 * signal it already has available (androidx.window `FoldingFeature`, a hinge-angle sensor,
 * or a static best guess) - the library intentionally does not bundle a window-manager
 * dependency to compute this itself, matching the "inject, don't own" rule for anything
 * outside its core resolver-chain responsibility.
 */
data class DeviceIdentitySignals(
    val fingerprint: DeviceFingerprint,
    val formFactor: FormFactor,
    val foldState: FoldState,
    val screenSizeClass: ScreenSizeClass,
    val isAndroid14ApiAvailable: Boolean,
)
