package com.muratcangzm.data.model

import kotlin.jvm.JvmInline

@JvmInline
value class NoteId(val value: String)

enum class NoteAttachmentKind {
    Photo,
    Pdf
}

data class NoteAttachmentPreview(
    val id: String,
    val kind: NoteAttachmentKind,
    val uri: String,
    val label: String? = null
)

data class Note(
    val id: NoteId,
    val title: String,
    val content: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val pinned: Long,

    val attachmentsCount: Int = 0,
    val primaryAttachment: NoteAttachmentPreview? = null
)

 fun com.muratcangzm.database.Note.toDomainNoAttachment(): Note = Note(
    id = NoteId(id),
    title = title,
    content = content,
    createdAtEpochMs = createdAt,
    updatedAtEpochMs = updatedAt,
    pinned = pinned
)
