package com.tapsense.app.ui.navigation

import android.nfc.NfcAdapter
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tapsense.app.ui.education.EducationRoute
import com.tapsense.app.ui.home.HomeRoute
import com.tapsense.app.ui.myphone.MyPhoneRoute
import com.tapsense.app.ui.onboarding.OnboardingRoute
import com.tapsense.app.ui.phoneselect.PhoneConfirmedRoute
import com.tapsense.app.ui.phoneselect.PhoneSelectionRoute
import com.tapsense.app.ui.privacy.PrivacyRoute
import com.tapsense.app.ui.settings.SettingsRoute
import com.tapsense.app.ui.splash.SplashRoute
import com.tapsense.app.ui.tapguide.TapGuideRoute
import com.tapsense.app.ui.taptest.TapTestRoute
import com.tapsense.app.ui.troubleshoot.TroubleshootRoute
import com.tapsense.app.util.openNfcSettingsSafely

@Composable
fun TapSenseNavHost(reducedMotion: Boolean, navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in TapSenseDestinations.BOTTOM_BAR_ROUTES
    val context = LocalContext.current
    // Hardware presence never changes at runtime, so a one-time check is sufficient - unlike
    // NfcStateObserver.isEnabled (on/off), this doesn't need live observation.
    val isNfcSupported = remember(context) { NfcAdapter.getDefaultAdapter(context) != null }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TapSenseBottomBar(
                    currentRoute = currentRoute,
                    onHomeClick = { navController.navigateTopLevel(TapSenseDestinations.HOME) },
                    onMyPhoneClick = { navController.navigateTopLevel(TapSenseDestinations.MY_PHONE) },
                    onTapGuideClick = { navController.navigate(TapSenseDestinations.TAP_GUIDE) },
                    onSettingsClick = { navController.navigateTopLevel(TapSenseDestinations.SETTINGS) },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TapSenseDestinations.SPLASH,
            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
        ) {
            composable(TapSenseDestinations.SPLASH) {
                SplashRoute(
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo(TapSenseDestinations.SPLASH) { inclusive = true }
                        }
                    },
                    reducedMotion = reducedMotion,
                )
            }
            composable(TapSenseDestinations.ONBOARDING) {
                OnboardingRoute(
                    onDone = {
                        navController.navigate(TapSenseDestinations.HOME) {
                            popUpTo(TapSenseDestinations.ONBOARDING) { inclusive = true }
                        }
                    },
                    onChooseDifferentPhone = { navController.navigate(TapSenseDestinations.PHONE_SELECTION) },
                    reducedMotion = reducedMotion,
                )
            }
            composable(TapSenseDestinations.HOME) {
                HomeRoute(
                    onStartTapGuide = { navController.navigate(TapSenseDestinations.TAP_GUIDE) },
                    onChangePhone = { navController.navigate(TapSenseDestinations.PHONE_SELECTION) },
                    onTapNotWorking = { navController.navigate(TapSenseDestinations.TROUBLESHOOT) },
                )
            }
            composable(TapSenseDestinations.MY_PHONE) {
                MyPhoneRoute(onTestLocation = { navController.navigate(TapSenseDestinations.TAP_TEST) })
            }
            composable(TapSenseDestinations.SETTINGS) {
                SettingsRoute(
                    isNfcSupported = isNfcSupported,
                    onPhoneModelClick = { navController.navigate(TapSenseDestinations.PHONE_SELECTION) },
                    onOpenNfcSettings = { context.openNfcSettingsSafely() },
                    onHelpCenterClick = { navController.navigate(TapSenseDestinations.TROUBLESHOOT) },
                    onPrivacyClick = { navController.navigate(TapSenseDestinations.PRIVACY) },
                )
            }
            composable(TapSenseDestinations.PHONE_SELECTION) {
                PhoneSelectionRoute(
                    onPhoneSelected = {
                        navController.navigate(TapSenseDestinations.PHONE_CONFIRMED) {
                            popUpTo(TapSenseDestinations.PHONE_SELECTION) { inclusive = true }
                        }
                    },
                    onUseMyPhone = { navController.popBackStack() },
                )
            }
            composable(TapSenseDestinations.PHONE_CONFIRMED) {
                PhoneConfirmedRoute(
                    onGoHome = { navController.navigateTopLevel(TapSenseDestinations.HOME) },
                    onChooseDifferent = {
                        navController.navigate(TapSenseDestinations.PHONE_SELECTION) {
                            popUpTo(TapSenseDestinations.PHONE_CONFIRMED) { inclusive = true }
                        }
                    },
                )
            }
            composable(TapSenseDestinations.TAP_GUIDE) {
                TapGuideRoute(
                    onRunTapTest = { navController.navigate(TapSenseDestinations.TAP_TEST) },
                    onClose = { navController.popBackStack() },
                    onNfcUnsupported = {
                        navController.navigate(TapSenseDestinations.TAP_TEST) {
                            popUpTo(TapSenseDestinations.TAP_GUIDE) { inclusive = true }
                        }
                    },
                )
            }
            composable(TapSenseDestinations.TAP_TEST) {
                TapTestRoute(
                    onCancel = { navController.popBackStack() },
                    onViewTapTips = { navController.navigate(TapSenseDestinations.TROUBLESHOOT) },
                )
            }
            composable(TapSenseDestinations.TROUBLESHOOT) {
                TroubleshootRoute(
                    isNfcSupported = isNfcSupported,
                    onOpenNfcSettings = { context.openNfcSettingsSafely() },
                    onRunTapTest = { navController.navigate(TapSenseDestinations.TAP_TEST) },
                    onViewTapZone = { navController.navigateTopLevel(TapSenseDestinations.MY_PHONE) },
                    onChoosePhone = { navController.navigate(TapSenseDestinations.PHONE_SELECTION) },
                    onLearnMore = { navController.navigate(TapSenseDestinations.EDUCATION) },
                )
            }
            composable(TapSenseDestinations.EDUCATION) {
                EducationRoute()
            }
            composable(TapSenseDestinations.PRIVACY) {
                PrivacyRoute()
            }
        }
    }
}

/** Navigates to a bottom-bar destination, clearing back stack up to the start so tabs don't stack. */
private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
