package com.tapsense.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nfclocator.core.domain.model.Confidence
import com.tapsense.app.R
import com.tapsense.app.ui.theme.TapSensePalette

/**
 * The terse "EXACT / APPROXIMATE / ESTIMATED / UNKNOWN" pill from the TapSense style guide's
 * confidence-badge swatches - distinct from `nfc-locator-core`'s [com.nfclocator.core.ui.component.ConfidenceBadge],
 * which uses longer, more descriptive copy suited to a generic host. This one is for contexts
 * that mirror the design literally: the phone picker list and the My Phone antenna detail card.
 *
 * [onDarkCard] switches to the design's subtler translucent-white-on-dark treatment (colored
 * text + dot, no colored pill background) used for the badge shown *inside* a hardcoded-dark
 * device card (Home, Tap Guide) - as opposed to the bold colored pill used everywhere else
 * (Phone Selection/Confirmed, My Phone, Onboarding), which stays the default (`false`).
 */
@Composable
fun ConfidenceChip(confidence: Confidence, modifier: Modifier = Modifier, onDarkCard: Boolean = false) {
    val style = confidence.chipStyle()
    val containerColor = if (onDarkCard) Color.White.copy(alpha = 0.1f) else style.container
    val textColor = if (onDarkCard) style.dot else style.text
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(containerColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(8.dp)
                .background(style.dot, CircleShape),
        )
        Text(
            text = stringResource(style.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

private data class ChipStyle(val dot: Color, val container: Color, val text: Color, val labelRes: Int)

private fun Confidence.chipStyle(): ChipStyle = when (this) {
    Confidence.EXACT -> ChipStyle(TapSensePalette.Success, TapSensePalette.SuccessContainer, TapSensePalette.SuccessOn, R.string.confidence_chip_exact)
    Confidence.APPROXIMATE -> ChipStyle(TapSensePalette.ApproxOn, TapSensePalette.ApproxContainer, TapSensePalette.ApproxOn, R.string.confidence_chip_approximate)
    Confidence.GENERIC -> ChipStyle(TapSensePalette.Amber, TapSensePalette.AmberContainer, TapSensePalette.AmberOnStrong, R.string.confidence_chip_estimated)
    Confidence.UNKNOWN -> ChipStyle(TapSensePalette.Ink2, TapSensePalette.LightSurfaceAlt, TapSensePalette.Ink2, R.string.confidence_chip_unknown)
}
