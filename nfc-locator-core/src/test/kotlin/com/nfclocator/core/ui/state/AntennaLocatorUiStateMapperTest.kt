package com.nfclocator.core.ui.state

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Test

class AntennaLocatorUiStateMapperTest {

    private fun profile(confidence: Confidence, lastVerifiedAt: Instant? = null) = DeviceAntennaProfile(
        manufacturer = "google",
        model = "pixel_8",
        formFactor = FormFactor.BAR,
        silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
        antennaZone = NormalizedRect(0.3f, 0.2f, 0.4f, 0.14f),
        confidence = confidence,
        source = when (confidence) {
            Confidence.EXACT -> DataSource.ANDROID14_API
            Confidence.APPROXIMATE -> DataSource.REMOTE_CATALOG
            Confidence.GENERIC, Confidence.UNKNOWN -> DataSource.HEURISTIC
        },
        catalogVersion = 1,
        lastVerifiedAt = lastVerifiedAt,
    )

    @Test
    fun `EXACT maps to a non-stale ResolvedMarker`() {
        val state = profile(Confidence.EXACT).toUiState()
        assertThat(state).isInstanceOf(AntennaLocatorUiState.ResolvedMarker::class.java)
        assertThat((state as AntennaLocatorUiState.ResolvedMarker).isStale).isFalse()
    }

    @Test
    fun `APPROXIMATE verified recently maps to a non-stale ResolvedMarker`() {
        val recent = Instant.now().minus(10, ChronoUnit.DAYS)
        val state = profile(Confidence.APPROXIMATE, recent).toUiState()
        assertThat((state as AntennaLocatorUiState.ResolvedMarker).isStale).isFalse()
    }

    @Test
    fun `APPROXIMATE verified over 180 days ago maps to a stale ResolvedMarker`() {
        val old = Instant.now().minus(200, ChronoUnit.DAYS)
        val state = profile(Confidence.APPROXIMATE, old).toUiState()
        assertThat((state as AntennaLocatorUiState.ResolvedMarker).isStale).isTrue()
    }

    @Test
    fun `APPROXIMATE with no verification timestamp is treated as stale`() {
        val state = profile(Confidence.APPROXIMATE, lastVerifiedAt = null).toUiState()
        assertThat((state as AntennaLocatorUiState.ResolvedMarker).isStale).isTrue()
    }

    @Test
    fun `GENERIC always maps to FallbackGuidance, never a marker`() {
        val state = profile(Confidence.GENERIC).toUiState()
        assertThat(state).isInstanceOf(AntennaLocatorUiState.FallbackGuidance::class.java)
    }

    @Test
    fun `UNKNOWN always maps to FallbackGuidance, never a marker`() {
        val state = profile(Confidence.UNKNOWN).toUiState()
        assertThat(state).isInstanceOf(AntennaLocatorUiState.FallbackGuidance::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ResolvedMarker cannot be constructed directly for a GENERIC confidence`() {
        AntennaLocatorUiState.ResolvedMarker(
            formFactor = FormFactor.BAR,
            silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
            antennaZone = NormalizedRect(0.3f, 0.2f, 0.4f, 0.14f),
            confidence = Confidence.GENERIC,
            source = DataSource.HEURISTIC,
            isStale = false,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FallbackGuidance cannot be constructed directly for an EXACT confidence`() {
        AntennaLocatorUiState.FallbackGuidance(
            formFactor = FormFactor.BAR,
            silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
            approximateZone = NormalizedRect(0.3f, 0.2f, 0.4f, 0.14f),
            confidence = Confidence.EXACT,
            tipTextResId = 0,
        )
    }
}
