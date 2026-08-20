package com.nfclocator.core.data.remote

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.data.remote.dto.CatalogEntryDto
import com.nfclocator.core.data.remote.dto.CatalogResponseDto
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceFingerprint
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import com.nfclocator.core.domain.model.ScreenSizeClass
import com.nfclocator.core.domain.repository.CatalogCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RemoteCatalogSourceTest {

    private val remoteApi = mockk<CatalogRemoteApi>()
    private val cache = mockk<CatalogCache>()
    private val logger = mockk<NfcLocatorLogger>(relaxed = true)
    private val source = RemoteCatalogSource(remoteApi, cache, logger)

    private val signals = DeviceIdentitySignals(
        fingerprint = DeviceFingerprint("google", "google", "pixel_8", "shiba", "shiba", null),
        formFactor = FormFactor.BAR,
        foldState = FoldState.NOT_APPLICABLE,
        screenSizeClass = ScreenSizeClass.COMPACT,
        isAndroid14ApiAvailable = false,
    )

    private val cachedProfile = DeviceAntennaProfile(
        manufacturer = "google",
        model = "pixel_8",
        formFactor = FormFactor.BAR,
        silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
        antennaZone = NormalizedRect(0.3f, 0.2f, 0.4f, 0.14f),
        confidence = Confidence.APPROXIMATE,
        source = DataSource.REMOTE_CATALOG,
        catalogVersion = 1,
        lastVerifiedAt = null,
    )

    @Test
    fun `returns a cache hit without calling the remote api`() = runTest {
        coEvery { cache.find(signals.fingerprint.lookupKeys()) } returns cachedProfile

        val result = source.resolve(signals)

        assertThat(result).isEqualTo(cachedProfile)
        coVerify(exactly = 0) { remoteApi.fetchCatalog(any()) }
    }

    @Test
    fun `on cache miss, fetches from remote, persists, and re-reads the cache`() = runTest {
        coEvery { cache.find(signals.fingerprint.lookupKeys()) } returnsMany listOf(null, cachedProfile)
        coEvery { cache.latestCachedVersion() } returns 0
        coEvery { remoteApi.fetchCatalog(0) } returns CatalogResponseDto(
            catalogVersion = 1,
            entries = listOf(
                CatalogEntryDto(
                    manufacturer = "google",
                    model = "pixel 8",
                    formFactor = "BAR",
                    silhouetteTemplateId = "silhouette_bar",
                    zoneX = 0.3f,
                    zoneY = 0.2f,
                    zoneWidth = 0.4f,
                    zoneHeight = 0.14f,
                    catalogVersion = 1,
                ),
            ),
        )
        coEvery { cache.upsertAll(any()) } returns Unit

        val result = source.resolve(signals)

        assertThat(result).isEqualTo(cachedProfile)
        coVerify(exactly = 1) { cache.upsertAll(any()) }
    }

    @Test
    fun `returns null when both cache and remote miss`() = runTest {
        coEvery { cache.find(signals.fingerprint.lookupKeys()) } returns null
        coEvery { cache.latestCachedVersion() } returns 0
        coEvery { remoteApi.fetchCatalog(0) } returns CatalogResponseDto(catalogVersion = 0, entries = emptyList())
        coEvery { cache.upsertAll(any()) } returns Unit

        val result = source.resolve(signals)

        assertThat(result).isNull()
    }

    @Test
    fun `treats a network failure as unavailable and falls through rather than throwing`() = runTest {
        coEvery { cache.find(signals.fingerprint.lookupKeys()) } returns null
        coEvery { cache.latestCachedVersion() } returns 0
        coEvery { remoteApi.fetchCatalog(0) } throws java.io.IOException("no network")

        val result = source.resolve(signals)

        assertThat(result).isNull()
    }

    @Test
    fun `drops an invalid remote entry without failing the whole refresh`() = runTest {
        coEvery { cache.find(signals.fingerprint.lookupKeys()) } returns null
        coEvery { cache.latestCachedVersion() } returns 0
        coEvery { remoteApi.fetchCatalog(0) } returns CatalogResponseDto(
            catalogVersion = 1,
            entries = listOf(
                CatalogEntryDto(
                    manufacturer = "broken",
                    model = "entry",
                    formFactor = "NOT_A_REAL_FORM_FACTOR",
                    silhouetteTemplateId = "silhouette_bar",
                    zoneX = 0f,
                    zoneY = 0f,
                    zoneWidth = 0.1f,
                    zoneHeight = 0.1f,
                    catalogVersion = 1,
                ),
            ),
        )
        var upserted: List<Pair<String, DeviceAntennaProfile>>? = null
        coEvery { cache.upsertAll(any()) } answers { upserted = firstArg() }

        source.resolve(signals)

        assertThat(upserted).isEmpty()
    }
}
