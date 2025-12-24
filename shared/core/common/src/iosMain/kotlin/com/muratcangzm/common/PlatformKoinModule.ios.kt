package com.muratcangzm.common

import org.koin.dsl.module

actual fun platformModule(platformContext: PlatformContext) = module {
    single { platformContext }
}