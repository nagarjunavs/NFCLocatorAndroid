package com.tapsense.app.ui.phoneselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.ActiveDeviceSignalsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PhoneConfirmedViewModel"

@HiltViewModel
class PhoneConfirmedViewModel @Inject constructor(
    private val resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
    private val activeDeviceSignalsProvider: ActiveDeviceSignalsProvider,
    private val settingsRepository: TapSenseSettingsRepository,
    private val logger: NfcLocatorLogger,
) : ViewModel() {

    /** Null while loading, and also left null (logged, not crashed) if resolution unexpectedly
     * throws - this screen's Go Home/Choose Different actions never depend on this value. */
    private val _confirmedProfile = MutableStateFlow<DeviceAntennaProfile?>(null)
    val confirmedProfile: StateFlow<DeviceAntennaProfile?> = _confirmedProfile.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.settings.first()
                val signals = activeDeviceSignalsProvider.signalsFor(settings)
                _confirmedProfile.value = resolveAntennaLocationUseCase(signals)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(TAG, "Failed to resolve antenna location", e)
            }
        }
    }

    /**
     * Marks onboarding complete (idempotent) before leaving - this screen is reachable both
     * from onboarding's optional "choose a different phone" detour and from Home/Settings'
     * "Change phone" path, and only the former needs this, but it's harmless either way.
     */
    fun goHome(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            onDone()
        }
    }
}
