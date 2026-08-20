package com.tapsense.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.DeviceIdentitySignalsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
    private val deviceIdentitySignalsProvider: DeviceIdentitySignalsProvider,
    private val settingsRepository: TapSenseSettingsRepository,
) : ViewModel() {

    /** Real resolution preview for page 3 ("TapSense already recognized this device") - null while loading. */
    private val _autoDetectedProfile = MutableStateFlow<DeviceAntennaProfile?>(null)
    val autoDetectedProfile: StateFlow<DeviceAntennaProfile?> = _autoDetectedProfile.asStateFlow()

    init {
        viewModelScope.launch {
            val signals = deviceIdentitySignalsProvider.current()
            _autoDetectedProfile.value = resolveAntennaLocationUseCase(signals)
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            onDone()
        }
    }
}
