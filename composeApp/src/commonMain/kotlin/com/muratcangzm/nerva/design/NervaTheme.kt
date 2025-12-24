package com.muratcangzm.nerva.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NervaBrand(
    val gradientStart: Color,
    val gradientEnd: Color
)

internal val LocalNervaBrand = staticCompositionLocalOf {
    NervaBrand(
        gradientStart = NervaColors.Cyan,
        gradientEnd = NervaColors.Violet
    )
}

object NervaBranding {
    val current: NervaBrand
        @Composable get() = LocalNervaBrand.current
}

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = NervaColors.Cyan,
    onPrimary = NervaColors.DarkBg,

    secondary = NervaColors.Violet,
    onSecondary = NervaColors.TextOnDark,

    tertiary = NervaColors.Coral,
    onTertiary = NervaColors.TextOnDark,

    background = NervaColors.DarkBg,
    onBackground = NervaColors.TextOnDark,

    surface = NervaColors.DarkSurface,
    onSurface = NervaColors.TextOnDark,

    surfaceVariant = NervaColors.DarkSurface2,
    onSurfaceVariant = NervaColors.TextMutedOnDark,

    outline = NervaColors.DarkOutline,

    error = NervaColors.Error,
    onError = NervaColors.TextOnDark
)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = NervaColors.Violet,
    onPrimary = NervaColors.LightSurface,

    secondary = NervaColors.Cyan,
    onSecondary = NervaColors.TextOnLight,

    tertiary = NervaColors.Coral,
    onTertiary = NervaColors.LightSurface,

    background = NervaColors.LightBg,
    onBackground = NervaColors.TextOnLight,

    surface = NervaColors.LightSurface,
    onSurface = NervaColors.TextOnLight,

    surfaceVariant = NervaColors.LightSurface2,
    onSurfaceVariant = NervaColors.TextMutedOnLight,

    outline = NervaColors.LightOutline,

    error = NervaColors.Error,
    onError = NervaColors.LightSurface
)

@Composable
fun NervaTheme(
    dark: Boolean = true,
    dimens: NervaDimens = NervaDimens(),
    brand: NervaBrand = NervaBrand(
        gradientStart = NervaColors.Cyan,
        gradientEnd = NervaColors.Violet
    ),
    content: @Composable () -> Unit
) {
    val scheme = if (dark) DarkScheme else LightScheme

    CompositionLocalProvider(
        LocalNervaDimens provides dimens,
        LocalNervaBrand provides brand
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = NervaTypography.typography,
            shapes = NervaShapes,
            content = content
        )
    }
}
