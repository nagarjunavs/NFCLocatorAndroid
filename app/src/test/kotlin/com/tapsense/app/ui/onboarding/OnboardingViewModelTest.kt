package com.tapsense.app.ui.onboarding

import com.google.common.truth.Truth.assertThat
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
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.nfclocator.core.ui.state.toUiState
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.DeviceIdentitySignalsProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val settingsRepository = mockk<TapSenseSettingsRepository>(relaxUnitFun = true)
    private val deviceIdentitySignalsProvider = mockk<DeviceIdentitySignalsProvider>()
    private val resolveAntennaLocationUseCase = mockk<ResolveAntennaLocationUseCase>()
    private val logger = mockk<NfcLocatorLogger>(relaxed = true)

    private val fakeSignals = DeviceIdentitySignals(
        fingerprint = DeviceFingerprint("google", "google", "pixel", "pixel", "pixel", null),
        formFactor = FormFactor.BAR,
        foldState = FoldState.NOT_APPLICABLE,
        screenSizeClass = ScreenSizeClass.COMPACT,
        isAndroid14ApiAvailable = false,
    )
    private val fakeProfile = DeviceAntennaProfile(
        manufacturer = "google",
        model = "pixel 8",
        formFactor = FormFactor.BAR,
        silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
        antennaZone = NormalizedRect(0.3f, 0.1f, 0.4f, 0.15f),
        confidence = Confidence.EXACT,
        source = DataSource.SEED_CATALOG,
        catalogVersion = 1,
        lastVerifiedAt = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { deviceIdentitySignalsProvider.current() } returns fakeSignals
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): OnboardingViewModel {
        val vm = OnboardingViewModel(resolveAntennaLocationUseCase, deviceIdentitySignalsProvider, settingsRepository, logger)
        dispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `resolves an auto-detected preview profile on init`() = runTest(dispatcher) {
        coEvery { resolveAntennaLocationUseCase(any()) } returns fakeProfile

        val vm = viewModel()

        assertThat(vm.autoDetectedProfile.value).isEqualTo(fakeProfile)
    }

    @Test
    fun `a resolver failure leaves the preview null instead of crashing`() = runTest(dispatcher) {
        coEvery { resolveAntennaLocationUseCase(any()) } throws IllegalStateException("boom")

        val vm = viewModel()

        assertThat(vm.autoDetectedProfile.value).isNull()
        assertThat(vm.antennaState.value).isNull()
    }

    @Test
    fun `exposes the same resolved profile as an AntennaLocatorUiState for the real marker`() = runTest(dispatcher) {
        coEvery { resolveAntennaLocationUseCase(any()) } returns fakeProfile

        val vm = viewModel()

        assertThat(vm.antennaState.value).isEqualTo(fakeProfile.toUiState())
        assertThat(vm.antennaState.value).isInstanceOf(AntennaLocatorUiState.ResolvedMarker::class.java)
    }

    @Test
    fun `completeOnboarding persists completion and invokes the callback`() = runTest(dispatcher) {
        coEvery { resolveAntennaLocationUseCase(any()) } returns fakeProfile
        val vm = viewModel()
        var done = false

        vm.completeOnboarding { done = true }
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setOnboardingCompleted(true) }
        assertThat(done).isTrue()
    }
}
