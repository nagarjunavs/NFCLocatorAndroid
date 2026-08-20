package com.nfclocator.core.domain.model

/**
 * How much a [DeviceAntennaProfile] should be trusted by the UI.
 *
 * Ordered from strongest to weakest signal. The UI must never render a solid,
 * confident-looking marker for [GENERIC] or [UNKNOWN] - see `ConfidenceBadge`
 * and `AntennaSilhouette` for the rendering rules tied to each level.
 */
enum class Confidence {
    /** Reported directly by the OS/hardware for this exact device (Android 14+ AntennaInfo). */
    EXACT,

    /** Matched against a curated catalog entry (remote or bundled seed) for this exact device. */
    APPROXIMATE,

    /** No device-specific match; derived from form factor / screen size heuristics only. */
    GENERIC,

    /** No signal could be resolved at all. */
    UNKNOWN,
}
