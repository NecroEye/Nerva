package com.muratcangzm.nerva.feature.library

data class LibraryState(
    val query: String = "",
    val isLoading: Boolean = true,
    val notes: List<LibraryNoteItem> = emptyList(),
    val errorMessage: String? = null
)

data class LibraryNoteItem(
    val id: String,
    val title: String,
    val preview: String,
    val pinned: Long,
    val updatedAtEpochMs: Long
)

sealed interface LibraryAction {
    data class QueryChanged(val value: String) : LibraryAction
    data object CreateNote : LibraryAction
    data class OpenNote(val id: String) : LibraryAction
    data class TogglePin(val id: String, val pinned: Long) : LibraryAction
    data class DeleteNote(val id: String) : LibraryAction
}

sealed interface LibraryEffect {
    data class NavigateToNote(val id: String) : LibraryEffect
    data class ShowMessage(val message: String) : LibraryEffect
}