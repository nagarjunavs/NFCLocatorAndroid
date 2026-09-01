package com.tapsense.app.ui.tapguide

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tapsense.app.R
import com.tapsense.app.ui.component.AntennaMarker
import com.tapsense.app.ui.component.ReaderDeviceIllustration
import com.tapsense.app.ui.theme.TapSensePalette
import com.tapsense.app.ui.theme.cameraBumpAccentColor
import com.tapsense.app.ui.theme.tapGuideBodyColor
import com.tapsense.app.ui.theme.tapGuideBorderColor
import com.tapsense.app.ui.theme.tapSenseFilled

/**
 * The whole phone silhouette wobbles side to side during the walkthrough - a "move your phone
 * to find the reader" motion cue, independent of confidence (unlike the marker's own pulse/
 * sweep, which is confidence-driven). Matches the design's `tsSweep` keyframe: ±6dp, ease-in-out,
 * ~2.4s full cycle.
 */
@Composable
private fun sweepOffset(reducedMotion: Boolean): Dp {
    if (reducedMotion) return 0.dp
    val infiniteTransition = rememberInfiniteTransition(label = "tap_guide_sweep")
    val offsetPx by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tap_guide_sweep_offset",
    )
    return offsetPx.dp
}

@Composable
fun TapGuideRoute(
    onRunTapTest: () -> Unit,
    onClose: () -> Unit,
    onNfcUnsupported: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TapGuideViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // A device with no NFC hardware can never complete the walkthrough's end goal (a real tap
    // test), so skip the guided steps entirely and land straight on Tap Test's own, already-
    // correct "no NFC hardware" screen rather than showing a second near-duplicate message here.
    LaunchedEffect(uiState.isNfcSupported) {
        if (!uiState.isNfcSupported) {
            onNfcUnsupported()
        }
    }

    TapGuideScreen(
        uiState = uiState,
        onRunTapTest = onRunTapTest,
        onClose = onClose,
        modifier = modifier,
    )
}

@Composable
private fun TapGuideScreen(
    uiState: TapGuideUiState,
    onRunTapTest: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val isDark = MaterialTheme.colorScheme.background == TapSensePalette.DarkBg
    val readerOuter = if (isDark) TapSensePalette.ReaderOuter else TapSensePalette.ReaderOuterLight
    val readerInner = if (isDark) TapSensePalette.ReaderInner else TapSensePalette.ReaderInnerLight

    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp), horizontalArrangement = Arrangement.End) {
                val closeDescription = stringResource(R.string.tap_guide_close_content_description)
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

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val state = uiState.antennaState
                if (state != null) {
                    AntennaMarker(
                        state = state,
                        reducedMotion = uiState.reduceMotion,
                        modifier = Modifier
                            .width(140.dp)
                            .height(300.dp)
                            .offset(x = sweepOffset(uiState.reduceMotion)),
                        silhouetteColor = tapGuideBodyColor(),
                        silhouetteBorderColor = tapGuideBorderColor(),
                        showCameraBump = true,
                        cameraBumpColor = cameraBumpAccentColor(),
                    )
                    ReaderDeviceIllustration(
                        outerColor = readerOuter,
                        innerColor = readerInner,
                        modifier = Modifier.size(150.dp),
                    )
                }
            }

            val step = TAP_GUIDE_STEPS[currentStep]
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                Text(
                    text = stringResource(R.string.tap_guide_step_of, currentStep + 1, TAP_GUIDE_STEPS.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(step.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = stringResource(step.bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TAP_GUIDE_STEPS.forEachIndexed { index, _ ->
                    val complete = index <= currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(2.dp),
                            ),
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (currentStep < TAP_GUIDE_STEPS.lastIndex) {
                    Button(
                        onClick = { currentStep += 1 },
                        colors = ButtonDefaults.tapSenseFilled(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.tap_guide_next))
                    }
                } else {
                    Button(
                        onClick = onRunTapTest,
                        colors = ButtonDefaults.tapSenseFilled(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.tap_guide_step5_action))
                    }
                }
            }
        }
    }
}
