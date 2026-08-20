package com.tapsense.app.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tapsense.app.R
import com.tapsense.app.ui.component.TapSenseLogo
import com.tapsense.app.ui.theme.TapSensePalette

@Composable
fun SplashRoute(
    onNavigate: (String) -> Unit,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val nextDestination by viewModel.nextDestination.collectAsState()

    LaunchedEffect(nextDestination) {
        nextDestination?.let(onNavigate)
    }

    SplashScreen(reducedMotion = reducedMotion, modifier = modifier)
}

@Composable
private fun SplashScreen(reducedMotion: Boolean, modifier: Modifier = Modifier) {
    Surface(color = TapSensePalette.DarkBg, modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TapSenseLogo(
                color = TapSensePalette.Aqua,
                pulsing = true,
                reducedMotion = reducedMotion,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = TapSensePalette.TextLight,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = TapSensePalette.TextLightSecondary,
            )
        }
    }
}
