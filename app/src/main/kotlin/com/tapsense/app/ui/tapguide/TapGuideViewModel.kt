package com.tapsense.app.ui.tapguide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.nfclocator.core.ui.state.toUiState
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.ActiveDeviceSignalsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TapGuideViewModel"

data class TapGuideUiState(
    val antennaState: AntennaLocatorUiState? = null,
    val reduceMotion: Boolean = false,
    val isNfcSupported: Boolean = true,
)

@HiltViewModel
class TapGuideViewModel @Inject constructor(
    private val resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
    private val activeDeviceSignalsProvider: ActiveDeviceSignalsProvider,
    private val settingsRepository: TapSenseSettingsRepository,
    nfcStateObserver: NfcStateObserver,
    private val logger: NfcLocatorLogger,
) : ViewModel() {

    // The walkthrough always ends in a real physical tap test, which needs live NFC hardware
    // regardless of which phone is being previewed - so unlike Home/My Phone's marker display,
    // there's no manual-override exemption here (see TapGuideRoute's redirect).
    private val _uiState = MutableStateFlow(TapGuideUiState(isNfcSupported = nfcStateObserver.isNfcSupported))
    val uiState: StateFlow<TapGuideUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.settings.first()
                _uiState.update { it.copy(reduceMotion = settings.reduceMotion) }
                val signals = activeDeviceSignalsProvider.signalsFor(settings)
                val profile = resolveAntennaLocationUseCase(signals)
                _uiState.update { it.copy(antennaState = profile.toUiState()) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // See HomeViewModel.resolveCurrent - defensive last resort so a resolver edge
                // case degrades to a visible error state instead of crashing the app outright.
                logger.e(TAG, "Failed to resolve antenna location", e)
                _uiState.update { it.copy(antennaState = AntennaLocatorUiState.Error) }
            }
        }
    }
}
