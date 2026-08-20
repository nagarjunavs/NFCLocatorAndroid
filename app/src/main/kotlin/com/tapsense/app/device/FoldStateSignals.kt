package com.tapsense.app.device

import androidx.window.layout.FoldingFeature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The most recently observed [FoldingFeature] for this process, or null when the current
 * window reports none (a plain bar/tablet device, or a foldable currently showing only its
 * cover display - `androidx.window` has no signal that distinguishes those two cases; see
 * [DeviceIdentitySignalsProvider]).
 *
 * A root composable (`TapSenseApp`) is the sole writer, via `WindowInfoTracker`'s
 * `windowLayoutInfo(activity)` flow - this holder just makes that latest value readable
 * synchronously from [DeviceIdentitySignalsProvider.current], which isn't itself composable.
 */
@Singleton
class FoldStateSignals @Inject constructor() {
    private val _current = MutableStateFlow<FoldingFeature?>(null)
    val current: StateFlow<FoldingFeature?> = _current

    fun update(foldingFeature: FoldingFeature?) {
        _current.value = foldingFeature
    }
}
