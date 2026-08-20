package com.tapsense.app.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Top and side safe-drawing inset (status bar, a front-camera display cutout, and any side
 * cutout/curved-edge inset) - deliberately excluding the bottom navigation-bar inset. `Home`,
 * `MyPhone`, and `Settings` are hosted inside [com.tapsense.app.ui.navigation.TapSenseNavHost]'s
 * bottom-bar `Scaffold`, which already reserves the bottom inset via its `innerPadding` plus
 * `TapSenseBottomBar`'s own `navigationBarsPadding()`; applying the bottom inset again here
 * would double that gap. Screens without a bottom bar should use the full
 * `Modifier.safeDrawingPadding()` instead, since nothing else reserves their bottom inset.
 */
fun Modifier.topSafeDrawingPadding(): Modifier = composed {
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
}
