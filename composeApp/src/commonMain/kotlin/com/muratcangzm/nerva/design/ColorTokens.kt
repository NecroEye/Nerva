package com.muratcangzm.nerva.design

import androidx.compose.ui.graphics.Color

object NervaColors {

    // ===== Brand (Ocean 🌊 + Forest 🌲) =====
    val Ocean = Color(0xFF2AA7FF)
    val OceanDeep = Color(0xFF0B5AA6)

    val Forest = Color(0xFF22C55E)
    val ForestDeep = Color(0xFF166534)

    val Mint = Color(0xFF5EEAD4)   // soft highlight / chips
    val Teal = Color(0xFF14B8A6)   // secondary accent (optional)

    // ===== Neutrals (Dark-first) =====
    val DarkBg = Color(0xFF06131A)
    val DarkSurface = Color(0xFF0A1B23)
    val DarkSurface2 = Color(0xFF0E2630)
    val DarkOutline = Color(0xFF1E3A46)

    // ===== Neutrals (Light) =====
    val LightBg = Color(0xFFF4FAFD)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurface2 = Color(0xFFEAF3F7)
    val LightOutline = Color(0xFFD2E3EC)

    // ===== Semantic =====
    val Success = Forest
    val Warning = Color(0xFFFBBF24) // amber
    val Error = Color(0xFFFB7185)   // modern pink-red

    // ===== Text =====
    val TextOnDark = Color(0xFFEAF6FF)
    val TextMutedOnDark = Color(0xFFA9C3D6)

    val TextOnLight = Color(0xFF0B1B23)
    val TextMutedOnLight = Color(0xFF4B6A7E)

    // ===== Backward-compatible aliases (keeps existing refs compiling) =====
    val Cyan = Ocean
    val Violet = Forest
    val Coral = Mint
}
