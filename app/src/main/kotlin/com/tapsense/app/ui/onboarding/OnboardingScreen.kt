package com.tapsense.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import com.tapsense.app.R
import com.tapsense.app.ui.component.AntennaMarker
import com.tapsense.app.ui.component.ConfidenceChip
import com.tapsense.app.ui.component.ReaderDeviceIllustration
import com.tapsense.app.ui.theme.TapSensePalette
import com.tapsense.app.ui.theme.cameraBumpAccentColor
import com.tapsense.app.ui.theme.tapGuideBodyColor
import com.tapsense.app.ui.theme.tapGuideBorderColor
import com.tapsense.app.ui.theme.tapSenseFilled
import com.tapsense.app.util.friendlyModelName
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute(
    onDone: () -> Unit,
    onStartGuidedWalkthrough: () -> Unit,
    onChooseDifferentPhone: () -> Unit,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val autoDetectedProfile by viewModel.autoDetectedProfile.collectAsState()
    val antennaState by viewModel.antennaState.collectAsState()

    OnboardingScreen(
        autoDetectedProfile = autoDetectedProfile,
        antennaState = antennaState,
        onGetStarted = { viewModel.completeOnboarding(onStartGuidedWalkthrough) },
        onSkip = { viewModel.completeOnboarding(onDone) },
        onChooseDifferentPhone = onChooseDifferentPhone,
        reducedMotion = reducedMotion,
        modifier = modifier,
    )
}

@Composable
private fun OnboardingScreen(
    autoDetectedProfile: DeviceAntennaProfile?,
    antennaState: AntennaLocatorUiState?,
    onGetStarted: () -> Unit,
    onSkip: () -> Unit,
    onChooseDifferentPhone: () -> Unit,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = stringResource(R.string.onboarding_page1_title),
                    body = stringResource(R.string.onboarding_page1_body),
                    antennaState = antennaState,
                    reducedMotion = reducedMotion,
                    showReaderDevice = false,
                )
                1 -> OnboardingPage(
                    title = stringResource(R.string.onboarding_page2_title),
                    body = stringResource(R.string.onboarding_page2_body),
                    antennaState = antennaState,
                    reducedMotion = reducedMotion,
                    showReaderDevice = true,
                )
                else -> OnboardingFinalPage(
                    autoDetectedProfile = autoDetectedProfile,
                    antennaState = antennaState,
                    reducedMotion = reducedMotion,
                )
            }
        }

        PageIndicator(pageCount = 3, currentPage = pagerState.currentPage, modifier = Modifier.padding(bottom = 20.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            if (pagerState.currentPage < 2) {
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    colors = ButtonDefaults.tapSenseFilled(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_continue))
                }
            } else {
                Button(onClick = onGetStarted, colors = ButtonDefaults.tapSenseFilled(), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.onboarding_start_walkthrough))
                }
                TextButton(onClick = onChooseDifferentPhone, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.phone_confirmed_choose_different))
                }
            }
        }

        Box(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OnboardingPage(
    title: String,
    body: String,
    antennaState: AntennaLocatorUiState?,
    reducedMotion: Boolean,
    showReaderDevice: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showReaderDevice) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OnboardingAntennaVisual(
                    antennaState = antennaState,
                    reducedMotion = reducedMotion,
                    modifier = Modifier.width(130.dp).height(280.dp),
                )
                ReaderDeviceIllustration(
                    outerColor = TapSensePalette.ReaderOuter,
                    innerColor = TapSensePalette.ReaderInner,
                    modifier = Modifier.size(120.dp),
                )
            }
        } else {
            OnboardingAntennaVisual(
                antennaState = antennaState,
                reducedMotion = reducedMotion,
                modifier = Modifier.width(150.dp).height(320.dp),
            )
        }
        Box(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.height(12.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OnboardingFinalPage(
    autoDetectedProfile: DeviceAntennaProfile?,
    antennaState: AntennaLocatorUiState?,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingAntennaVisual(
            antennaState = antennaState,
            reducedMotion = reducedMotion,
            modifier = Modifier.width(140.dp).height(300.dp),
        )
        Box(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_page3_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        Text(
            text = stringResource(R.string.onboarding_page3_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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
                if (autoDetectedProfile == null) {
                    Text(
                        text = stringResource(R.string.phone_selection_loading),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(autoDetectedProfile.model.friendlyModelName(), style = MaterialTheme.typography.titleSmall)
                        Text(
                            autoDetectedProfile.manufacturer.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ConfidenceChip(confidence = autoDetectedProfile.confidence)
                }
            }
        }
    }
}

/**
 * The real per-device marker (same [AntennaMarker] every other screen renders), shown once
 * resolved - a spinner beforehand rather than a decorative placeholder, since a wrong-looking
 * marker during the very first thing a new user sees would be worse than a brief wait. Shared by
 * all 3 onboarding pages so the walkthrough demonstrates this device's actual tap zone, not a
 * generic illustration.
 */
@Composable
private fun OnboardingAntennaVisual(antennaState: AntennaLocatorUiState?, reducedMotion: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (antennaState != null) {
            AntennaMarker(
                state = antennaState,
                reducedMotion = reducedMotion,
                modifier = Modifier.fillMaxSize(),
                silhouetteColor = tapGuideBodyColor(),
                silhouetteBorderColor = tapGuideBorderColor(),
                showCameraBump = true,
                cameraBumpColor = cameraBumpAccentColor(),
            )
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(if (selected) 20.dp else 6.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(3.dp),
                    ),
            )
        }
    }
}
