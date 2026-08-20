package com.nfclocator.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.analytics.NfcLocatorAnalytics
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
import com.nfclocator.core.domain.source.AntennaLocationSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResolveAntennaLocationUseCaseTest {

    private val android14Source = mockk<AntennaLocationSource>()
    private val remoteCatalogSource = mockk<AntennaLocationSource>()
    private val seedCatalogSource = mockk<AntennaLocationSource>()
    private val genericFallbackSource = mockk<AntennaLocationSource>()
    private val analytics = mockk<NfcLocatorAnalytics>(relaxed = true)
    private val logger = mockk<NfcLocatorLogger>(relaxed = true)

    private lateinit var useCase: ResolveAntennaLocationUseCase

    private val signals = DeviceIdentitySignals(
        fingerprint = DeviceFingerprint("google", "google", "pixel_8", "shiba", "shiba", null),
        formFactor = FormFactor.BAR,
        foldState = FoldState.NOT_APPLICABLE,
        screenSizeClass = ScreenSizeClass.COMPACT,
        isAndroid14ApiAvailable = true,
    )

    @Before
    fun setUp() {
        useCase = ResolveAntennaLocationUseCase(
            android14Source = android14Source,
            remoteCatalogSource = remoteCatalogSource,
            seedCatalogSource = seedCatalogSource,
            genericFallbackSource = genericFallbackSource,
            analytics = analytics,
            logger = logger,
        )
    }

    @Test
    fun `android14 source wins when it returns a result, remaining sources are not consulted`() = runTest {
        val profile = profile(DataSource.ANDROID14_API, Confidence.EXACT)
        coEvery { android14Source.resolve(signals) } returns profile

        val result = useCase(signals)

        assertThat(result).isEqualTo(profile)
        coVerify(exactly = 0) { remoteCatalogSource.resolve(any()) }
        coVerify(exactly = 0) { seedCatalogSource.resolve(any()) }
        coVerify(exactly = 0) { genericFallbackSource.resolve(any()) }
    }

    @Test
    fun `falls through to remote catalog when android14 source returns null`() = runTest {
        coEvery { android14Source.resolve(signals) } returns null
        val profile = profile(DataSource.REMOTE_CATALOG, Confidence.APPROXIMATE)
        coEvery { remoteCatalogSource.resolve(signals) } returns profile

        val result = useCase(signals)

        assertThat(result).isEqualTo(profile)
        coVerify(exactly = 0) { seedCatalogSource.resolve(any()) }
    }

    @Test
    fun `falls through to seed catalog when android14 and remote both miss`() = runTest {
        coEvery { android14Source.resolve(signals) } returns null
        coEvery { remoteCatalogSource.resolve(signals) } returns null
        val profile = profile(DataSource.SEED_CATALOG, Confidence.APPROXIMATE)
        coEvery { seedCatalogSource.resolve(signals) } returns profile

        val result = useCase(signals)

        assertThat(result).isEqualTo(profile)
        coVerify(exactly = 0) { genericFallbackSource.resolve(any()) }
    }

    @Test
    fun `falls through all the way to generic fallback, which always answers`() = runTest {
        coEvery { android14Source.resolve(signals) } returns null
        coEvery { remoteCatalogSource.resolve(signals) } returns null
        coEvery { seedCatalogSource.resolve(signals) } returns null
        val profile = profile(DataSource.HEURISTIC, Confidence.GENERIC)
        coEvery { genericFallbackSource.resolve(signals) } returns profile

        val result = useCase(signals)

        assertThat(result).isEqualTo(profile)
        assertThat(result.confidence).isEqualTo(Confidence.GENERIC)
    }

    @Test
    fun `a source throwing is treated as a miss and the chain continues`() = runTest {
        coEvery { android14Source.resolve(signals) } throws IllegalStateException("OEM bug")
        val profile = profile(DataSource.REMOTE_CATALOG, Confidence.APPROXIMATE)
        coEvery { remoteCatalogSource.resolve(signals) } returns profile

        val result = useCase(signals)

        assertThat(result).isEqualTo(profile)
    }

    private fun profile(source: DataSource, confidence: Confidence) = DeviceAntennaProfile(
        manufacturer = "google",
        model = "pixel_8",
        formFactor = FormFactor.BAR,
        silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
        antennaZone = NormalizedRect(0.3f, 0.2f, 0.4f, 0.14f),
        confidence = confidence,
        source = source,
        catalogVersion = 1,
        lastVerifiedAt = null,
    )
}
