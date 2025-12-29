package com.muratcangzm.nerva.feature.library.components.background

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.muratcangzm.nerva.design.NervaBranding

@Composable
fun BrandGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val brand = NervaBranding.current

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        brand.gradientStart.copy(alpha = 1f),
                        brand.gradientEnd.copy(alpha = 1f)
                    )
                )
            )
    ) {
        content()
    }
}
