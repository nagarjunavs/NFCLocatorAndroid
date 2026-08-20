package com.tapsense.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.ui.navigation.TapSenseDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SPLASH_MIN_VISIBLE_MILLIS = 1100L

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsRepository: TapSenseSettingsRepository,
) : ViewModel() {

    private val _nextDestination = MutableStateFlow<String?>(null)
    val nextDestination: StateFlow<String?> = _nextDestination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_MIN_VISIBLE_MILLIS)
            val settings = settingsRepository.settings.first()
            _nextDestination.value = if (settings.onboardingCompleted) {
                TapSenseDestinations.HOME
            } else {
                TapSenseDestinations.ONBOARDING
            }
        }
    }
}
