package com.tapsense.app.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.tapsense.app.ui.navigation.TapSenseNavHost
import com.tapsense.app.ui.theme.TapSenseTheme
import kotlinx.coroutines.flow.map

@Composable
fun TapSenseApp(viewModel: AppShellViewModel = hiltViewModel()) {
    val appearanceMode by viewModel.appearanceMode.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val activity = LocalContext.current.findActivity()

    // Real fold-state detection for DeviceIdentitySignalsProvider - see FoldStateSignals'
    // KDoc for why this lives at the composable/window layer rather than inside the ViewModel.
    LaunchedEffect(activity) {
        if (activity != null) {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .map { it.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull() }
                .collect { folding -> viewModel.updateFoldingFeature(folding) }
        }
    }

    TapSenseTheme(appearanceMode = appearanceMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            TapSenseNavHost(reducedMotion = reduceMotion)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
