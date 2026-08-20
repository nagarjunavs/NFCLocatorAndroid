package com.nfclocator.core.data.seed

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.data.remote.dto.CatalogEntryDto
import com.nfclocator.core.data.remote.dto.SeedCatalogDto
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceFingerprint
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.ScreenSizeClass
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BundledSeedCatalogSourceTest {

    private val loader = mockk<BundledSeedCatalogLoader>()
    private val logger = mockk<NfcLocatorLogger>(relaxed = true)
    private val source = BundledSeedCatalogSource(loader, logger)

    private fun signalsFor(manufacturer: String, model: String) = DeviceIdentitySignals(
        fingerprint = DeviceFingerprint(manufacturer, manufacturer, model, model, model, null),
        formFactor = FormFactor.BAR,
        foldState = FoldState.NOT_APPLICABLE,
        screenSizeClass = ScreenSizeClass.COMPACT,
        isAndroid14ApiAvailable = false,
    )

    private val seedEntry = CatalogEntryDto(
        manufacturer = "google",
        model = "pixel 8",
        formFactor = "BAR",
        silhouetteTemplateId = "silhouette_bar",
        zoneX = 0.3f,
        zoneY = 0.16f,
        zoneWidth = 0.4f,
        zoneHeight = 0.14f,
        catalogVersion = 1,
    )

    @Test
    fun `matches a known device from the bundled seed catalog`() = runTest {
        coEvery { loader.load() } returns SeedCatalogDto(catalogVersion = 1, entries = listOf(seedEntry))

        val result = source.resolve(signalsFor("google", "pixel_8"))

        assertThat(result).isNotNull()
        assertThat(result!!.confidence).isEqualTo(Confidence.APPROXIMATE)
        assertThat(result.source).isEqualTo(DataSource.SEED_CATALOG)
    }

    @Test
    fun `returns null for a device not present in the seed catalog`() = runTest {
        coEvery { loader.load() } returns SeedCatalogDto(catalogVersion = 1, entries = listOf(seedEntry))

        val result = source.resolve(signalsFor("acme", "widget_9000"))

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when the seed catalog is empty (asset failed to load)`() = runTest {
        coEvery { loader.load() } returns SeedCatalogDto(catalogVersion = 0, entries = emptyList())

        val result = source.resolve(signalsFor("google", "pixel_8"))

        assertThat(result).isNull()
    }

    @Test
    fun `skips an invalid seed entry rather than crashing the lookup`() = runTest {
        val broken = seedEntry.copy(formFactor = "NOT_REAL")
        coEvery { loader.load() } returns SeedCatalogDto(catalogVersion = 1, entries = listOf(broken))

        val result = source.resolve(signalsFor("google", "pixel_8"))

        assertThat(result).isNull()
    }
}
