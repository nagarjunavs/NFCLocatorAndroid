package com.tapsense.app.ui.settings

import com.google.common.truth.Truth.assertThat
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.settings.AppearanceMode
import com.tapsense.app.data.settings.TapSenseSettings
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val settingsFlow = MutableStateFlow(TapSenseSettings())
    private val nfcEnabledFlow = MutableStateFlow(false)
    private val settingsRepository = mockk<TapSenseSettingsRepository>(relaxUnitFun = true) {
        every { settings } returns settingsFlow
    }
    private val nfcStateObserver = mockk<NfcStateObserver> {
        every { isEnabled } returns nfcEnabledFlow
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): SettingsViewModel {
        val vm = SettingsViewModel(settingsRepository, nfcStateObserver)
        dispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `exposes the current settings and NFC status`() = runTest(dispatcher) {
        settingsFlow.value = TapSenseSettings(hapticsEnabled = false, appearanceMode = AppearanceMode.DARK)
        nfcEnabledFlow.value = true

        val vm = viewModel()

        // vm.settings/isNfcOn are WhileSubscribed StateFlows - reading .value with no subscriber
        // ever attached would just see the initial default forever (StateFlow.first() doesn't
        // help either - it returns the already-seeded current value immediately, before
        // WhileSubscribed's lazy upstream collection gets a chance to run). Attaching a real
        // collector is what actually starts that upstream collection.
        val settingsJob = launch { vm.settings.collect {} }
        val nfcJob = launch { vm.isNfcOn.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.settings.value.hapticsEnabled).isFalse()
        assertThat(vm.settings.value.appearanceMode).isEqualTo(AppearanceMode.DARK)
        assertThat(vm.isNfcOn.value).isTrue()

        settingsJob.cancel()
        nfcJob.cancel()
    }

    @Test
    fun `clearPhoneOverride delegates to the repository`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.clearPhoneOverride()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.clearSelectedPhone() }
    }

    @Test
    fun `setHapticsEnabled delegates to the repository`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.setHapticsEnabled(false)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setHapticsEnabled(false) }
    }

    @Test
    fun `setReduceMotion delegates to the repository`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.setReduceMotion(true)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setReduceMotion(true) }
    }

    @Test
    fun `setAppearanceMode delegates to the repository`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.setAppearanceMode(AppearanceMode.LIGHT)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setAppearanceMode(AppearanceMode.LIGHT) }
    }
}
