package com.tapsense.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.tapsense.app.R
import com.tapsense.app.ui.component.AntennaMarker
import com.tapsense.app.ui.component.ConfidenceChip
import com.tapsense.app.ui.component.NfcUnsupportedNotice
import com.tapsense.app.ui.theme.DarkMockupColors
import com.tapsense.app.ui.theme.TapSensePalette
import com.tapsense.app.ui.theme.cameraBumpAccentColor
import com.tapsense.app.ui.theme.darkCardBackgroundColor
import com.tapsense.app.ui.theme.tapSenseFilled
import com.tapsense.app.ui.util.topSafeDrawingPadding

@Composable
fun HomeRoute(
    onStartTapGuide: () -> Unit,
    onChangePhone: () -> Unit,
    onTapNotWorking: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onStartTapGuide = onStartTapGuide,
        onChangePhone = onChangePhone,
        onTapNotWorking = onTapNotWorking,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onStartTapGuide: () -> Unit,
    onChangePhone: () -> Unit,
    onTapNotWorking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .topSafeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = if (uiState.isManualOverride) {
                    stringResource(R.string.home_previewing_label)
                } else {
                    stringResource(timeOfDayGreetingRes())
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (uiState.displayModel.isBlank()) " " else uiState.displayModel,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Surface(
            color = darkCardBackgroundColor(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = TapSensePalette.Aqua)
                } else if (!uiState.isNfcSupported && !uiState.isManualOverride) {
                    // The resolver chain always returns *some* zone (its generic fallback
                    // guarantees that), but a device with no NFC hardware at all has no real
                    // antenna to point at - showing a tap zone here would be actively
                    // misleading, not just imprecise. Manual-override previews are exempt:
                    // the antenna belongs to the *previewed* model, not this physical device.
                    NfcUnsupportedNotice(
                        heading = stringResource(R.string.home_nfc_unsupported),
                        body = stringResource(R.string.home_nfc_unsupported_body),
                        iconBackground = Color.White.copy(alpha = 0.08f),
                        iconTint = TapSensePalette.TextLightSecondary,
                        headingColor = TapSensePalette.TextLight,
                        bodyColor = TapSensePalette.TextLightSecondary,
                    )
                } else {
                    val confidence = uiState.antennaState.confidenceOrNull()
                    if (confidence != null) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                            ConfidenceChip(confidence, onDarkCard = true)
                        }
                    }
                    HomeMarker(state = uiState.antennaState, reducedMotion = uiState.reduceMotion)
                    val tapAreaTextRes = when (uiState.antennaState) {
                        is AntennaLocatorUiState.FallbackGuidance -> R.string.home_tap_area_estimated
                        else -> R.string.home_tap_area_recommended
                    }
                    Text(
                        text = stringResource(tapAreaTextRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TapSensePalette.TextLightSecondary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onStartTapGuide, colors = ButtonDefaults.tapSenseFilled(), modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_start_tap_guide))
            }
            OutlinedButton(onClick = onChangePhone) {
                Text(stringResource(R.string.home_change_phone))
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(
                    text = stringResource(R.string.home_tip_case),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    when {
                        !uiState.isNfcSupported -> R.string.home_nfc_unsupported
                        uiState.isNfcOn -> R.string.home_nfc_status_on
                        else -> R.string.home_nfc_status_off
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            val dotColor = if (uiState.isNfcSupported && uiState.isNfcOn) TapSensePalette.Success else TapSensePalette.Ink3
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(10.dp).background(dotColor, CircleShape),
            )
        }

        TextButton(
            onClick = onTapNotWorking,
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_tap_not_working))
        }
    }
}

@Composable
private fun HomeMarker(state: AntennaLocatorUiState, reducedMotion: Boolean) {
    val bumpColor = cameraBumpAccentColor()
    DarkMockupColors {
        AntennaMarker(
            state = state,
            reducedMotion = reducedMotion,
            modifier = Modifier.width(120.dp).height(230.dp),
            silhouetteColor = TapSensePalette.PhoneBody,
            silhouetteBorderColor = TapSensePalette.PhoneBodyBorder,
            showCameraBump = true,
            cameraBumpColor = bumpColor,
        )
    }
}

private fun AntennaLocatorUiState.confidenceOrNull(): Confidence? = when (this) {
    is AntennaLocatorUiState.ResolvedMarker -> confidence
    is AntennaLocatorUiState.FallbackGuidance -> confidence
    else -> null
}

private fun timeOfDayGreetingRes(): Int = when (java.time.LocalTime.now().hour) {
    in 5..11 -> R.string.home_greeting_morning
    in 12..17 -> R.string.home_greeting_afternoon
    else -> R.string.home_greeting_evening
}
