package com.muratcangzm.nerva.di

import com.muratcangzm.common.PlatformContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

object KoinBootstrap {
    private var started: KoinApplication? = null

    fun init(platformContext: PlatformContext): KoinApplication {
        started?.let { return it }

        val allModules = buildList {
            addAll(appModules(platformContext))
            addAll(viewModelModules())
        }

        return startKoin {
            modules(allModules)
        }.also { started = it }
    }
}
