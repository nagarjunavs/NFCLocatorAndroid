package com.tapsense.app.ui.taptest

/** Mirrors the design's `testStates`: Ready, Detecting, Detected, No tag yet, NFC off, Timed out. */
sealed interface TapTestUiState {
    data object NfcUnsupported : TapTestUiState
    data object NfcOff : TapTestUiState
    data object Ready : TapTestUiState
    data object Detecting : TapTestUiState
    data object Detected : TapTestUiState
    data object TimedOut : TapTestUiState
}
