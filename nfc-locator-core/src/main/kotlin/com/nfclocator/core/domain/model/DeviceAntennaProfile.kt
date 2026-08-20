package com.nfclocator.core.domain.model

import java.time.Instant

/**
 * A resolved (or heuristically guessed) NFC antenna location for a device.
 *
 * This is never presented to the UI without [confidence] and [source] attached -
 * see the class-level rule in the project spec: never silently present a guess as fact.
 */
data class DeviceAntennaProfile(
    val manufacturer: String,
    val model: String,
    val formFactor: FormFactor,
    val silhouetteTemplateId: String,
    val antennaZone: NormalizedRect,
    val confidence: Confidence,
    val source: DataSource,
    val catalogVersion: Int,
    val lastVerifiedAt: Instant?,
    /**
     * The device's real width/height ratio, when known - lets the UI fit the silhouette to the
     * device's actual proportions instead of [silhouetteTemplateId]'s fixed per-bucket ratio.
     * Null when no real dimension is available (most catalog/heuristic sources), in which case
     * callers fall back to the template's own ratio.
     */
    val aspectRatio: Float? = null,
) {
    companion object {
        /** minSdk-safe id for the generic bar-phone silhouette; every form factor maps to one. */
        const val TEMPLATE_BAR = "silhouette_bar"
        const val TEMPLATE_FOLD_BOOK_OPEN = "silhouette_fold_book_open"
        const val TEMPLATE_FOLD_BOOK_CLOSED = "silhouette_fold_book_closed"
        const val TEMPLATE_FOLD_FLIP_OPEN = "silhouette_fold_flip_open"
        const val TEMPLATE_FOLD_FLIP_CLOSED = "silhouette_fold_flip_closed"
        const val TEMPLATE_TABLET = "silhouette_tablet"
    }
}
