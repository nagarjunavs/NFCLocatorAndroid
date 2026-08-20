package com.nfclocator.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nfclocator.core.R

/**
 * Shown after a failed unlock/read attempt, prompting the user to reposition using the
 * guidance already on screen. Pairs with [com.nfclocator.core.domain.analytics.NfcLocatorAnalytics.retryGuidanceShown] -
 * hosts are expected to call that when this banner appears.
 *
 * `liveRegion = Polite` so TalkBack announces the retry prompt as soon as it appears, without
 * the user needing to swipe to find it.
 */
@Composable
fun RetryGuidanceBanner(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.nfc_locator_retry_guidance_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = stringResource(R.string.nfc_locator_retry_guidance_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
