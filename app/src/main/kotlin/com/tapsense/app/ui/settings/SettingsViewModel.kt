package com.tapsense.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.settings.AppearanceMode
import com.tapsense.app.data.settings.TapSenseSettings
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: TapSenseSettingsRepository,
    private val nfcStateObserver: NfcStateObserver,
) : ViewModel() {

    val settings: StateFlow<TapSenseSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TapSenseSettings())

    val isNfcOn: StateFlow<Boolean> = nfcStateObserver.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun clearPhoneOverride() {
        viewModelScope.launch { settingsRepository.clearSelectedPhone() }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setReduceMotion(enabled) }
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch { settingsRepository.setAppearanceMode(mode) }
    }
}
