package com.muratcangzm.nerva.di

import com.muratcangzm.common.PlatformContext
import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.common.coroutines.DefaultAppDispatchers
import com.muratcangzm.common.platformModule
import com.muratcangzm.data.repo.NoteRepository
import com.muratcangzm.data.repo.SqlDelightNoteRepository
import com.muratcangzm.database.NervaDatabaseProvider
import com.muratcangzm.nerva.app.nav.AppNavigator
import org.koin.core.module.Module
import org.koin.dsl.module

fun appModules(platformContext: PlatformContext): List<Module> = listOf(
    platformModule(platformContext),
    module {
        single<AppDispatchers> { DefaultAppDispatchers }

        single { NervaDatabaseProvider(get()).create() }

        single<NoteRepository> { SqlDelightNoteRepository(get(), get()) }
    }
)
