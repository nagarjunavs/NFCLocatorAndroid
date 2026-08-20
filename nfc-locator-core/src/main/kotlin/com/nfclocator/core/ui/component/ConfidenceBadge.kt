package com.nfclocator.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nfclocator.core.R
import com.nfclocator.core.domain.model.Confidence

/**
 * Small pill labeling how much to trust the marker/zone currently shown. Exists specifically
 * so a [Confidence.GENERIC] or [Confidence.UNKNOWN] state is never visually indistinguishable
 * from a confident one - see the non-negotiable rule in the project spec.
 */
@Composable
fun ConfidenceBadge(confidence: Confidence, modifier: Modifier = Modifier) {
    val label = stringResource(confidence.labelResId())
    val (background, content) = confidence.colors()
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = modifier
            .semantics { contentDescription = label }
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

private fun Confidence.labelResId(): Int = when (this) {
    Confidence.EXACT -> R.string.nfc_locator_confidence_exact
    Confidence.APPROXIMATE -> R.string.nfc_locator_confidence_approximate
    Confidence.GENERIC -> R.string.nfc_locator_confidence_generic
    Confidence.UNKNOWN -> R.string.nfc_locator_confidence_unknown
}

@Composable
private fun Confidence.colors() = when (this) {
    Confidence.EXACT -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    Confidence.APPROXIMATE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    Confidence.GENERIC -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    Confidence.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
}
