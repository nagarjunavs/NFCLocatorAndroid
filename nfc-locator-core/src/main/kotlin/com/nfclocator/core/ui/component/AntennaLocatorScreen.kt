package com.nfclocator.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nfclocator.core.R
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.ui.state.AntennaLocatorUiState

/**
 * Caps how tall the silhouette can grow. Without this, a tall/narrow template (e.g. a bar
 * phone's ~0.5 aspect ratio) inside this screen's weighted layout can expand to consume
 * nearly all available height, leaving no room for the hint/tip text below it.
 */
private val MAX_SILHOUETTE_HEIGHT = 340.dp

/**
 * Top-level guidance screen. Stateless by design (state hoisted, no `ViewModel` dependency in
 * this module) so the host app owns the actual lifecycle-aware state holder and can wire this
 * into whatever navigation/DI shape it already uses.
 *
 * Confidence-aware rendering is the whole point of this composable: [AntennaLocatorUiState.ResolvedMarker]
 * gets a marker (solid for [Confidence.EXACT], badge-flagged for [Confidence.APPROXIMATE]),
 * [AntennaLocatorUiState.FallbackGuidance] always gets [GuidedSweepAnimation] instead - see the
 * non-negotiable rule in the project spec against showing a confident marker for a guess.
 */
@Composable
fun AntennaLocatorScreen(
    state: AntennaLocatorUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.nfc_locator_screen_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        when (state) {
            AntennaLocatorUiState.Loading -> LoadingContent()
            AntennaLocatorUiState.Error -> ErrorContent(onRetry)
            is AntennaLocatorUiState.ResolvedMarker -> ResolvedMarkerContent(state, reducedMotion)
            is AntennaLocatorUiState.FallbackGuidance -> FallbackGuidanceContent(state, reducedMotion)
        }
    }
}

@Composable
private fun LoadingContent() {
    val description = stringResource(R.string.nfc_locator_loading_content_description)
    CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Text(stringResource(R.string.nfc_locator_error_title), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.nfc_locator_error_body), style = MaterialTheme.typography.bodyMedium)
    Button(onClick = onRetry) {
        Text(stringResource(R.string.nfc_locator_retry_button))
    }
}

@Composable
private fun ResolvedMarkerContent(state: AntennaLocatorUiState.ResolvedMarker, reducedMotion: Boolean) {
    ConfidenceBadge(state.confidence)

    val markerDescription = stringResource(R.string.nfc_locator_marker_content_description)
    AntennaSilhouette(
        templateId = state.silhouetteTemplateId,
        zone = state.antennaZone,
        // ResolvedMarker only ever carries EXACT or APPROXIMATE confidence (see its init
        // check), and isStale is only ever true for an APPROXIMATE entry past the staleness
        // window - so "not stale" is exactly "still trustworthy enough for a solid marker."
        isConfident = !state.isStale,
        modifier = Modifier
            .fillMaxWidth()
            .height(MAX_SILHOUETTE_HEIGHT)
            .semantics { contentDescription = markerDescription },
        reducedMotion = reducedMotion,
        aspectRatioOverride = state.aspectRatio,
    )

    val hint = when {
        state.isStale -> stringResource(R.string.nfc_locator_marker_stale_hint)
        state.confidence == Confidence.APPROXIMATE -> stringResource(R.string.nfc_locator_marker_approximate_hint)
        // EXACT can come from either on-device measurement or a vendor/community-verified
        // catalog entry (see CatalogEntryDto.verified) - both earn a solid marker, but the copy
        // is honest about which kind of "exact" this is.
        state.source == DataSource.ANDROID14_API -> stringResource(R.string.nfc_locator_marker_exact_hint)
        else -> stringResource(R.string.nfc_locator_marker_verified_hint)
    }
    Text(hint, style = MaterialTheme.typography.bodyMedium)

    if (state.isStale) {
        GuidedSweepAnimation(
            templateId = state.silhouetteTemplateId,
            zone = state.antennaZone,
            modifier = Modifier
                .fillMaxWidth()
                .height(MAX_SILHOUETTE_HEIGHT),
            reducedMotion = reducedMotion,
            aspectRatioOverride = state.aspectRatio,
        )
    }
}

@Composable
private fun FallbackGuidanceContent(state: AntennaLocatorUiState.FallbackGuidance, reducedMotion: Boolean) {
    ConfidenceBadge(state.confidence)

    GuidedSweepAnimation(
        templateId = state.silhouetteTemplateId,
        zone = state.approximateZone,
        modifier = Modifier
            .fillMaxWidth()
            .height(MAX_SILHOUETTE_HEIGHT),
        reducedMotion = reducedMotion,
        aspectRatioOverride = state.aspectRatio,
    )

    Text(stringResource(state.tipTextResId), style = MaterialTheme.typography.bodyMedium)
}
