package com.muratcangzm.nerva.app.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable data object Library : AppRoute
    @Serializable data class NoteDetails(val noteId: String) : AppRoute
}