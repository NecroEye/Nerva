package com.muratcangzm.nerva.di

import com.muratcangzm.common.PlatformContext
import com.muratcangzm.common.platformModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun appModules(platformContext: PlatformContext): List<Module> = listOf(
    platformModule(platformContext),
    module {
        // Şimdilik boş. DB/Data ekleyince buraya bağlayacağız.
    }
)
