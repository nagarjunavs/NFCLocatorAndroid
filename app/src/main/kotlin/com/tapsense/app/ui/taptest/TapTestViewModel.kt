package com.tapsense.app.ui.taptest

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.nfclocator.core.ui.state.toUiState
import com.tapsense.app.data.nfc.NfcStateObserver
import com.tapsense.app.data.nfc.TapReaderModeController
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import com.tapsense.app.device.ActiveDeviceSignalsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_TIMEOUT_MILLIS = 25_000L

/**
 * Drives the Tap Test screen's state machine. [startListening]/[stopListening] are the only
 * methods that touch the real `NfcAdapter.enableReaderMode` boundary (via
 * [TapReaderModeController], which needs a live `Activity`); everything else is plain state-machine
 * logic with no Android framework dependency, so the transitions themselves stay unit-testable
 * (see the secondary constructor, used only by tests to inject a short timeout).
 */
@HiltViewModel
class TapTestViewModel @Inject constructor(
    private val nfcStateObserver: NfcStateObserver,
    private val readerModeController: TapReaderModeController,
    private val settingsRepository: TapSenseSettingsRepository,
    private val resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
    private val activeDeviceSignalsProvider: ActiveDeviceSignalsProvider,
) : ViewModel() {

    constructor(
        nfcStateObserver: NfcStateObserver,
        readerModeController: TapReaderModeController,
        settingsRepository: TapSenseSettingsRepository,
        resolveAntennaLocationUseCase: ResolveAntennaLocationUseCase,
        activeDeviceSignalsProvider: ActiveDeviceSignalsProvider,
        timeoutMillis: Long,
    ) : this(nfcStateObserver, readerModeController, settingsRepository, resolveAntennaLocationUseCase, activeDeviceSignalsProvider) {
        this.timeoutMillis = timeoutMillis
    }

    private var timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
    private var timeoutJob: Job? = null

    private val _uiState = MutableStateFlow<TapTestUiState>(TapTestUiState.Ready)
    val uiState: StateFlow<TapTestUiState> = _uiState.asStateFlow()

    private val _antennaState = MutableStateFlow<AntennaLocatorUiState?>(null)
    /** The resolved device's marker, shown behind the Ready/Detecting content - matches Home/My Phone/Tap Guide. */
    val antennaState: StateFlow<AntennaLocatorUiState?> = _antennaState.asStateFlow()

    val hapticsEnabled: StateFlow<Boolean> = settingsRepository.settings
        .map { it.hapticsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val reduceMotion: StateFlow<Boolean> = settingsRepository.settings
        .map { it.reduceMotion }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isNfcSupported: Boolean get() = nfcStateObserver.isNfcSupported

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val signals = activeDeviceSignalsProvider.signalsFor(settings)
            _antennaState.value = resolveAntennaLocationUseCase(signals).toUiState()
        }
    }

    /** Starts real NFC reader mode against [activity] - call from a `DisposableEffect` when the screen appears. */
    fun startListening(activity: Activity) {
        val started = readerModeController.start(activity) { onTagDetected() }
        onReaderModeStarted(started)
    }

    /** Stops reader mode - call from the same `DisposableEffect`'s `onDispose`. */
    fun stopListening(activity: Activity) {
        readerModeController.stop(activity)
    }

    /** Call once the screen is composed and reader mode has been (attempted to be) started. */
    fun onReaderModeStarted(started: Boolean) {
        if (!isNfcSupported) {
            _uiState.value = TapTestUiState.NfcUnsupported
            return
        }
        if (!started) {
            _uiState.value = TapTestUiState.NfcOff
            return
        }
        beginDetecting()
    }

    fun onTagDetected() {
        timeoutJob?.cancel()
        _uiState.value = TapTestUiState.Detected
    }

    /**
     * User tapped "Try again" after a timeout. Reader mode is still active in the background
     * (it's only disabled when the screen is left, see [stopListening]) - this just resets the
     * UI back to "Detecting" and restarts the timeout window, without re-registering with the
     * `NfcAdapter`.
     */
    fun retry() {
        beginDetecting()
    }

    private fun beginDetecting() {
        timeoutJob?.cancel()
        _uiState.value = TapTestUiState.Detecting
        timeoutJob = viewModelScope.launch {
            delay(timeoutMillis)
            if (_uiState.value == TapTestUiState.Detecting) {
                _uiState.value = TapTestUiState.TimedOut
            }
        }
    }

    override fun onCleared() {
        timeoutJob?.cancel()
    }
}
