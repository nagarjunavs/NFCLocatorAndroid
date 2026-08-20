package com.nfclocator.core.ui.state

import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect

/**
 * What `AntennaLocatorScreen` renders. Deliberately not 1:1 with [com.nfclocator.core.domain.model.DeviceAntennaProfile] -
 * this is a UI-shaped projection so the composable never has to branch on [Confidence] itself;
 * the state variant already encodes which visual treatment applies (see §7 of the project spec).
 */
sealed interface AntennaLocatorUiState {

    data object Loading : AntennaLocatorUiState

    data object Error : AntennaLocatorUiState

    /**
     * A device-specific match with a marker worth trusting: [Confidence.EXACT] (solid marker)
     * or [Confidence.APPROXIMATE] (marker + "approximate" badge + sweep hint). Never built for
     * [Confidence.GENERIC]/[Confidence.UNKNOWN] - use [FallbackGuidance] for those.
     */
    data class ResolvedMarker(
        val formFactor: FormFactor,
        val silhouetteTemplateId: String,
        val antennaZone: NormalizedRect,
        val confidence: Confidence,
        val source: DataSource,
        val isStale: Boolean,
        /** The device's real width/height ratio, when known - see [com.nfclocator.core.domain.model.DeviceAntennaProfile.aspectRatio]. */
        val aspectRatio: Float? = null,
    ) : AntennaLocatorUiState {
        init {
            require(confidence == Confidence.EXACT || confidence == Confidence.APPROXIMATE) {
                "ResolvedMarker must not be built for $confidence; use FallbackGuidance instead"
            }
        }
    }

    /**
     * [Confidence.GENERIC] (known form factor, no device match) or [Confidence.UNKNOWN]
     * (nothing resolved). Always rendered as a guided sweep, never a fixed marker.
     */
    data class FallbackGuidance(
        val formFactor: FormFactor,
        val silhouetteTemplateId: String,
        val approximateZone: NormalizedRect,
        val confidence: Confidence,
        val tipTextResId: Int,
        /** The device's real width/height ratio, when known - see [com.nfclocator.core.domain.model.DeviceAntennaProfile.aspectRatio]. */
        val aspectRatio: Float? = null,
    ) : AntennaLocatorUiState {
        init {
            require(confidence == Confidence.GENERIC || confidence == Confidence.UNKNOWN) {
                "FallbackGuidance must not be built for $confidence; use ResolvedMarker instead"
            }
        }
    }
}

/** True for states where a retry ("simulate unlock failure") flow should offer sweep guidance. */
val AntennaLocatorUiState.isGuidedSweep: Boolean
    get() = this is AntennaLocatorUiState.FallbackGuidance ||
        (this is AntennaLocatorUiState.ResolvedMarker && isStale)
