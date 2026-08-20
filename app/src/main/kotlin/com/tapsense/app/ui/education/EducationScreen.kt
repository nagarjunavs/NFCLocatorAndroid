package com.tapsense.app.ui.education

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

private data class Faq(val questionRes: Int, val answerRes: Int)

private val FAQS = listOf(
    Faq(R.string.education_faq_antenna_location, R.string.education_faq_antenna_location_answer),
    Faq(R.string.education_faq_tap_tag, R.string.education_faq_tap_tag_answer),
    Faq(R.string.education_faq_pay_vs_scan, R.string.education_faq_pay_vs_scan_answer),
    Faq(R.string.education_faq_why_fail, R.string.education_faq_why_fail_answer),
)

@Composable
fun EducationRoute(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.education_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.education_what_is_nfc_title), style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = stringResource(R.string.education_what_is_nfc_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
            items(FAQS) { faq -> FaqRow(faq) }
        }
    }
}

@Composable
private fun FaqRow(faq: Faq) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.animateContentSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(faq.questionRes), style = MaterialTheme.typography.bodyMedium)
                Text(if (expanded) "−" else "+", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (expanded) {
                Text(
                    text = stringResource(faq.answerRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
