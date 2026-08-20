package com.tapsense.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw TapSense design-system palette (from the TapSense style guide), for composables that
 * need a specific brand token Material3's `ColorScheme` slots don't name directly - e.g. the
 * "graphite" brand color used for primary buttons, distinct from the "aqua" accent reserved
 * for the tap-zone marker (see [com.tapsense.app.ui.theme.TapSenseColorScheme] for how the two
 * map onto `ColorScheme`).
 */
object TapSensePalette {
    // Light
    val LightBg = Color(0xFFF6F5F1)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceAlt = Color(0xFFF1F0EC)
    val Ink = Color(0xFF211F1C)
    val Ink2 = Color(0xFF6B675F)
    val Ink3 = Color(0xFF8B8779)
    val LightOutline = Color(0xFFE4E1DA)
    val LightDivider = Color(0xFFEFEDE7)

    // Dark
    val DarkBg = Color(0xFF171613)
    val DarkSurface = Color(0xFF1F1D19)
    val DarkSurfaceAlt = Color(0xFF262420)
    val DarkSurfaceDeep = Color(0xFF100F0D)
    val DarkOutline = Color(0xFF34322C)
    val DarkDivider = Color(0xFF2A2823)
    val TextLight = Color(0xFFF3F1EB)
    val TextLightSecondary = Color(0xFFA6A199)

    // Brand / accents (shared both themes unless noted)
    val Graphite = Color(0xFF33312C)
    val Aqua = Color(0xFF35C6D9)
    val AquaDark = Color(0xFF4FE0F0)
    val AquaLink = Color(0xFF0E4B54)
    val Amber = Color(0xFFE0A63C)
    val AmberOn = Color(0xFF7A5A1E)
    val AmberOnStrong = Color(0xFF8C6317)
    val AmberContainer = Color(0xFFFBF2E2)
    val AmberContainerDark = Color(0xFF262420)
    val Success = Color(0xFF3FA66B)
    val SuccessOn = Color(0xFF245C3E)
    val SuccessContainer = Color(0xFFEAF6EF)
    val Error = Color(0xFFD1483C)
    val ApproxOn = Color(0xFF1B818E)
    val ApproxContainer = Color(0xFFE9FAFB)

    // Generic reader/terminal device illustration (Onboarding, Tap Guide) - a neutral warm
    // gray distinct from both the phone silhouette fill and the aqua marker accent, since it
    // represents hardware the phone taps against, not app guidance.
    val ReaderOuter = Color(0xFF413E37)
    val ReaderInner = Color(0xFF514E46)
    // Light-theme counterparts (Tap Guide is the one screen whose reader illustration sits on
    // a background that switches with AppearanceMode, so it needs both tone pairs).
    val ReaderOuterLight = Color(0xFFDAD6CC)
    val ReaderInnerLight = Color(0xFFC9C6BD)

    // My Phone's Back/Front segmented toggle - a track/selected-tab pair distinct from the
    // ambient surfaceVariant/surface tones, matching the design's explicit toggle colors
    // (which read too close to each other using M3's generic surface defaults).
    val ToggleTrackLight = Color(0xFFEAE7E0)
    val ToggleTrackDark = Color(0xFF2E2C27)
    val ToggleTabSelectedDark = Color(0xFF454239)

    // Settings switches, unchecked (off) state only - M3's default unchecked thumb/track
    // (outline/surfaceVariant) reads too close to the background, especially in dark mode. The
    // checked (on) state already reads well with M3 defaults and is left alone.
    val SwitchOffTrackLight = Color(0xFFC9C6BD)
    val SwitchOffTrackDark = Color(0xFF4C4A44)
    val SwitchOffThumbDark = Color(0xFFD8D5CC)

    // My Phone's Front screen-inset hardware-cutout accent (dark mode only) - distinct from
    // every other "dark" token above since dark-mode's own body fill (DarkSurfaceDeep) is
    // already near-black.
    val ScreenInsetLight = Color(0xFF0D0C0B)
    val HardwareCutoutDark = Color(0xFF000000)

    // Back-of-phone silhouette body/border - a warm mid-gray with its own outline, used
    // wherever the silhouette represents the phone's *back* panel (Home, My Phone's Back tab,
    // Tap Guide, Tap Test, Onboarding). Deliberately the same fill regardless of app light/dark
    // mode: unlike the old Graphite/DarkSurfaceDeep split it replaces, this tone plus its
    // outline already reads clearly against both a white/cream card and a near-black one, so it
    // doesn't need to change - only the camera-bump accent on top of it does (see
    // cameraBumpAccentColor()).
    val PhoneBody = Color(0xFF57544C)
    val PhoneBodyBorder = Color(0xFF6B675F)
    // My Phone's Back tab in dark mode has no intermediate card between the phone and the
    // screen's own near-black background (unlike Home's/Tap Test's dark mockup card), so it
    // needs a step lighter than PhoneBody to keep the same margin of contrast.
    val PhoneBodyDark = Color(0xFF68655C)
    val PhoneBodyBorderDark = Color(0xFF7A766C)
}
