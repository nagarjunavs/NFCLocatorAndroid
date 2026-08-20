package com.tapsense.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.window.layout.FoldingFeature
import com.tapsense.app.data.settings.AppearanceMode
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.FoldStateSignals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Drives the two settings that apply above/around navigation: theme mode and reduced motion. */
@HiltViewModel
class AppShellViewModel @Inject constructor(
    settingsRepository: TapSenseSettingsRepository,
    private val foldStateSignals: FoldStateSignals,
) : ViewModel() {

    val appearanceMode: StateFlow<AppearanceMode> = settingsRepository.settings
        .map { it.appearanceMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceMode.SYSTEM)

    val reduceMotion: StateFlow<Boolean> = settingsRepository.settings
        .map { it.reduceMotion }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Called from `TapSenseApp`'s `WindowInfoTracker` collection - see [FoldStateSignals]. */
    fun updateFoldingFeature(foldingFeature: FoldingFeature?) {
        foldStateSignals.update(foldingFeature)
    }
}
