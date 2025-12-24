package com.muratcangzm.nerva.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object NervaTypography {
    private val base = FontFamily.SansSerif

    val typography: Typography = Typography(
        displaySmall = TextStyle(
            fontFamily = base, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = base, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = base, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = base, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = base, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = base, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp
        ),
        labelLarge = TextStyle(
            fontFamily = base, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp
        )
    )
}
