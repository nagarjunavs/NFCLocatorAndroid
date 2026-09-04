package com.tapsense.app.ui.troubleshoot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tapsense.app.R

/**
 * Selecting an issue reveals contextual, real actions - not just static tips: NFC-off routes
 * to the system NFC settings deep link, tap-zone confusion routes to My Phone, etc.
 */
@Composable
fun TroubleshootRoute(
    isNfcSupported: Boolean,
    onOpenNfcSettings: () -> Unit,
    onRunTapTest: () -> Unit,
    onViewTapZone: () -> Unit,
    onChoosePhone: () -> Unit,
    onLearnMore: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<TroubleshootIssue?>(null) }

    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp), horizontalArrangement = Arrangement.End) {
            val closeDescription = stringResource(R.string.troubleshoot_close_content_description)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.troubleshoot_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = stringResource(R.string.troubleshoot_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )

            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TroubleshootIssue.entries.forEach { issue ->
                    IssueRow(issue = issue, selected = issue == selected, onClick = { selected = issue })
                }
            }

            selected?.let { issue ->
                TroubleshootActions(
                    issue = issue,
                    isNfcSupported = isNfcSupported,
                    onOpenNfcSettings = onOpenNfcSettings,
                    onRunTapTest = onRunTapTest,
                    onViewTapZone = onViewTapZone,
                    onChoosePhone = onChoosePhone,
                    onLearnMore = onLearnMore,
                )
            }
        }
    }
}

@Composable
private fun IssueRow(issue: TroubleshootIssue, selected: Boolean, onClick: () -> Unit) {
    TroubleshootListRow(label = stringResource(issue.labelRes), selected = selected, onClick = onClick)
}

@Composable
private fun TroubleshootListRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TroubleshootActions(
    issue: TroubleshootIssue,
    isNfcSupported: Boolean,
    onOpenNfcSettings: () -> Unit,
    onRunTapTest: () -> Unit,
    onViewTapZone: () -> Unit,
    onChoosePhone: () -> Unit,
    onLearnMore: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 24.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    text = stringResource(R.string.troubleshoot_selected_prefix),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = " " + stringResource(issue.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when (issue) {
                TroubleshootIssue.NO_REACTION, TroubleshootIssue.READER_SILENT, TroubleshootIssue.PAY_FAILING -> {
                    // Opening NFC settings only makes sense - and only resolves to an actual
                    // settings screen - on a device that has NFC hardware to begin with.
                    if (isNfcSupported) {
                        ActionRow(stringResource(R.string.troubleshoot_action_open_nfc_settings), onOpenNfcSettings)
                    }
                    ActionRow(stringResource(R.string.troubleshoot_action_run_tap_test), onRunTapTest)
                }
                TroubleshootIssue.CANNOT_SCAN -> {
                    ActionRow(stringResource(R.string.troubleshoot_action_run_tap_test), onRunTapTest)
                    ActionRow(stringResource(R.string.troubleshoot_action_view_tap_zone), onViewTapZone)
                }
                TroubleshootIssue.DONT_KNOW_WHERE -> {
                    ActionRow(stringResource(R.string.troubleshoot_action_view_tap_zone), onViewTapZone)
                    ActionRow(stringResource(R.string.troubleshoot_action_learn_more), onLearnMore)
                }
                TroubleshootIssue.MODEL_MISSING -> {
                    ActionRow(stringResource(R.string.troubleshoot_action_choose_phone), onChoosePhone)
                }
            }
        }
    }
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}
