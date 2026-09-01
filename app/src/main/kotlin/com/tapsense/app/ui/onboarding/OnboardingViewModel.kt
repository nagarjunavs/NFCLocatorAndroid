package com.tapsense.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.DeviceIdentitySignalsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "OnboardingViewModel"

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
    private val deviceIdentitySignalsProvider: DeviceIdentitySignalsProvider,
    private val settingsRepository: TapSenseSettingsRepository,
    private val logger: NfcLocatorLogger,
) : ViewModel() {

    /**
     * Real resolution preview for page 3 ("TapSense already recognized this device") - null
     * while loading. Also left null (logged, not crashed) if resolution unexpectedly throws;
     * the preview card then keeps showing its loading text indefinitely, which is an honest
     * degradation - the page's Continue/Skip actions never depend on this value, so onboarding
     * itself is never blocked by a resolver failure here.
     */
    private val _autoDetectedProfile = MutableStateFlow<DeviceAntennaProfile?>(null)
    val autoDetectedProfile: StateFlow<DeviceAntennaProfile?> = _autoDetectedProfile.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val signals = deviceIdentitySignalsProvider.current()
                _autoDetectedProfile.value = resolveAntennaLocationUseCase(signals)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(TAG, "Failed to resolve antenna location", e)
            }
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            onDone()
        }
    }
}
