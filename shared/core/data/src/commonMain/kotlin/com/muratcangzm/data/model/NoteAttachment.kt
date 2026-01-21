package com.muratcangzm.data.model

data class NoteAttachment(
    val id: String,
    val noteId: NoteId,
    val kind: NoteAttachmentKind,
    val uri: String,
    val label: String?,
    val isPrimary: Boolean,
    val createdAtEpochMs: Long
)

data class NoteAttachmentUpsert(
    val id: String,
    val kind: NoteAttachmentKind,
    val uri: String,
    val label: String?
)