package com.tapsense.app.ui.myphone

import com.nfclocator.core.ui.state.AntennaLocatorUiState

data class MyPhoneUiState(
    val isLoading: Boolean = true,
    val displayModel: String = "",
    val displayManufacturer: String = "",
    val antennaState: AntennaLocatorUiState = AntennaLocatorUiState.Loading,
    val reduceMotion: Boolean = false,
    val isNfcSupported: Boolean = true,
    val isManualOverride: Boolean = false,
)
