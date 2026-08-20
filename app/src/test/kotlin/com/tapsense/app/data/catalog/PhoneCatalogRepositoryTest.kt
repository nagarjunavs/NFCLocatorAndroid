package com.tapsense.app.data.catalog

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.data.remote.CatalogRemoteApi
import com.nfclocator.core.data.remote.dto.CatalogEntryDto
import com.nfclocator.core.data.remote.dto.CatalogResponseDto
import com.nfclocator.core.data.remote.dto.SeedCatalogDto
import com.nfclocator.core.data.seed.BundledSeedCatalogLoader
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PhoneCatalogRepositoryTest {

    private val seedLoader = mockk<BundledSeedCatalogLoader>()
    private val remoteApi = mockk<CatalogRemoteApi>()
    private lateinit var repository: PhoneCatalogRepository

    private fun entry(
        manufacturer: String,
        model: String,
        verified: Boolean = false,
    ) = CatalogEntryDto(
        manufacturer = manufacturer,
        model = model,
        formFactor = "BAR",
        silhouetteTemplateId = "silhouette_bar",
        zoneX = 0.3f,
        zoneY = 0.2f,
        zoneWidth = 0.4f,
        zoneHeight = 0.14f,
        catalogVersion = 1,
        verified = verified,
    )

    @Before
    fun setUp() {
        repository = PhoneCatalogRepository(seedLoader, remoteApi)
    }

    @Test
    fun `merges seed and remote entries with no overlap`() = runTest {
        coEvery { seedLoader.load() } returns SeedCatalogDto(1, listOf(entry("google", "pixel 8")))
        coEvery { remoteApi.fetchCatalog(0) } returns CatalogResponseDto(1, listOf(entry("samsung", "sm-s918b")))

        val result = repository.listAll()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.manufacturer }).containsExactly("google", "samsung")
    }

    @Test
    fun `remote entry wins over a seed entry for the same device`() = runTest {
        coEvery { seedLoader.load() } returns SeedCatalogDto(
            1,
            listOf(entry("google", "pixel 8", verified = false)),
        )
        coEvery { remoteApi.fetchCatalog(0) } returns CatalogResponseDto(
            2,
            listOf(entry("google", "pixel 8", verified = true)),
        )

        val result = repository.listAll()

        assertThat(result).hasSize(1)
        assertThat(result.single().source).isEqualTo(DataSource.REMOTE_CATALOG)
        assertThat(result.single().confidence).isEqualTo(Confidence.EXACT)
    }

    @Test
    fun `verified entries resolve to EXACT and unverified to APPROXIMATE`() = runTest {
        coEvery { seedLoader.load() } returns SeedCatalogDto(
            1,
            listOf(entry("google", "pixel 8", verified = true), entry("motorola", "moto g power", verified = false)),
        )
        coEvery { remoteApi.fetchCatalog(0) } returns CatalogResponseDto(1, emptyList())

        val result = repository.listAll().associateBy { it.model }

        assertThat(result.getValue("pixel 8").confidence).isEqualTo(Confidence.EXACT)
        assertThat(result.getValue("moto g power").confidence).isEqualTo(Confidence.APPROXIMATE)
    }

    @Test
    fun `falls back to seed-only when the remote fetch throws`() = runTest {
        coEvery { seedLoader.load() } returns SeedCatalogDto(1, listOf(entry("google", "pixel 8")))
        coEvery { remoteApi.fetchCatalog(0) } throws java.io.IOException("offline")

        val result = repository.listAll()

        assertThat(result).hasSize(1)
        assertThat(result.single().source).isEqualTo(DataSource.SEED_CATALOG)
    }

    @Test
    fun `search filters by manufacturer or model, case-insensitively`() = runTest {
        coEvery { seedLoader.load() } returns SeedCatalogDto(
            1,
            listOf(entry("google", "pixel 8"), entry("samsung", "sm-s918b")),
        )
        coEvery { remoteApi.fetchCatalog(0) } returns CatalogResponseDto(1, emptyList())

        assertThat(repository.search("PIXEL").map { it.model }).containsExactly("pixel 8")
        assertThat(repository.search("samsung").map { it.manufacturer }).containsExactly("samsung")
        assertThat(repository.search("").map { it.model }).containsExactly("pixel 8", "sm-s918b")
        assertThat(repository.search("nokia")).isEmpty()
    }
}
