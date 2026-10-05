package com.edrl.stickerbridge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The app's own colours: a green that sits between TikTok and WhatsApp without being either, and
// a warm pink reserved for likes. The window background in res/values/colors.xml matches them.
private object Light {
    const val PRIMARY = 0xFF006C4C
    const val ON_PRIMARY = 0xFFFFFFFF
    const val PRIMARY_CONTAINER = 0xFF9BE7C4
    const val ON_PRIMARY_CONTAINER = 0xFF002114
    const val SECONDARY = 0xFF4C6358
    const val ON_SECONDARY = 0xFFFFFFFF
    const val SECONDARY_CONTAINER = 0xFFD5EBDD
    const val ON_SECONDARY_CONTAINER = 0xFF092017
    const val TERTIARY = 0xFFB3305C
    const val ON_TERTIARY = 0xFFFFFFFF
    const val TERTIARY_CONTAINER = 0xFFFFD9E1
    const val ON_TERTIARY_CONTAINER = 0xFF3F001A
    const val BACKGROUND = 0xFFF8FAF6
    const val ON_BACKGROUND = 0xFF191C1A
    const val SURFACE = 0xFFF8FAF6
    const val ON_SURFACE = 0xFF191C1A
    const val SURFACE_VARIANT = 0xFFDDE5DE
    const val ON_SURFACE_VARIANT = 0xFF414943
    const val SURFACE_CONTAINER_LOWEST = 0xFFFFFFFF
    const val SURFACE_CONTAINER_LOW = 0xFFF1F5F0
    const val SURFACE_CONTAINER = 0xFFEBF0EA
    const val SURFACE_CONTAINER_HIGH = 0xFFE4EAE4
    const val SURFACE_CONTAINER_HIGHEST = 0xFFDEE4DE
    const val OUTLINE = 0xFF717972
    const val OUTLINE_VARIANT = 0xFFC0C9C1
}

private object Dark {
    const val PRIMARY = 0xFF7FDAB1
    const val ON_PRIMARY = 0xFF003825
    const val PRIMARY_CONTAINER = 0xFF005138
    const val ON_PRIMARY_CONTAINER = 0xFF9BE7C4
    const val SECONDARY = 0xFFB3CCBE
    const val ON_SECONDARY = 0xFF1F352B
    const val SECONDARY_CONTAINER = 0xFF354B41
    const val ON_SECONDARY_CONTAINER = 0xFFCFE9DA
    const val TERTIARY = 0xFFFFB1C5
    const val ON_TERTIARY = 0xFF65002E
    const val TERTIARY_CONTAINER = 0xFF8E1345
    const val ON_TERTIARY_CONTAINER = 0xFFFFD9E1
    const val BACKGROUND = 0xFF101411
    const val ON_BACKGROUND = 0xFFE0E4DE
    const val SURFACE = 0xFF101411
    const val ON_SURFACE = 0xFFE0E4DE
    const val SURFACE_VARIANT = 0xFF414943
    const val ON_SURFACE_VARIANT = 0xFFC0C9C1
    const val SURFACE_CONTAINER_LOWEST = 0xFF0B0F0C
    const val SURFACE_CONTAINER_LOW = 0xFF191C1A
    const val SURFACE_CONTAINER = 0xFF1D211E
    const val SURFACE_CONTAINER_HIGH = 0xFF272B28
    const val SURFACE_CONTAINER_HIGHEST = 0xFF323633
    const val OUTLINE = 0xFF8A938C
    const val OUTLINE_VARIANT = 0xFF414943
}

private val LightColors =
    lightColorScheme(
        primary = Color(Light.PRIMARY),
        onPrimary = Color(Light.ON_PRIMARY),
        primaryContainer = Color(Light.PRIMARY_CONTAINER),
        onPrimaryContainer = Color(Light.ON_PRIMARY_CONTAINER),
        secondary = Color(Light.SECONDARY),
        onSecondary = Color(Light.ON_SECONDARY),
        secondaryContainer = Color(Light.SECONDARY_CONTAINER),
        onSecondaryContainer = Color(Light.ON_SECONDARY_CONTAINER),
        tertiary = Color(Light.TERTIARY),
        onTertiary = Color(Light.ON_TERTIARY),
        tertiaryContainer = Color(Light.TERTIARY_CONTAINER),
        onTertiaryContainer = Color(Light.ON_TERTIARY_CONTAINER),
        background = Color(Light.BACKGROUND),
        onBackground = Color(Light.ON_BACKGROUND),
        surface = Color(Light.SURFACE),
        onSurface = Color(Light.ON_SURFACE),
        surfaceVariant = Color(Light.SURFACE_VARIANT),
        onSurfaceVariant = Color(Light.ON_SURFACE_VARIANT),
        surfaceContainerLowest = Color(Light.SURFACE_CONTAINER_LOWEST),
        surfaceContainerLow = Color(Light.SURFACE_CONTAINER_LOW),
        surfaceContainer = Color(Light.SURFACE_CONTAINER),
        surfaceContainerHigh = Color(Light.SURFACE_CONTAINER_HIGH),
        surfaceContainerHighest = Color(Light.SURFACE_CONTAINER_HIGHEST),
        outline = Color(Light.OUTLINE),
        outlineVariant = Color(Light.OUTLINE_VARIANT),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(Dark.PRIMARY),
        onPrimary = Color(Dark.ON_PRIMARY),
        primaryContainer = Color(Dark.PRIMARY_CONTAINER),
        onPrimaryContainer = Color(Dark.ON_PRIMARY_CONTAINER),
        secondary = Color(Dark.SECONDARY),
        onSecondary = Color(Dark.ON_SECONDARY),
        secondaryContainer = Color(Dark.SECONDARY_CONTAINER),
        onSecondaryContainer = Color(Dark.ON_SECONDARY_CONTAINER),
        tertiary = Color(Dark.TERTIARY),
        onTertiary = Color(Dark.ON_TERTIARY),
        tertiaryContainer = Color(Dark.TERTIARY_CONTAINER),
        onTertiaryContainer = Color(Dark.ON_TERTIARY_CONTAINER),
        background = Color(Dark.BACKGROUND),
        onBackground = Color(Dark.ON_BACKGROUND),
        surface = Color(Dark.SURFACE),
        onSurface = Color(Dark.ON_SURFACE),
        surfaceVariant = Color(Dark.SURFACE_VARIANT),
        onSurfaceVariant = Color(Dark.ON_SURFACE_VARIANT),
        surfaceContainerLowest = Color(Dark.SURFACE_CONTAINER_LOWEST),
        surfaceContainerLow = Color(Dark.SURFACE_CONTAINER_LOW),
        surfaceContainer = Color(Dark.SURFACE_CONTAINER),
        surfaceContainerHigh = Color(Dark.SURFACE_CONTAINER_HIGH),
        surfaceContainerHighest = Color(Dark.SURFACE_CONTAINER_HIGHEST),
        outline = Color(Dark.OUTLINE),
        outlineVariant = Color(Dark.OUTLINE_VARIANT),
    )

/** The app's look, in light and dark. */
@Composable
fun StickerBridgeTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
