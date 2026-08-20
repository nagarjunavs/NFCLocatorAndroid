package com.nfclocator.core.ui.state

import com.nfclocator.core.R
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.FormFactor
import java.time.Instant
import java.time.temporal.ChronoUnit

/** A catalog entry not re-verified within this window is shown with a "stale" hint. */
private const val STALE_AFTER_DAYS = 180L

/**
 * Maps a resolved [DeviceAntennaProfile] to the UI-shaped [AntennaLocatorUiState].
 * This is the one place confidence decides which visual treatment applies - see §7 of the
 * project spec: [Confidence.EXACT] and [Confidence.APPROXIMATE] get a marker (approximate
 * additionally flagged stale past [STALE_AFTER_DAYS]); [Confidence.GENERIC] and
 * [Confidence.UNKNOWN] always get guided-sweep fallback, never a marker.
 */
fun DeviceAntennaProfile.toUiState(now: Instant = Instant.now()): AntennaLocatorUiState = when (confidence) {
    Confidence.EXACT, Confidence.APPROXIMATE -> AntennaLocatorUiState.ResolvedMarker(
        formFactor = formFactor,
        silhouetteTemplateId = silhouetteTemplateId,
        antennaZone = antennaZone,
        confidence = confidence,
        source = source,
        isStale = isStale(now),
        aspectRatio = aspectRatio,
    )

    Confidence.GENERIC, Confidence.UNKNOWN -> AntennaLocatorUiState.FallbackGuidance(
        formFactor = formFactor,
        silhouetteTemplateId = silhouetteTemplateId,
        approximateZone = antennaZone,
        confidence = confidence,
        tipTextResId = tipTextResIdFor(confidence, formFactor),
        aspectRatio = aspectRatio,
    )
}

private fun DeviceAntennaProfile.isStale(now: Instant): Boolean {
    if (confidence != Confidence.APPROXIMATE) return false
    val verifiedAt = lastVerifiedAt ?: return true
    return ChronoUnit.DAYS.between(verifiedAt, now) > STALE_AFTER_DAYS
}

private fun tipTextResIdFor(confidence: Confidence, formFactor: FormFactor): Int = when (confidence) {
    Confidence.UNKNOWN -> R.string.nfc_locator_sweep_unknown_tip
    Confidence.GENERIC -> when (formFactor) {
        FormFactor.BAR -> R.string.nfc_locator_sweep_bar_tip
        FormFactor.FOLD_BOOK -> R.string.nfc_locator_sweep_fold_book_tip
        FormFactor.FOLD_FLIP -> R.string.nfc_locator_sweep_fold_flip_tip
        FormFactor.TABLET -> R.string.nfc_locator_sweep_tablet_tip
    }
    Confidence.EXACT, Confidence.APPROXIMATE -> R.string.nfc_locator_sweep_generic_tip
}
