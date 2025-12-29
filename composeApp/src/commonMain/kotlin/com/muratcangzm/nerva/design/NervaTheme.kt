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
        gradientStart = NervaColors.Ocean,
        gradientEnd = NervaColors.Forest
    )
}

object NervaBranding {
    val current: NervaBrand
        @Composable get() = LocalNervaBrand.current
}

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = NervaColors.Ocean,
    onPrimary = NervaColors.TextOnDark,

    secondary = NervaColors.Forest,
    onSecondary = NervaColors.TextOnDark,

    tertiary = NervaColors.Mint,
    onTertiary = NervaColors.TextOnLight,

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
    primary = NervaColors.OceanDeep,
    onPrimary = Color.White,

    secondary = NervaColors.ForestDeep,
    onSecondary = Color.White,

    tertiary = NervaColors.Teal,
    onTertiary = Color.White,

    background = NervaColors.LightBg,
    onBackground = NervaColors.TextOnLight,

    surface = NervaColors.LightSurface,
    onSurface = NervaColors.TextOnLight,

    surfaceVariant = NervaColors.LightSurface2,
    onSurfaceVariant = NervaColors.TextMutedOnLight,

    outline = NervaColors.LightOutline,

    error = NervaColors.Error,
    onError = Color.White
)

@Composable
fun NervaTheme(
    dark: Boolean = true,
    dimens: NervaDimens = NervaDimens(),
    brand: NervaBrand = NervaBrand(
        gradientStart = NervaColors.Ocean,
        gradientEnd = NervaColors.Forest
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
