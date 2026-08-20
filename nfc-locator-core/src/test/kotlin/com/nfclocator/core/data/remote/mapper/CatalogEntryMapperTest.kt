package com.nfclocator.core.data.remote.mapper

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.data.remote.dto.CatalogEntryDto
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import org.junit.Test

class CatalogEntryMapperTest {

    private fun validDto() = CatalogEntryDto(
        manufacturer = "google",
        model = "pixel 8",
        formFactor = "BAR",
        silhouetteTemplateId = "silhouette_bar",
        zoneX = 0.3f,
        zoneY = 0.2f,
        zoneWidth = 0.4f,
        zoneHeight = 0.14f,
        catalogVersion = 1,
        lastVerifiedAtEpochMs = 1_700_000_000_000L,
    )

    @Test
    fun `maps an unverified entry to a domain profile with APPROXIMATE confidence`() {
        val result = validDto().toDomainOrNull(DataSource.REMOTE_CATALOG)

        assertThat(result).isNotNull()
        assertThat(result!!.confidence).isEqualTo(Confidence.APPROXIMATE)
        assertThat(result.source).isEqualTo(DataSource.REMOTE_CATALOG)
        assertThat(result.formFactor.name).isEqualTo("BAR")
    }

    @Test
    fun `maps a verified entry to a domain profile with EXACT confidence`() {
        val result = validDto().copy(verified = true).toDomainOrNull(DataSource.REMOTE_CATALOG)

        assertThat(result).isNotNull()
        assertThat(result!!.confidence).isEqualTo(Confidence.EXACT)
        assertThat(result.source).isEqualTo(DataSource.REMOTE_CATALOG)
    }

    @Test
    fun `verified defaults to false when omitted`() {
        val result = validDto().toDomainOrNull(DataSource.SEED_CATALOG)
        assertThat(result!!.confidence).isEqualTo(Confidence.APPROXIMATE)
    }

    @Test
    fun `rejects an unrecognized form factor string`() {
        val result = validDto().copy(formFactor = "SPHERE").toDomainOrNull(DataSource.REMOTE_CATALOG)
        assertThat(result).isNull()
    }

    @Test
    fun `rejects a zone rect that exceeds normalized bounds`() {
        val result = validDto().copy(zoneWidth = 1.5f).toDomainOrNull(DataSource.REMOTE_CATALOG)
        assertThat(result).isNull()
    }

    @Test
    fun `rejects a negative zone coordinate`() {
        val result = validDto().copy(zoneX = -0.1f).toDomainOrNull(DataSource.REMOTE_CATALOG)
        assertThat(result).isNull()
    }

    @Test
    fun `rejects a blank manufacturer or model`() {
        assertThat(validDto().copy(manufacturer = "").toDomainOrNull(DataSource.REMOTE_CATALOG)).isNull()
        assertThat(validDto().copy(model = "  ").toDomainOrNull(DataSource.REMOTE_CATALOG)).isNull()
    }

    @Test
    fun `rejects a negative catalog version`() {
        val result = validDto().copy(catalogVersion = -1).toDomainOrNull(DataSource.REMOTE_CATALOG)
        assertThat(result).isNull()
    }

    @Test
    fun `lookupKey normalizes manufacturer and model consistently with DeviceFingerprint`() {
        val key = validDto().lookupKey()
        assertThat(key).isEqualTo("google:pixel_8")
    }
}
