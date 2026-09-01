package com.tapsense.app.ui.taptest

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.tapsense.app.R
import com.tapsense.app.ui.component.AntennaMarker
import com.tapsense.app.ui.theme.DarkMockupColors
import com.tapsense.app.ui.theme.TapSensePalette
import com.tapsense.app.ui.theme.cameraBumpAccentColor
import com.tapsense.app.ui.theme.tapSenseFilled
import com.tapsense.app.ui.theme.tapSenseOutlined
import com.tapsense.app.ui.theme.tapSenseOutlinedBorder
import com.tapsense.app.util.openNfcSettingsSafely

/**
 * The real "does this tap zone actually work" flow: registers as a live NFC reader
 * (`NfcAdapter#enableReaderMode`) for as long as this screen is on screen, driven by
 * [TapTestViewModel]. Reader mode is started/stopped from a `DisposableEffect` tied to this
 * composable's lifecycle, since it needs the hosting [Activity].
 */
@Composable
fun TapTestRoute(
    onCancel: () -> Unit,
    onViewTapTips: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TapTestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val antennaState by viewModel.antennaState.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val hapticFeedback = LocalHapticFeedback.current
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

    DisposableEffect(activity) {
        if (activity != null) {
            viewModel.startListening(activity)
        } else {
            viewModel.onReaderModeStarted(started = false)
        }
        onDispose {
            if (activity != null) viewModel.stopListening(activity)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TapTestUiState.Detected && hapticsEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    TapTestScreen(
        uiState = uiState,
        antennaState = antennaState,
        reduceMotion = reduceMotion,
        onCancel = onCancel,
        onRetry = viewModel::retry,
        onRetryFromNfcOff = { activity?.let(viewModel::startListening) },
        onViewTapTips = onViewTapTips,
        onOpenNfcSettings = { context.openNfcSettingsSafely() },
        modifier = modifier,
    )
}

@Composable
private fun TapTestScreen(
    uiState: TapTestUiState,
    antennaState: AntennaLocatorUiState?,
    reduceMotion: Boolean,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onRetryFromNfcOff: () -> Unit,
    onViewTapTips: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp), horizontalArrangement = Arrangement.End) {
            val closeDescription = stringResource(R.string.tap_test_close_content_description)
            IconButton(onClick = onCancel) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = closeDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text(
                text = stringResource(R.string.tap_test_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (uiState) {
                    TapTestUiState.Ready, TapTestUiState.Detecting -> DetectingContent(antennaState, reduceMotion)
                    TapTestUiState.Detected -> DetectedContent()
                    TapTestUiState.TimedOut -> TimedOutContent()
                    TapTestUiState.NfcOff -> NfcOffContent()
                    TapTestUiState.NfcUnsupported -> NfcUnsupportedContent()
                }
            }

            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                when (uiState) {
                    TapTestUiState.Ready, TapTestUiState.Detecting -> {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.tapSenseOutlined(),
                            border = tapSenseOutlinedBorder(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.tap_test_cancel))
                        }
                    }
                    TapTestUiState.Detected -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.tap_test_tap_again))
                            }
                            Button(onClick = onCancel, colors = ButtonDefaults.tapSenseFilled(), modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.tap_test_cancel))
                            }
                        }
                    }
                    TapTestUiState.TimedOut -> {
                        Button(onClick = onRetry, colors = ButtonDefaults.tapSenseFilled(), modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.tap_test_try_again))
                        }
                    }
                    TapTestUiState.NfcOff -> {
                        Button(onClick = onOpenNfcSettings, colors = ButtonDefaults.tapSenseFilled(), modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.tap_test_open_settings))
                        }
                        OutlinedButton(onClick = onRetryFromNfcOff, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text(stringResource(R.string.tap_test_try_again))
                        }
                    }
                    TapTestUiState.NfcUnsupported -> {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.tapSenseOutlined(),
                            border = tapSenseOutlinedBorder(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.tap_test_cancel))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectingContent(antennaState: AntennaLocatorUiState?, reduceMotion: Boolean) {
    if (antennaState != null) {
        val bumpColor = cameraBumpAccentColor()
        DarkMockupColors {
            AntennaMarker(
                state = antennaState,
                reducedMotion = reduceMotion,
                modifier = Modifier.width(147.dp).height(320.dp),
                silhouetteColor = TapSensePalette.PhoneBody,
                silhouetteBorderColor = TapSensePalette.PhoneBodyBorder,
                showCameraBump = true,
                cameraBumpColor = bumpColor,
            )
        }
    } else {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
    }
    Text(
        text = stringResource(R.string.tap_test_state_detecting),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 20.dp),
    )
    Text(
        text = stringResource(R.string.tap_test_hint_detecting),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
    )
}

@Composable
private fun DetectedContent() {
    StatusIcon(icon = Icons.Filled.CheckCircle, background = MaterialTheme.colorScheme.primaryContainer, tint = TapSensePalette.Success)
    Text(
        text = stringResource(R.string.tap_test_state_detected),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 20.dp),
    )
    Text(
        text = stringResource(R.string.tap_test_hint_detected),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun TimedOutContent() {
    StatusIcon(icon = Icons.Filled.Warning, background = MaterialTheme.colorScheme.tertiaryContainer, tint = MaterialTheme.colorScheme.onTertiaryContainer)
    Text(
        text = stringResource(R.string.tap_test_state_timed_out),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 20.dp),
    )
    Text(
        text = stringResource(R.string.tap_test_hint_timed_out),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun NfcOffContent() {
    StatusIcon(icon = Icons.Filled.Warning, background = MaterialTheme.colorScheme.errorContainer, tint = MaterialTheme.colorScheme.error)
    Text(
        text = stringResource(R.string.tap_test_state_nfc_off),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 20.dp),
    )
    Text(
        text = stringResource(R.string.tap_test_hint_nfc_off),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun NfcUnsupportedContent() {
    StatusIcon(icon = Icons.Filled.Warning, background = MaterialTheme.colorScheme.surfaceVariant, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
        text = stringResource(R.string.tap_test_state_nfc_unsupported),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 20.dp),
    )
    Text(
        text = stringResource(R.string.tap_test_hint_nfc_unsupported),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun StatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, background: androidx.compose.ui.graphics.Color, tint: androidx.compose.ui.graphics.Color) {
    Surface(shape = CircleShape, color = background, modifier = Modifier.size(96.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(40.dp))
        }
    }
}
