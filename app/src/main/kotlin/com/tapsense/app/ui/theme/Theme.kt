package com.tapsense.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tapsense.app.data.settings.AppearanceMode

/**
 * `primary`/`tertiary` are both mapped to the design's single aqua accent (light: `#35C6D9`,
 * dark: `#4FE0F0`) - the design's own rationale is "a single luminous aqua accent reserved for
 * the tap-zone marker so it reads as the one thing to look at on every screen." The shared
 * `nfc-locator-core` marker components already use `primary` for a solid (confident) marker and
 * `tertiary` for a dashed/pulsing (uncertain) one - reusing the same hue for both and letting
 * fill-vs-outline carry the confidence distinction matches that rationale exactly, rather than
 * introducing a second marker color the design never specifies.
 *
 * The `*Container` slots are deliberately *not* tonal derivatives of `primary` here (unlike
 * typical Material3 usage) - they're wired directly to the design's four distinct confidence-badge
 * colors (success green / teal / amber / neutral) from the style guide's "Confidence badges"
 * section, since `ConfidenceBadge` (in `nfc-locator-core`) reads confidence colors from exactly
 * these four container slots.
 */
private val LightColors = lightColorScheme(
    background = TapSensePalette.LightBg,
    onBackground = TapSensePalette.Ink,
    surface = TapSensePalette.LightSurface,
    onSurface = TapSensePalette.Ink,
    surfaceVariant = TapSensePalette.LightSurfaceAlt,
    onSurfaceVariant = TapSensePalette.Ink2,
    outline = TapSensePalette.LightOutline,
    outlineVariant = TapSensePalette.LightDivider,
    primary = TapSensePalette.Aqua,
    onPrimary = TapSensePalette.AquaLink,
    primaryContainer = TapSensePalette.SuccessContainer,
    onPrimaryContainer = TapSensePalette.SuccessOn,
    secondary = TapSensePalette.Graphite,
    onSecondary = TapSensePalette.LightSurface,
    secondaryContainer = TapSensePalette.ApproxContainer,
    onSecondaryContainer = TapSensePalette.ApproxOn,
    tertiary = TapSensePalette.Aqua,
    onTertiary = TapSensePalette.AquaLink,
    tertiaryContainer = TapSensePalette.AmberContainer,
    onTertiaryContainer = TapSensePalette.AmberOnStrong,
    error = TapSensePalette.Error,
    onError = TapSensePalette.LightSurface,
    errorContainer = TapSensePalette.AmberContainer,
    onErrorContainer = TapSensePalette.Error,
    // The design's actual primary-button color (graphite-on-white) - kept off the `primary`
    // slot itself since `primary` is deliberately aqua for the marker and six other ambient
    // reads (nav bar, NFC status text, etc.) that must stay aqua. See [tapSenseFilled].
    inverseSurface = TapSensePalette.Graphite,
    inverseOnSurface = TapSensePalette.LightSurface,
)

private val DarkColors = darkColorScheme(
    background = TapSensePalette.DarkBg,
    onBackground = TapSensePalette.TextLight,
    surface = TapSensePalette.DarkSurface,
    onSurface = TapSensePalette.TextLight,
    surfaceVariant = TapSensePalette.DarkSurfaceAlt,
    onSurfaceVariant = TapSensePalette.TextLightSecondary,
    outline = TapSensePalette.DarkOutline,
    outlineVariant = TapSensePalette.DarkDivider,
    primary = TapSensePalette.AquaDark,
    onPrimary = TapSensePalette.DarkSurfaceDeep,
    primaryContainer = TapSensePalette.DarkSurfaceAlt,
    onPrimaryContainer = TapSensePalette.AquaDark,
    secondary = TapSensePalette.TextLight,
    onSecondary = TapSensePalette.DarkBg,
    secondaryContainer = TapSensePalette.DarkSurfaceAlt,
    onSecondaryContainer = TapSensePalette.AquaDark,
    tertiary = TapSensePalette.AquaDark,
    onTertiary = TapSensePalette.DarkSurfaceDeep,
    tertiaryContainer = TapSensePalette.AmberContainerDark,
    onTertiaryContainer = TapSensePalette.Amber,
    error = TapSensePalette.Error,
    onError = TapSensePalette.TextLight,
    errorContainer = TapSensePalette.DarkSurfaceAlt,
    onErrorContainer = TapSensePalette.Error,
    inverseSurface = TapSensePalette.TextLight,
    inverseOnSurface = TapSensePalette.DarkSurfaceDeep,
)

/**
 * Colors for the design's primary filled button (graphite-on-white light / cream-on-black
 * dark) - deliberately not `ButtonDefaults.buttonColors()`'s default, since that reads
 * `colorScheme.primary`, which this theme reserves for the aqua tap-zone-marker accent.
 */
@Composable
fun ButtonDefaults.tapSenseFilled(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.inverseSurface,
    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
)

/**
 * Colors for the design's neutral outlined button (e.g. Tap Test's Cancel) - `onSurface`
 * content on a transparent container, distinct from `OutlinedButton`'s M3 default (which would
 * read as aqua here, since this theme reserves `colorScheme.primary` for the tap-zone marker
 * accent rather than Material's usual "brand color" role).
 */
@Composable
fun ButtonDefaults.tapSenseOutlined(): ButtonColors = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.onSurface,
)

/** Border to pair with [tapSenseOutlined] - `outline` reads as a visible neutral line in both themes. */
@Composable
fun tapSenseOutlinedBorder(): BorderStroke = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)

/**
 * Switch colors matching the design's explicit off-state thumb/track (M3's own default -
 * `outline`/`surfaceVariant` - reads too close to the background, especially in dark mode).
 * The checked (on) state is left at M3's default, which already reads correctly.
 */
@Composable
fun SwitchDefaults.tapSenseColors(): SwitchColors {
    val isDark = MaterialTheme.colorScheme.background == TapSensePalette.DarkBg
    return SwitchDefaults.colors(
        uncheckedThumbColor = if (isDark) TapSensePalette.SwitchOffThumbDark else MaterialTheme.colorScheme.surface,
        uncheckedTrackColor = if (isDark) TapSensePalette.SwitchOffTrackDark else TapSensePalette.SwitchOffTrackLight,
        uncheckedBorderColor = Color.Transparent,
    )
}

@Composable
fun TapSenseTheme(
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (appearanceMode) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = TapSenseTypography,
        content = content,
    )
}
