package com.muratcangzm.common

import android.content.Context
import org.koin.dsl.module

actual fun platformModule(platformContext: PlatformContext) = module {
    single { platformContext }
    single<Context> { platformContext.appContext }
}