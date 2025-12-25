package com.muratcangzm.data.model

import kotlin.jvm.JvmInline

@JvmInline
value class NoteId(val value: String)

data class Note(
    val id: NoteId,
    val title: String,
    val content: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val pinned: Long,
)