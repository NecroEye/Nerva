package com.muratcangzm.nerva.feature.note.shared

data class NoteDraft(
    val title: String,
    val content: String,
    val attachments: List<NoteAttachmentUi>,
)

data class NoteUi(
    val id: String,
    val title: String,
    val content: String,
    val updatedAtEpochMs: Long,
    val pinned: Boolean,
    val attachments: List<NoteAttachmentUi>,
)

enum class NoteAttachmentKind(val label: String) {
    Image("Image"),
    Pdf("PDF"),
}

data class NoteAttachmentUi(
    val id: String,
    val kind: NoteAttachmentKind,
    val displayName: String,
    val uri: String,
)

data class PickedAttachment(
    val id: String,
    val kind: NoteAttachmentKind,
    val displayName: String,
    val uri: String,
)
