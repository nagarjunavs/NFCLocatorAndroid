package com.nfclocator.core.domain.source

import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import com.nfclocator.core.domain.model.toSilhouetteTemplateId
import javax.inject.Inject

/**
 * Layer 4 of the resolver chain (last resort): a pure device-form-factor heuristic with no
 * I/O and no device-specific data. Maps [FormFactor] (+ fold state, for foldables) to the
 * zone most Android phones of that shape place their antenna in.
 *
 * This is the layer that guarantees the chain always terminates - it never returns null - so
 * the UI can rely on always getting *a* profile, always at [Confidence.GENERIC], meaning the
 * UI must pair it with the guided sweep animation rather than a confident-looking marker.
 */
internal class GenericFallbackSource @Inject constructor() : AntennaLocationSource {

    override suspend fun resolve(signals: DeviceIdentitySignals): DeviceAntennaProfile {
        val zone = zoneFor(signals.formFactor, signals.foldState)
        return DeviceAntennaProfile(
            manufacturer = signals.fingerprint.manufacturer,
            model = signals.fingerprint.model,
            formFactor = signals.formFactor,
            silhouetteTemplateId = signals.formFactor.toSilhouetteTemplateId(signals.foldState),
            antennaZone = zone,
            confidence = Confidence.GENERIC,
            source = DataSource.HEURISTIC,
            catalogVersion = 0,
            lastVerifiedAt = null,
        )
    }

    private fun zoneFor(formFactor: FormFactor, foldState: FoldState): NormalizedRect = when (formFactor) {
        // Most bar phones: upper-center rear, near the main camera bump.
        FormFactor.BAR -> NormalizedRect.centeredSquare(centerX = 0.5f, centerY = 0.22f, side = 0.30f)

        // Tablets commonly center the antenna mid-back regardless of orientation.
        FormFactor.TABLET -> NormalizedRect.centeredSquare(centerX = 0.5f, centerY = 0.45f, side = 0.34f)

        FormFactor.FOLD_BOOK -> when (foldState) {
            // Folded: behaves like a thick bar phone, upper-center rear (cover screen side).
            FoldState.FOLDED -> NormalizedRect.centeredSquare(centerX = 0.5f, centerY = 0.24f, side = 0.34f)
            // Unfolded: antenna typically sits on one inner half, away from the hinge.
            FoldState.UNFOLDED, FoldState.NOT_APPLICABLE ->
                NormalizedRect.centeredSquare(centerX = 0.25f, centerY = 0.5f, side = 0.30f)
        }

        FormFactor.FOLD_FLIP -> when (foldState) {
            // Folded (pocket-sized): center rear, near the cover screen/hinge.
            FoldState.FOLDED -> NormalizedRect.centeredSquare(centerX = 0.5f, centerY = 0.45f, side = 0.34f)
            FoldState.UNFOLDED, FoldState.NOT_APPLICABLE ->
                NormalizedRect.centeredSquare(centerX = 0.5f, centerY = 0.28f, side = 0.32f)
        }
    }
}
