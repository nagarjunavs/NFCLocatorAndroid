package com.tapsense.app.ui.phoneselect

import com.nfclocator.core.domain.model.DeviceAntennaProfile

enum class PhoneOsFilter {
    ANDROID,
    APPLE,
}

data class PhoneSelectionUiState(
    val query: String = "",
    val osFilter: PhoneOsFilter = PhoneOsFilter.ANDROID,
    val isLoading: Boolean = true,
    val results: List<DeviceAntennaProfile> = emptyList(),
)
