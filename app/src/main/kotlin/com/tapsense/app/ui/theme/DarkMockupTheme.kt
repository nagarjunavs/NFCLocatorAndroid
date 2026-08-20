package com.tapsense.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Forces the color scheme `AntennaSilhouette` reads (`outline`/`primary`/`tertiary`) to their
 * dark-appropriate values, for the few places (Home's device card, Tap Guide, Tap Test) that
 * draw the phone mockup on a *hardcoded* dark background regardless of the app's own
 * light/dark [AppearanceMode].
 *
 * Without this, `AntennaSilhouette` reads the ambient `MaterialTheme.colorScheme.outline` -
 * correct when the app theme is light (a light outline shows up fine on the hardcoded dark
 * card), but when the app theme is *also* dark, `colorScheme.outline` becomes a dark color too,
 * rendering an almost-invisible outline on the equally-dark card. The mockup's background
 * doesn't follow [AppearanceMode] (by design, matching the source mockups), so its foreground
 * colors can't either.
 */
@Composable
fun DarkMockupColors(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = TapSensePalette.Aqua,
            onPrimary = TapSensePalette.DarkSurfaceDeep,
            tertiary = TapSensePalette.Aqua,
            onTertiary = TapSensePalette.DarkSurfaceDeep,
            outline = TapSensePalette.TextLightSecondary,
        ),
        typography = MaterialTheme.typography,
        content = content,
    )
}

/**
 * The solid device-shape fill for My Phone's Front tab (the only remaining caller - every
 * *back*-panel silhouette uses [TapSensePalette.PhoneBody] instead, see that token's KDoc) -
 * two different dark tones depending on the *app's* actual light/dark theme, matching the
 * design's own light-mode-card-vs-dark-mode-card distinction. Must be read *before* entering
 * [DarkMockupColors], which overrides the ambient scheme this reads from.
 */
@Composable
fun darkCardSilhouetteColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.DarkSurfaceDeep
    } else {
        TapSensePalette.Graphite
    }

/**
 * The outer card's own background on Home - deliberately a *different* dark tone than
 * [darkCardSilhouetteColor] (which fills the phone silhouette drawn on top of this card), so
 * the phone shape stays visible against its card instead of blending into it.
 */
@Composable
fun darkCardBackgroundColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.DarkSurfaceAlt
    } else {
        TapSensePalette.Ink
    }

/**
 * The top-left camera-bump accent every *back*-panel silhouette draws (Home, My Phone's Back
 * tab, Tap Guide, Tap Test, Onboarding) - a dark "cutout" tone that reads clearly against
 * [TapSensePalette.PhoneBody] regardless of app theme, since the body fill itself doesn't
 * change between light and dark (see that token's KDoc).
 */
@Composable
fun cameraBumpAccentColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.DarkSurfaceDeep
    } else {
        TapSensePalette.Ink
    }

/**
 * My Phone Back tab's silhouette body fill - [TapSensePalette.PhoneBody] in light mode, but a
 * step lighter ([TapSensePalette.PhoneBodyDark]) in dark mode, since this is the one back-panel
 * screen where the phone sits directly on the screen's own near-black background with no
 * intermediate card (Home/Tap Test wrap it in a dark mockup card first). Must be read *before*
 * entering [DarkMockupColors], same as [darkCardSilhouetteColor].
 */
@Composable
fun myPhoneBackBodyColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.PhoneBodyDark
    } else {
        TapSensePalette.PhoneBody
    }

/** Border to pair with [myPhoneBackBodyColor]. */
@Composable
fun myPhoneBackBorderColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.PhoneBodyBorderDark
    } else {
        TapSensePalette.PhoneBodyBorder
    }

/**
 * Tap Guide's silhouette body fill - unlike every other back-panel screen, dark mode here
 * swaps to a *light* body ([TapSensePalette.ReaderOuterLight], the same tone the reader-device
 * illustration itself uses in light mode) rather than reusing [TapSensePalette.PhoneBody],
 * since Tap Guide's dark card is noticeably less black than Home's/Tap Test's - a lighter body
 * reads more clearly there than the universal mid-gray does.
 */
@Composable
fun tapGuideBodyColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.ReaderOuterLight
    } else {
        TapSensePalette.PhoneBody
    }

/** Border to pair with [tapGuideBodyColor]. */
@Composable
fun tapGuideBorderColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.ReaderInnerLight
    } else {
        TapSensePalette.PhoneBodyBorder
    }

/** My Phone Front tab's inset "screen" fill - matches [cameraBumpAccentColor]'s dark-mode tone but has its own light-mode value. */
@Composable
fun screenInsetColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.HardwareCutoutDark
    } else {
        TapSensePalette.ScreenInsetLight
    }

/** My Phone Front tab's top-center notch - a step lighter than [screenInsetColor] so it reads as a distinct cutout. */
@Composable
fun screenNotchColor(): Color =
    if (MaterialTheme.colorScheme.background == TapSensePalette.DarkBg) {
        TapSensePalette.DarkSurfaceDeep
    } else {
        TapSensePalette.Ink
    }
