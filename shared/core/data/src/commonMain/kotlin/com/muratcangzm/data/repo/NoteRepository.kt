package com.muratcangzm.data.repo

import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteAttachment
import com.muratcangzm.data.model.NoteAttachmentUpsert
import com.muratcangzm.data.model.NoteId
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(query: String?): Flow<List<Note>>

    fun observeNote(id: NoteId): Flow<Note?>
    fun observeAttachments(noteId: NoteId): Flow<List<NoteAttachment>>

    suspend fun getById(id: NoteId): Note?

    suspend fun upsert(note: Note)
    suspend fun deleteById(id: NoteId)

    suspend fun setPinned(id: NoteId, pinned: Long, updatedAtEpochMs: Long)

    suspend fun updateContent(
        id: NoteId,
        title: String,
        content: String,
        updatedAtEpochMs: Long
    )

    suspend fun replaceAttachments(
        noteId: NoteId,
        attachments: List<NoteAttachmentUpsert>,
        nowEpochMs: Long
    )
}
