package com.tapsense.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tapsense.app.BuildConfig
import com.tapsense.app.R
import com.tapsense.app.data.settings.AppearanceMode
import com.tapsense.app.data.settings.TapSenseSettings
import com.tapsense.app.ui.theme.tapSenseColors
import com.tapsense.app.ui.util.topSafeDrawingPadding
import com.tapsense.app.util.friendlyModelName

@Composable
fun SettingsRoute(
    isNfcSupported: Boolean,
    onPhoneModelClick: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val isNfcOn by viewModel.isNfcOn.collectAsState()

    SettingsScreen(
        settings = settings,
        isNfcOn = isNfcOn,
        isNfcSupported = isNfcSupported,
        onPhoneModelClick = onPhoneModelClick,
        onOpenNfcSettings = onOpenNfcSettings,
        onHelpCenterClick = onHelpCenterClick,
        onPrivacyClick = onPrivacyClick,
        onHapticsChange = viewModel::setHapticsEnabled,
        onReduceMotionChange = viewModel::setReduceMotion,
        onAppearanceChange = viewModel::setAppearanceMode,
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreen(
    settings: TapSenseSettings,
    isNfcOn: Boolean,
    isNfcSupported: Boolean,
    onPhoneModelClick: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onAppearanceChange: (AppearanceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .topSafeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )

        SettingsRow(
            label = stringResource(R.string.settings_phone_model),
            onClick = onPhoneModelClick,
        ) {
            val phoneLabel = if (settings.hasManualPhoneOverride) {
                settings.selectedPhoneModel.orEmpty().friendlyModelName()
            } else {
                stringResource(R.string.settings_phone_auto)
            }
            Text("$phoneLabel  ›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()

        SettingsRow(
            label = stringResource(R.string.settings_nfc_status),
            onClick = if (isNfcSupported) onOpenNfcSettings else null,
        ) {
            Text(
                text = stringResource(
                    when {
                        !isNfcSupported -> R.string.settings_nfc_not_supported
                        isNfcOn -> R.string.settings_nfc_on
                        else -> R.string.settings_nfc_off
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNfcSupported && isNfcOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider()

        SettingsRow(label = stringResource(R.string.settings_haptics)) {
            Switch(checked = settings.hapticsEnabled, onCheckedChange = onHapticsChange, colors = SwitchDefaults.tapSenseColors())
        }
        HorizontalDivider()

        SettingsRow(label = stringResource(R.string.settings_reduce_motion)) {
            Switch(checked = settings.reduceMotion, onCheckedChange = onReduceMotionChange, colors = SwitchDefaults.tapSenseColors())
        }
        HorizontalDivider()

        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                AppearanceChip(AppearanceMode.SYSTEM, settings.appearanceMode, stringResource(R.string.settings_appearance_system), onAppearanceChange)
                AppearanceChip(AppearanceMode.LIGHT, settings.appearanceMode, stringResource(R.string.settings_appearance_light), onAppearanceChange)
                AppearanceChip(AppearanceMode.DARK, settings.appearanceMode, stringResource(R.string.settings_appearance_dark), onAppearanceChange)
            }
        }
        HorizontalDivider()

        SettingsRow(label = stringResource(R.string.settings_help_center), onClick = onHelpCenterClick) {
            Text("›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()
        SettingsRow(label = stringResource(R.string.settings_privacy), onClick = onPrivacyClick) {
            Text("›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text(
            text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp),
        )
    }
}

@Composable
private fun AppearanceChip(mode: AppearanceMode, selected: AppearanceMode, label: String, onSelect: (AppearanceMode) -> Unit) {
    FilterChip(selected = mode == selected, onClick = { onSelect(mode) }, label = { Text(label) })
}

@Composable
private fun SettingsRow(label: String, onClick: (() -> Unit)? = null, trailing: @Composable () -> Unit) {
    val base = Modifier.fillMaxWidth().padding(vertical = 13.dp)
    if (onClick != null) {
        Surface(onClick = onClick, color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
            SettingsRowContent(label, base, trailing)
        }
    } else {
        SettingsRowContent(label, base, trailing)
    }
}

@Composable
private fun SettingsRowContent(label: String, modifier: Modifier, trailing: @Composable () -> Unit) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        trailing()
    }
}
