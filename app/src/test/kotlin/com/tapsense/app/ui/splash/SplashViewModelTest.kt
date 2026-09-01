package com.tapsense.app.ui.splash

import com.google.common.truth.Truth.assertThat
import com.tapsense.app.data.settings.TapSenseSettings
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.ui.navigation.TapSenseDestinations
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
class SplashViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val settingsFlow = MutableStateFlow(TapSenseSettings())
    private val settingsRepository = mockk<TapSenseSettingsRepository> {
        every { settings } returns settingsFlow
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `routes to Home when onboarding is already complete`() = runTest(dispatcher) {
        settingsFlow.value = TapSenseSettings(onboardingCompleted = true)

        val vm = SplashViewModel(settingsRepository)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.nextDestination.value).isEqualTo(TapSenseDestinations.HOME)
    }

    @Test
    fun `routes to Onboarding when it hasn't been completed`() = runTest(dispatcher) {
        settingsFlow.value = TapSenseSettings(onboardingCompleted = false)

        val vm = SplashViewModel(settingsRepository)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.nextDestination.value).isEqualTo(TapSenseDestinations.ONBOARDING)
    }

    @Test
    fun `stays null until the minimum splash duration has elapsed`() = runTest(dispatcher) {
        settingsFlow.value = TapSenseSettings(onboardingCompleted = true)

        val vm = SplashViewModel(settingsRepository)
        dispatcher.scheduler.runCurrent()

        assertThat(vm.nextDestination.value).isNull()
    }
}
