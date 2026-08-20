package com.tapsense.app.ui.myphone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.nfclocator.core.ui.state.toUiState
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.ActiveDeviceSignalsProvider
import com.tapsense.app.util.friendlyManufacturerName
import com.tapsense.app.util.friendlyModelName
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class MyPhoneViewModel @Inject constructor(
    private val resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
    private val activeDeviceSignalsProvider: ActiveDeviceSignalsProvider,
    private val settingsRepository: TapSenseSettingsRepository,
    nfcStateObserver: NfcStateObserver,
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
    }
}
