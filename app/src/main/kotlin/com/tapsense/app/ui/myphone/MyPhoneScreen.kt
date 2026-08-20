package com.tapsense.app.ui.myphone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.tapsense.app.R
import com.tapsense.app.ui.component.AntennaMarker
import com.tapsense.app.ui.component.ConfidenceChip
import com.tapsense.app.ui.component.NfcUnsupportedNotice
import com.tapsense.app.ui.component.PhoneSilhouette
import com.tapsense.app.ui.theme.DarkMockupColors
import com.tapsense.app.ui.theme.TapSensePalette
import com.tapsense.app.ui.theme.cameraBumpAccentColor
import com.tapsense.app.ui.theme.darkCardSilhouetteColor
import com.tapsense.app.ui.theme.myPhoneBackBodyColor
import com.tapsense.app.ui.theme.myPhoneBackBorderColor
import com.tapsense.app.ui.theme.screenInsetColor
import com.tapsense.app.ui.theme.screenNotchColor
import com.tapsense.app.ui.theme.tapSenseFilled
import com.tapsense.app.ui.util.topSafeDrawingPadding

private enum class PhoneSide { BACK, FRONT }

@Composable
fun MyPhoneRoute(
    onTestLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPhoneViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    MyPhoneScreen(
        uiState = uiState,
        onTestLocation = onTestLocation,
        modifier = modifier,
    )
}

@Composable
private fun MyPhoneScreen(
    uiState: MyPhoneUiState,
    onTestLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var side by remember { mutableStateOf(PhoneSide.BACK) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .topSafeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
            Text(uiState.displayManufacturer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(uiState.displayModel.ifBlank { " " }, style = MaterialTheme.typography.headlineSmall)
        }

        // A device with no NFC hardware at all has no antenna to show on either side, so the
        // Back/Front toggle (and its Front placeholder) is moot - skip straight to the notice.
        val showNfcUnsupported = !uiState.isLoading && !uiState.isNfcSupported && !uiState.isManualOverride

        if (!showNfcUnsupported) {
            SideToggle(selected = side, onSelect = { side = it }, modifier = Modifier.padding(vertical = 8.dp))
        }

        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().height(280.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (showNfcUnsupported) {
            Column(
                modifier = Modifier.fillMaxWidth().height(280.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                NfcUnsupportedNotice(
                    heading = stringResource(R.string.home_nfc_unsupported),
                    body = stringResource(R.string.my_phone_nfc_unsupported_body),
                )
            }
        } else if (side == PhoneSide.FRONT) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                PhoneSilhouette(
                    color = darkCardSilhouetteColor(),
                    screenInset = true,
                    insetColor = screenInsetColor(),
                    notchColor = screenNotchColor(),
                    modifier = Modifier.width(150.dp).height(300.dp),
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.Center) {
                NotApplicableBadge()
            }

            Text(
                text = stringResource(R.string.my_phone_front_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.my_phone_front_banner),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Button(
                onClick = { side = PhoneSide.BACK },
                colors = ButtonDefaults.tapSenseFilled(),
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text(stringResource(R.string.my_phone_view_back_placement))
            }
        } else {
            MyPhoneMarker(state = uiState.antennaState, reducedMotion = uiState.reduceMotion)

            val confidence = when (val s = uiState.antennaState) {
                is AntennaLocatorUiState.ResolvedMarker -> s.confidence
                is AntennaLocatorUiState.FallbackGuidance -> s.confidence
                else -> null
            }
            if (confidence != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    ConfidenceChip(confidence)
                }
            }

            Text(
                text = stringResource(R.string.my_phone_zone_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.my_phone_case_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = stringResource(R.string.my_phone_orientation_header),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OrientationRow(stringResource(R.string.my_phone_orientation_back_contact))
            }

            Button(onClick = onTestLocation, colors = ButtonDefaults.tapSenseFilled(), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(stringResource(R.string.my_phone_test_location))
            }
        }
    }
}

@Composable
private fun MyPhoneMarker(state: AntennaLocatorUiState, reducedMotion: Boolean) {
    val silhouetteColor = myPhoneBackBodyColor()
    val borderColor = myPhoneBackBorderColor()
    val bumpColor = cameraBumpAccentColor()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        DarkMockupColors {
            AntennaMarker(
                state = state,
                reducedMotion = reducedMotion,
                modifier = Modifier.width(150.dp).height(300.dp),
                silhouetteColor = silhouetteColor,
                silhouetteBorderColor = borderColor,
                showCameraBump = true,
                cameraBumpColor = bumpColor,
            )
        }
    }
}

/** My Phone Front tab's "this isn't the antenna side" indicator - a UI-only state, distinct from [ConfidenceChip]'s resolver-confidence tiers. */
@Composable
private fun NotApplicableBadge(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == TapSensePalette.DarkBg
    val iconColor = if (isDark) TapSensePalette.TextLightSecondary else TapSensePalette.Ink3
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Canvas(modifier = Modifier.size(10.dp)) {
                drawCircle(color = iconColor, radius = size.minDimension / 2f - 0.7.dp.toPx(), style = Stroke(width = 1.4.dp.toPx()))
            }
            Text(
                text = stringResource(R.string.my_phone_not_applicable),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SideToggle(selected: PhoneSide, onSelect: (PhoneSide) -> Unit, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == TapSensePalette.DarkBg
    val trackColor = if (isDark) TapSensePalette.ToggleTrackDark else TapSensePalette.ToggleTrackLight
    val selectedTabColor = if (isDark) TapSensePalette.ToggleTabSelectedDark else MaterialTheme.colorScheme.surface
    Surface(color = trackColor, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Row(modifier = Modifier.padding(4.dp)) {
            SideToggleTab(stringResource(R.string.my_phone_tab_back), selected == PhoneSide.BACK, selectedTabColor) { onSelect(PhoneSide.BACK) }
            SideToggleTab(stringResource(R.string.my_phone_tab_front), selected == PhoneSide.FRONT, selectedTabColor) { onSelect(PhoneSide.FRONT) }
        }
    }
}

@Composable
private fun SideToggleTab(label: String, selected: Boolean, selectedTabColor: Color, onClick: () -> Unit) {
    val background = if (selected) selectedTabColor else Color.Transparent
    val textColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        color = background,
        shape = RoundedCornerShape(11.dp),
        onClick = onClick,
        modifier = Modifier.padding(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun OrientationRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
