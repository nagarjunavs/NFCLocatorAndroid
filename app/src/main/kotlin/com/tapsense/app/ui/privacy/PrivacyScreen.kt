package com.tapsense.app.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tapsense.app.R
import com.tapsense.app.ui.util.topSafeDrawingPadding

/**
 * States only what the app's code actually does with data - each section below corresponds
 * directly to a real code path (NFC APIs, the catalog network call, local logging, the single
 * declared permission), not boilerplate policy language.
 */
@Composable
fun PrivacyRoute(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .topSafeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.privacy_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp),
        )

        Text(
            text = stringResource(R.string.privacy_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PrivacySection(R.string.privacy_on_device_title, R.string.privacy_on_device_body)
        PrivacySection(R.string.privacy_catalog_title, R.string.privacy_catalog_body)
        PrivacySection(R.string.privacy_analytics_title, R.string.privacy_analytics_body)
        PrivacySection(R.string.privacy_permissions_title, R.string.privacy_permissions_body)
        PrivacySection(R.string.privacy_third_party_title, R.string.privacy_third_party_body)
        PrivacySection(R.string.privacy_retention_title, R.string.privacy_retention_body)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PrivacySection(titleRes: Int, bodyRes: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(bodyRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
