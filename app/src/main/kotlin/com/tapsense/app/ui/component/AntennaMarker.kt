package com.tapsense.app.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nfclocator.core.ui.component.AntennaSilhouette
import com.nfclocator.core.ui.component.GuidedSweepAnimation
import com.nfclocator.core.ui.state.AntennaLocatorUiState

/**
 * Renders the antenna-location marker for a resolved [AntennaLocatorUiState], mirroring
 * `AntennaLocatorScreen`'s own ResolvedMarkerContent/FallbackGuidanceContent branching so every
 * screen in this app treats confidence the same way: a solid marker only for a non-stale
 * [AntennaLocatorUiState.ResolvedMarker], [GuidedSweepAnimation]'s moving highlight for every
 * other case (a stale approximate match, or [AntennaLocatorUiState.FallbackGuidance]) - never a
 * static marker for a guess.
 *
 * [silhouetteColor] is the solid device-shape fill, distinct from the marker's own confidence
 * accent - callers pick it per their own background context (see call sites).
 */
@Composable
fun AntennaMarker(
    state: AntennaLocatorUiState,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    silhouetteColor: Color = MaterialTheme.colorScheme.outline,
    showCameraBump: Boolean = false,
    cameraBumpColor: Color = silhouetteColor,
    silhouetteBorderColor: Color? = null,
) {
    when (state) {
        is AntennaLocatorUiState.ResolvedMarker -> if (state.isStale) {
            GuidedSweepAnimation(
                templateId = state.silhouetteTemplateId,
                zone = state.antennaZone,
                reducedMotion = reducedMotion,
                modifier = modifier,
                silhouetteColor = silhouetteColor,
                aspectRatioOverride = state.aspectRatio,
                showCameraBump = showCameraBump,
                cameraBumpColor = cameraBumpColor,
                silhouetteBorderColor = silhouetteBorderColor,
            )
        } else {
            AntennaSilhouette(
                templateId = state.silhouetteTemplateId,
                zone = state.antennaZone,
                isConfident = true,
                reducedMotion = reducedMotion,
                modifier = modifier,
                silhouetteColor = silhouetteColor,
                aspectRatioOverride = state.aspectRatio,
                showCameraBump = showCameraBump,
                cameraBumpColor = cameraBumpColor,
                silhouetteBorderColor = silhouetteBorderColor,
            )
        }
        is AntennaLocatorUiState.FallbackGuidance -> GuidedSweepAnimation(
            templateId = state.silhouetteTemplateId,
            zone = state.approximateZone,
            reducedMotion = reducedMotion,
            modifier = modifier,
            silhouetteColor = silhouetteColor,
            aspectRatioOverride = state.aspectRatio,
            showCameraBump = showCameraBump,
            cameraBumpColor = cameraBumpColor,
            silhouetteBorderColor = silhouetteBorderColor,
        )
        else -> Unit
    }
}
