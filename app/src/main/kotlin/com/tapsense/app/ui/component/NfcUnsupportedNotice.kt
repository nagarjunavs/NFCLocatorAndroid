package com.tapsense.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The "this device can't do that" notice for a screen that would otherwise show an antenna
 * tap-zone marker - a device with no NFC hardware at all has no antenna location to show, so
 * showing one anyway (as the resolver chain's generic-fallback heuristic otherwise would) is
 * actively misleading. Mirrors Tap Test's [com.tapsense.app.ui.taptest.TapTestScreen]
 * `NfcUnsupportedContent` treatment (same icon/heading/body language) so the message reads the
 * same everywhere a user can encounter it, just recolored per host screen's background context
 * via [iconBackground]/[iconTint]/[headingColor]/[bodyColor].
 */
@Composable
fun NfcUnsupportedNotice(
    heading: String,
    body: String,
    modifier: Modifier = Modifier,
    iconBackground: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    headingColor: Color = MaterialTheme.colorScheme.onSurface,
    bodyColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()) {
        Surface(shape = CircleShape, color = iconBackground, modifier = Modifier.size(96.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = iconTint, modifier = Modifier.size(40.dp))
            }
        }
        Text(
            text = heading,
            style = MaterialTheme.typography.titleSmall,
            color = headingColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = bodyColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
