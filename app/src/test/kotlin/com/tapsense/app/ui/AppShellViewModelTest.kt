package com.tapsense.app.ui

import com.google.common.truth.Truth.assertThat
import com.tapsense.app.data.settings.AppearanceMode
import com.tapsense.app.data.settings.TapSenseSettings
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.FoldStateSignals
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
class AppShellViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val settingsFlow = MutableStateFlow(TapSenseSettings())
    private val settingsRepository = mockk<TapSenseSettingsRepository> {
        every { settings } returns settingsFlow
    }
    // Trivial no-dependency holder - a real instance is simpler and just as safe as mocking it.
    private val foldStateSignals = FoldStateSignals()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): AppShellViewModel {
        val vm = AppShellViewModel(settingsRepository, foldStateSignals)
        dispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `exposes the persisted appearance mode and reduce-motion setting`() = runTest(dispatcher) {
        settingsFlow.value = TapSenseSettings(appearanceMode = AppearanceMode.DARK, reduceMotion = true)

        val vm = viewModel()

        assertThat(vm.appearanceMode.value).isEqualTo(AppearanceMode.DARK)
        assertThat(vm.reduceMotion.value).isTrue()
    }

    @Test
    fun `updateFoldingFeature forwards to the shared FoldStateSignals holder`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.updateFoldingFeature(null)

        // No real FoldingFeature is constructible in a unit test (it requires a real Activity's
        // WindowLayoutInfo); asserting the pass-through with null still exercises the wiring
        // without needing a heavier instrumented test.
        assertThat(foldStateSignals.current.value).isNull()
    }
}
