package com.muratcangzm.nerva.app

import androidx.compose.runtime.Composable
import com.muratcangzm.nerva.design.NervaTheme

@Composable
fun NervaApp(content: @Composable () -> Unit) {
    NervaTheme {
        content()
    }
}