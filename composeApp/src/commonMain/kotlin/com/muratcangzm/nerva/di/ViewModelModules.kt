package com.muratcangzm.nerva.di

import com.muratcangzm.nerva.feature.library.LibraryViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun viewModelModules(): List<Module> = listOf(
    module {
        factory { LibraryViewModel(noteRepository = get(), dispatchers = get()) }
    }
)