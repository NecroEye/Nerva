package com.muratcangzm.nerva.app.nav

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    //TODO iOS: no-op rn dont forget native swipe/back
}