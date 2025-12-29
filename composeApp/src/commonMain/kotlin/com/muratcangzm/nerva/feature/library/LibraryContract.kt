package com.muratcangzm.nerva.feature.library

import androidx.compose.runtime.Immutable

@Immutable
data class LibraryState(
    val query: String = "",
    val isLoading: Boolean = true,
    val notes: List<LibraryNoteItem> = emptyList(),
    val errorMessage: String? = null,
    val pendingDeletionIds: Set<String> = emptySet()
)

enum class LibraryReorderSection { Pinned, Normal }

@Immutable
data class LibraryNoteItem(
    val id: String,
    val title: String,
    val preview: String,
    val pinned: Long,
    val updatedAtEpochMs: Long,
    val orderIndex: Long = 0L,
    val tags: List<String> = emptyList(),
    val attachmentsCount: Int = 0,
    val tagColorArgb: Int = DEFAULT_TAG_COLOR_ARGB,
    val attachmentPreviews: List<LibraryAttachmentPreview> = emptyList()
)

@Immutable
data class LibraryAttachmentPreview(
    val id: String,
    val kind: LibraryAttachmentKind,
    val uri: String,
    val label: String? = null
)

enum class LibraryAttachmentKind { Photo, Pdf }

sealed interface LibraryAction {
    data class QueryChanged(val value: String) : LibraryAction
    data class OpenNote(val id: String) : LibraryAction
    data class TogglePin(val id: String, val pinned: Long) : LibraryAction
    data class DeleteNote(val id: String) : LibraryAction
    data class Reorder(val section: LibraryReorderSection, val fromIndex: Int, val toIndex: Int) : LibraryAction
    data object CreateNote : LibraryAction
}

sealed interface LibraryEffect {
    data class NavigateToNote(val id: String) : LibraryEffect
    data class ShowMessage(val message: String) : LibraryEffect
}

const val DEFAULT_TAG_COLOR_ARGB: Int = 0x662DE2E6
