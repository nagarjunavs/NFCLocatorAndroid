package com.tapsense.app.ui.phoneselect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.tapsense.app.R
import com.tapsense.app.ui.component.ConfidenceChip
import com.tapsense.app.ui.theme.TapSensePalette
import com.tapsense.app.util.friendlyModelName

@Composable
fun PhoneSelectionRoute(
    onPhoneSelected: () -> Unit,
    onUseMyPhone: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneSelectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    PhoneSelectionScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onOsFilterChange = viewModel::onOsFilterChange,
        onPhoneClick = { profile -> viewModel.selectPhone(profile, onPhoneSelected) },
        onUseMyPhone = { viewModel.useMyPhoneAutomatically(onUseMyPhone) },
        onClose = onClose,
        modifier = modifier,
    )
}

@Composable
private fun PhoneSelectionScreen(
    uiState: PhoneSelectionUiState,
    onQueryChange: (String) -> Unit,
    onOsFilterChange: (PhoneOsFilter) -> Unit,
    onPhoneClick: (DeviceAntennaProfile) -> Unit,
    onUseMyPhone: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp), horizontalArrangement = Arrangement.End) {
            val closeDescription = stringResource(R.string.phone_selection_close_content_description)
            IconButton(onClick = onClose) {
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

        Text(
            text = stringResource(R.string.phone_selection_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )

        PhoneOsFilterToggle(
            selected = uiState.osFilter,
            onSelect = onOsFilterChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text(stringResource(R.string.phone_selection_search_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Text(
            text = stringResource(R.string.phone_selection_section_header),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.phone_selection_empty, uiState.query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.results, key = { "${it.manufacturer}:${it.model}" }) { profile ->
                    PhoneRow(profile = profile, onClick = { onPhoneClick(profile) })
                }
            }
        }

        TextButton(onClick = onUseMyPhone, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(stringResource(R.string.phone_selection_use_my_phone))
        }
    }
}

/**
 * Android/Apple segmented filter, matching the design's explicit toggle colors (a track/selected
 * tab pair distinct from the ambient surface tones - reuses the same tokens as My Phone's
 * Back/Front toggle in dark mode, and the surface/surfaceAlt pair in light mode, per the design's
 * own light vs dark hex values for this control).
 */
@Composable
private fun PhoneOsFilterToggle(
    selected: PhoneOsFilter,
    onSelect: (PhoneOsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background == TapSensePalette.DarkBg
    val trackColor = if (isDark) TapSensePalette.ToggleTrackDark else TapSensePalette.LightSurfaceAlt
    val selectedTabColor = if (isDark) TapSensePalette.ToggleTabSelectedDark else TapSensePalette.LightSurface
    Surface(color = trackColor, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Row(modifier = Modifier.padding(4.dp)) {
            PhoneOsFilterTab(
                label = stringResource(R.string.phone_selection_filter_android),
                selected = selected == PhoneOsFilter.ANDROID,
                selectedTabColor = selectedTabColor,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(PhoneOsFilter.ANDROID) },
            )
            PhoneOsFilterTab(
                label = stringResource(R.string.phone_selection_filter_apple),
                selected = selected == PhoneOsFilter.APPLE,
                selectedTabColor = selectedTabColor,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(PhoneOsFilter.APPLE) },
            )
        }
    }
}

@Composable
private fun PhoneOsFilterTab(
    label: String,
    selected: Boolean,
    selectedTabColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) selectedTabColor else Color.Transparent
    val textColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        color = background,
        shape = RoundedCornerShape(11.dp),
        onClick = onClick,
        modifier = modifier.padding(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        )
    }
}

@Composable
private fun PhoneRow(profile: DeviceAntennaProfile, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.model.friendlyModelName(), style = MaterialTheme.typography.titleSmall)
                Text(
                    profile.manufacturer.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ConfidenceChip(profile.confidence)
        }
    }
}
