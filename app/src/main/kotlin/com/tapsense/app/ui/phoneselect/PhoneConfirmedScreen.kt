package com.tapsense.app.ui.phoneselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tapsense.app.R
import com.tapsense.app.ui.component.ConfidenceChip
import com.tapsense.app.ui.theme.tapSenseFilled
import com.tapsense.app.util.friendlyModelName

@Composable
fun PhoneConfirmedRoute(
    onGoHome: () -> Unit,
    onChooseDifferent: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneConfirmedViewModel = hiltViewModel(),
) {
    val profile by viewModel.confirmedProfile.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().safeDrawingPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp),
        ) {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Text(
            text = stringResource(R.string.phone_confirmed_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val current = profile
                if (current == null) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(current.model.friendlyModelName(), style = MaterialTheme.typography.titleSmall)
                        Text(
                            current.manufacturer.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ConfidenceChip(current.confidence)
                }
            }
        }

        Button(onClick = { viewModel.goHome(onGoHome) }, colors = ButtonDefaults.tapSenseFilled(), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text(stringResource(R.string.phone_confirmed_go_home))
        }
        TextButton(onClick = onChooseDifferent, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.phone_confirmed_choose_different))
        }
    }
}
