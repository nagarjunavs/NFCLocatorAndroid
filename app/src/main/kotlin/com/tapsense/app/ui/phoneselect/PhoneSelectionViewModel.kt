package com.tapsense.app.ui.phoneselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.tapsense.app.data.catalog.PhoneCatalogRepository
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.util.friendlyDeviceName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PhoneSelectionViewModel"

@HiltViewModel
class PhoneSelectionViewModel @Inject constructor(
    private val catalogRepository: PhoneCatalogRepository,
    private val settingsRepository: TapSenseSettingsRepository,
    private val logger: NfcLocatorLogger,
) : ViewModel() {

    private var allProfiles: List<DeviceAntennaProfile> = emptyList()

    private val _uiState = MutableStateFlow(PhoneSelectionUiState())
    val uiState: StateFlow<PhoneSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                allProfiles = catalogRepository.listAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Catalog load failed entirely (e.g. a corrupted bundled asset) - degrade to an
                // empty catalog, which the screen already renders as its existing "no phones
                // match" empty state, instead of crashing or hanging in the loading spinner.
                logger.e(TAG, "Failed to load phone catalog", e)
            }
            _uiState.update {
                it.copy(isLoading = false, results = filteredProfiles(it.query, it.osFilter))
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, results = filteredProfiles(query, it.osFilter)) }
    }

    fun onOsFilterChange(osFilter: PhoneOsFilter) {
        _uiState.update { it.copy(osFilter = osFilter, results = filteredProfiles(it.query, osFilter)) }
    }

    private fun filteredProfiles(query: String, osFilter: PhoneOsFilter): List<DeviceAntennaProfile> {
        val needle = query.trim().lowercase()
        return allProfiles.filter { profile ->
            matchesOsFilter(profile, osFilter) &&
                (
                    needle.isBlank() ||
                        profile.manufacturer.lowercase().contains(needle) ||
                        profile.model.lowercase().contains(needle) ||
                        friendlyDeviceName(profile.manufacturer, profile.model).lowercase().contains(needle)
                    )
        }
    }

    private fun matchesOsFilter(profile: DeviceAntennaProfile, osFilter: PhoneOsFilter): Boolean {
        val isApple = profile.manufacturer.equals("apple", ignoreCase = true)
        return if (osFilter == PhoneOsFilter.APPLE) isApple else !isApple
    }

    fun selectPhone(profile: DeviceAntennaProfile, onSelected: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setSelectedPhone(profile.manufacturer, profile.model, profile.formFactor)
            onSelected()
        }
    }

    fun useMyPhoneAutomatically(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.clearSelectedPhone()
            onDone()
        }
    }
}
