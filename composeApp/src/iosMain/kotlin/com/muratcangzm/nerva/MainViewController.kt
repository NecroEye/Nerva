package com.muratcangzm.nerva

import androidx.compose.ui.window.ComposeUIViewController
import com.muratcangzm.common.PlatformContext
import com.muratcangzm.nerva.di.KoinBootstrap

@Suppress("FunctionName")
fun MainViewController() = ComposeUIViewController {
    KoinBootstrap.init(PlatformContext())
    App()
}