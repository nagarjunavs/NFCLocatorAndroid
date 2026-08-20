package com.tapsense.app.ui.navigation

/** Flat route table - a bottom bar is shown/hidden per-route rather than via a nested graph, see [TapSenseNavHost]. */
object TapSenseDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val MY_PHONE = "my_phone"
    const val SETTINGS = "settings"
    const val PHONE_SELECTION = "phone_selection"
    const val PHONE_CONFIRMED = "phone_confirmed"
    const val TAP_GUIDE = "tap_guide"
    const val TAP_TEST = "tap_test"
    const val TROUBLESHOOT = "troubleshoot"
    const val EDUCATION = "education"
    const val PRIVACY = "privacy"

    /** Routes that show the persistent bottom navigation bar. */
    val BOTTOM_BAR_ROUTES = setOf(HOME, MY_PHONE, SETTINGS)
}
