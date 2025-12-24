package com.muratcangzm.nerva.di

import org.koin.core.module.Module
import org.koin.dsl.module

fun viewModelModules(): List<Module> = listOf(
    module {
        // factory { LibraryViewModel(get(), ...) } vs.
    }
)