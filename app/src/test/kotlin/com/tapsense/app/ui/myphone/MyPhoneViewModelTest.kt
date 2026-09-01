package com.tapsense.app.ui.myphone

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
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.settings.TapSenseSettings
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.ActiveDeviceSignalsProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyPhoneViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val settingsFlow = MutableStateFlow(TapSenseSettings())
    private val settingsRepository = mockk<TapSenseSettingsRepository> {
        every { settings } returns settingsFlow
    }
    private val nfcStateObserver = mockk<NfcStateObserver> {
        every { isNfcSupported } returns true
    }
    private val activeDeviceSignalsProvider = mockk<ActiveDeviceSignalsProvider>()
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
        manufacturer = "samsung",
        model = "sm-s921b",
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
        every { activeDeviceSignalsProvider.signalsFor(any()) } returns fakeSignals
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): MyPhoneViewModel {
        val vm = MyPhoneViewModel(resolveAntennaLocationUseCase, activeDeviceSignalsProvider, settingsRepository, nfcStateObserver, logger)
        dispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `resolves the current device and exposes a friendly display name`() = runTest(dispatcher) {
        coEvery { resolveAntennaLocationUseCase(any()) } returns fakeProfile

        val vm = viewModel()

        assertThat(vm.uiState.value.isLoading).isFalse()
        assertThat(vm.uiState.value.displayModel).isEqualTo("Galaxy S24")
        assertThat(vm.uiState.value.displayManufacturer).isEqualTo("Samsung")
        assertThat(vm.uiState.value.antennaState).isInstanceOf(AntennaLocatorUiState.ResolvedMarker::class.java)
    }

    @Test
    fun `a resolver failure degrades to an error state instead of crashing`() = runTest(dispatcher) {
        coEvery { resolveAntennaLocationUseCase(any()) } throws IllegalStateException("boom")

        val vm = viewModel()

        assertThat(vm.uiState.value.isLoading).isFalse()
        assertThat(vm.uiState.value.antennaState).isEqualTo(AntennaLocatorUiState.Error)
    }

    @Test
    fun `re-resolves when the selected phone changes`() = runTest(dispatcher) {
        coEvery { resolveAntennaLocationUseCase(any()) } returns fakeProfile
        val vm = viewModel()

        settingsFlow.value = settingsFlow.value.copy(
            selectedPhoneManufacturer = "google",
            selectedPhoneModel = "pixel 8",
            selectedPhoneFormFactor = FormFactor.BAR,
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.uiState.value.isManualOverride).isTrue()
    }
}
