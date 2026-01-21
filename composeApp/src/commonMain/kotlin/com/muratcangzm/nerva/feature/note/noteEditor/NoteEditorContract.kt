package com.muratcangzm.nerva.feature.note.noteEditor

import com.muratcangzm.nerva.feature.note.shared.NoteAttachmentUi

data class NoteEditorState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val attachments: List<NoteAttachmentUi> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
)

sealed interface NoteEditorAction {
    data class TitleChanged(val value: String) : NoteEditorAction
    data class ContentChanged(val value: String) : NoteEditorAction
    data object AddImageClicked : NoteEditorAction
    data object AddPdfClicked : NoteEditorAction
    data class RemoveAttachment(val id: String) : NoteEditorAction
    data object SaveClicked : NoteEditorAction
}

sealed interface NoteEditorEffect {
    data object PickImage : NoteEditorEffect
    data object PickPdf : NoteEditorEffect
    data class Saved(val noteId: String) : NoteEditorEffect
    data class Message(val text: String) : NoteEditorEffect
}
