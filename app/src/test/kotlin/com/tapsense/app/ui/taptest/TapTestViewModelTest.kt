package com.tapsense.app.ui.taptest

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceFingerprint
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.ScreenSizeClass
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.nfc.TapReaderModeController
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
class TapTestViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val nfcStateObserver = mockk<NfcStateObserver>()
    private val readerModeController = mockk<TapReaderModeController>()
    private val settingsRepository = mockk<TapSenseSettingsRepository> {
        every { settings } returns MutableStateFlow(TapSenseSettings())
        coEvery { recordTapTestSuccessAndCheckReviewEligibility(any()) } returns false
    }
    private val fakeSignals = DeviceIdentitySignals(
        fingerprint = DeviceFingerprint("google", "google", "pixel", "pixel", "pixel", null),
        formFactor = FormFactor.BAR,
        foldState = FoldState.NOT_APPLICABLE,
        screenSizeClass = ScreenSizeClass.COMPACT,
        isAndroid14ApiAvailable = false,
    )
    private val fakeProfile = DeviceAntennaProfile(
        manufacturer = "google",
        model = "pixel",
        formFactor = FormFactor.BAR,
        silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
        antennaZone = NormalizedRect(0.3f, 0.1f, 0.4f, 0.15f),
        confidence = Confidence.GENERIC,
        source = DataSource.HEURISTIC,
        catalogVersion = 0,
        lastVerifiedAt = null,
    )
    private val activeDeviceSignalsProvider = mockk<ActiveDeviceSignalsProvider>()
    private val resolveAntennaLocationUseCase = mockk<ResolveAntennaLocationUseCase>()
    private val logger = mockk<NfcLocatorLogger>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { activeDeviceSignalsProvider.signalsFor(any()) } returns fakeSignals
        coEvery { resolveAntennaLocationUseCase(any()) } returns fakeProfile
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(nfcSupported: Boolean = true, timeoutMillis: Long = 1_000L): TapTestViewModel {
        every { nfcStateObserver.isNfcSupported } returns nfcSupported
        return TapTestViewModel(
            nfcStateObserver,
            readerModeController,
            settingsRepository,
            resolveAntennaLocationUseCase,
            activeDeviceSignalsProvider,
            logger,
            timeoutMillis,
        )
    }

    @Test
    fun `starts in Ready state before the screen wires reader mode`() {
        val vm = viewModel()
        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.Ready)
    }

    @Test
    fun `no NFC hardware moves straight to NfcUnsupported regardless of reader-mode result`() {
        val vm = viewModel(nfcSupported = false)

        vm.onReaderModeStarted(started = true)

        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.NfcUnsupported)
    }

    @Test
    fun `reader mode failing to start (NFC off) moves to NfcOff`() {
        val vm = viewModel(nfcSupported = true)

        vm.onReaderModeStarted(started = false)

        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.NfcOff)
    }

    @Test
    fun `reader mode starting successfully moves to Detecting`() {
        val vm = viewModel(nfcSupported = true)

        vm.onReaderModeStarted(started = true)

        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.Detecting)
    }

    @Test
    fun `a detected tag moves to Detected and cancels the timeout`() = runTest(dispatcher) {
        val vm = viewModel(timeoutMillis = 1_000L)
        vm.onReaderModeStarted(started = true)

        vm.onTagDetected()
        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.Detected)

        // Advance well past the timeout - it must not fire and clobber Detected.
        dispatcher.scheduler.advanceTimeBy(5_000L)
        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.Detected)
    }

    @Test
    fun `no tag within the timeout window moves to TimedOut`() = runTest(dispatcher) {
        val vm = viewModel(timeoutMillis = 1_000L)
        vm.onReaderModeStarted(started = true)

        dispatcher.scheduler.advanceTimeBy(1_500L)

        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.TimedOut)
    }

    @Test
    fun `retry after a timeout starts detecting again`() = runTest(dispatcher) {
        val vm = viewModel(timeoutMillis = 1_000L)
        vm.onReaderModeStarted(started = true)
        dispatcher.scheduler.advanceTimeBy(1_500L)
        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.TimedOut)

        vm.retry()

        assertThat(vm.uiState.value).isEqualTo(TapTestUiState.Detecting)
    }

    @Test
    fun `a detected tag that reaches review eligibility sets reviewFlowEligible`() = runTest(dispatcher) {
        coEvery { settingsRepository.recordTapTestSuccessAndCheckReviewEligibility(any()) } returns true
        val vm = viewModel()
        vm.onReaderModeStarted(started = true)

        vm.onTagDetected()
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.reviewFlowEligible.value).isTrue()
    }

    @Test
    fun `a detected tag that is not yet eligible leaves reviewFlowEligible false`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onReaderModeStarted(started = true)

        vm.onTagDetected()
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.reviewFlowEligible.value).isFalse()
    }

    @Test
    fun `onReviewFlowRequested consumes the eligibility signal`() = runTest(dispatcher) {
        coEvery { settingsRepository.recordTapTestSuccessAndCheckReviewEligibility(any()) } returns true
        val vm = viewModel()
        vm.onReaderModeStarted(started = true)
        vm.onTagDetected()
        dispatcher.scheduler.advanceUntilIdle()
        assertThat(vm.reviewFlowEligible.value).isTrue()

        vm.onReviewFlowRequested()

        assertThat(vm.reviewFlowEligible.value).isFalse()
    }
}
