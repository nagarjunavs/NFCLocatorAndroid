package com.tapsense.app.ui.myphone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.nfclocator.core.ui.state.toUiState
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.ActiveDeviceSignalsProvider
import com.tapsense.app.util.friendlyManufacturerName
import com.tapsense.app.util.friendlyModelName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyPhoneViewModel"

@HiltViewModel
class MyPhoneViewModel @Inject constructor(
    private val resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
    private val activeDeviceSignalsProvider: ActiveDeviceSignalsProvider,
    private val settingsRepository: TapSenseSettingsRepository,
    nfcStateObserver: NfcStateObserver,
    private val logger: NfcLocatorLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPhoneUiState(isNfcSupported = nfcStateObserver.isNfcSupported))
    val uiState: StateFlow<MyPhoneUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                _uiState.update { it.copy(reduceMotion = settings.reduceMotion, isManualOverride = settings.hasManualPhoneOverride) }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings
                .map { Triple(it.selectedPhoneManufacturer, it.selectedPhoneModel, it.selectedPhoneFormFactor) }
                .distinctUntilChanged()
                .collectLatest { resolveCurrent() }
        }
    }

    private suspend fun resolveCurrent() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val settings = settingsRepository.settings.first()
            val signals = activeDeviceSignalsProvider.signalsFor(settings)
            val profile = resolveAntennaLocationUseCase(signals)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    displayModel = profile.model.friendlyModelName(),
                    displayManufacturer = profile.manufacturer.friendlyManufacturerName(),
                    antennaState = profile.toUiState(),
                )
            }
        } catch (e: CancellationException) {
            throw e // a newer collectLatest emission superseding this call, not a real failure
        } catch (e: Exception) {
            // See HomeViewModel.resolveCurrent - defensive last resort so a resolver edge case
            // degrades to a visible error state instead of crashing the app outright.
            logger.e(TAG, "Failed to resolve antenna location", e)
            _uiState.update { it.copy(isLoading = false, antennaState = AntennaLocatorUiState.Error) }
        }
    }
}
