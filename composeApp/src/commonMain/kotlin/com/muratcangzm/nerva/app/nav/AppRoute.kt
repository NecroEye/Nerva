package com.muratcangzm.nerva.app.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {

    @Serializable
    data object Library : AppRoute

    @Serializable
    data object Schedule : AppRoute

    @Serializable
    data object NoteCreate : AppRoute

    @Serializable
    data class NoteEdit(val noteId: String) : AppRoute
}