package com.tapsense.app.ui.home

import com.nfclocator.core.ui.state.AntennaLocatorUiState

data class HomeUiState(
    val isLoading: Boolean = true,
    val displayModel: String = "",
    val displayManufacturer: String = "",
    val antennaState: AntennaLocatorUiState = AntennaLocatorUiState.Loading,
    val isNfcOn: Boolean = false,
    val isNfcSupported: Boolean = true,
    val reduceMotion: Boolean = false,
    val isManualOverride: Boolean = false,
)
