package com.muratcangzm.nerva.design

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

object TagColorDefaults {

    fun tagSeedColor(seed: String): Color {
        val palette = listOf(
            NervaColors.Ocean,
            NervaColors.Forest,
            NervaColors.Teal,
            NervaColors.Mint,
            NervaColors.OceanDeep,
            NervaColors.ForestDeep
        )
        return palette[abs(seed.hashCode()) % palette.size]
    }

    fun container(color: Color): Color = color.copy(alpha = 0.18f)
    fun content(color: Color): Color = color.copy(alpha = 0.95f)
}
