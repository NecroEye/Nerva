package com.muratcangzm.data.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteAttachment
import com.muratcangzm.data.model.NoteAttachmentKind
import com.muratcangzm.data.model.NoteAttachmentPreview
import com.muratcangzm.data.model.NoteAttachmentUpsert
import com.muratcangzm.data.model.NoteId
import com.muratcangzm.data.model.toDomainNoAttachment
import com.muratcangzm.database.NervaDatabase
import com.muratcangzm.database.Note_attachment
import com.muratcangzm.database.SearchWithPrimaryAttachment
import com.muratcangzm.database.SelectAllWithPrimaryAttachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightNoteRepository(
    private val db: NervaDatabase,
    private val dispatchers: AppDispatchers
) : NoteRepository {

    override fun observeNotes(query: String?): Flow<List<Note>> {
        val q = query?.trim().orEmpty()

        return if (q.isEmpty()) {
            db.noteQueries
                .selectAllWithPrimaryAttachment()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { rows -> rows.map(SelectAllWithPrimaryAttachment::toDomainNote) }
        } else {
            db.noteQueries
                .searchWithPrimaryAttachment(value_ = q)
                .asFlow()
                .mapToList(dispatchers.io)
                .map { rows -> rows.map(SearchWithPrimaryAttachment::toDomainNote) }
        }
    }

    override fun observeNote(id: NoteId): Flow<Note?> {
        return db.noteQueries
            .selectById(id.value)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { it?.toDomainNoAttachment() }
    }

    override fun observeAttachments(noteId: NoteId): Flow<List<NoteAttachment>> {
        return db.noteQueries
            .selectAttachmentsByNoteId(noteId.value)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.map(Note_attachment::toDomainAttachment) }
    }

    override suspend fun getById(id: NoteId): Note? {
        return withContext(dispatchers.io) {
            db.noteQueries.selectById(id.value)
                .executeAsOneOrNull()
                ?.toDomainNoAttachment()
        }
    }

    override suspend fun upsert(note: Note) {
        withContext(dispatchers.io) {
            db.noteQueries.upsert(
                id = note.id.value,
                title = note.title,
                content = note.content,
                createdAt = note.createdAtEpochMs, 
                updatedAt = note.updatedAtEpochMs,
                pinned = note.pinned
            )
        }
    }

    override suspend fun deleteById(id: NoteId) {
        withContext(dispatchers.io) {
            db.noteQueries.deleteById(id.value)
        }
    }

    override suspend fun setPinned(id: NoteId, pinned: Long, updatedAtEpochMs: Long) {
        withContext(dispatchers.io) {
            db.noteQueries.setPinned(
                id = id.value,
                pinned = pinned,
                updatedAt = updatedAtEpochMs
            )
        }
    }

    override suspend fun updateContent(id: NoteId, title: String, content: String, updatedAtEpochMs: Long) {
        withContext(dispatchers.io) {
            db.noteQueries.updateContent(
                id = id.value,
                title = title,
                content = content,
                updatedAt = updatedAtEpochMs
            )
        }
    }

    override suspend fun replaceAttachments(
        noteId: NoteId,
        attachments: List<NoteAttachmentUpsert>,
        nowEpochMs: Long
    ) {
        withContext(dispatchers.io) {
            db.transaction {
                db.noteQueries.deleteAttachmentsByNoteId(noteId.value)

                attachments.forEachIndexed { index, a ->
                    db.noteQueries.insertAttachment(
                        id = a.id,
                        noteId = noteId.value,
                        kind = a.kind.name,
                        uri = a.uri,
                        label = a.label,
                        isPrimary = if (index == 0) 1L else 0L,
                        createdAt = nowEpochMs
                    )
                }
            }
        }
    }
}

private fun SelectAllWithPrimaryAttachment.toDomainNote(): Note = Note(
    id = NoteId(id),
    title = title,
    content = content,
    createdAtEpochMs = createdAt,
    updatedAtEpochMs = updatedAt,
    pinned = pinned,
    attachmentsCount = attachmentsCount.toInt(),
    primaryAttachment = toPrimaryAttachmentOrNull()
)

private fun SearchWithPrimaryAttachment.toDomainNote(): Note = Note(
    id = NoteId(id),
    title = title,
    content = content,
    createdAtEpochMs = createdAt,
    updatedAtEpochMs = updatedAt,
    pinned = pinned,
    attachmentsCount = attachmentsCount.toInt(),
    primaryAttachment = toPrimaryAttachmentOrNull()
)

private fun SelectAllWithPrimaryAttachment.toPrimaryAttachmentOrNull(): NoteAttachmentPreview? {
    val attId = primaryAttachmentId ?: return null
    val kind = primaryAttachmentKind.toAttachmentKindOrNull() ?: return null
    val uri = primaryAttachmentUri ?: return null
    return NoteAttachmentPreview(
        id = attId,
        kind = kind,
        uri = uri,
        label = primaryAttachmentLabel
    )
}

private fun SearchWithPrimaryAttachment.toPrimaryAttachmentOrNull(): NoteAttachmentPreview? {
    val attId = primaryAttachmentId ?: return null
    val kind = primaryAttachmentKind.toAttachmentKindOrNull() ?: return null
    val uri = primaryAttachmentUri ?: return null
    return NoteAttachmentPreview(
        id = attId,
        kind = kind,
        uri = uri,
        label = primaryAttachmentLabel
    )
}

private fun Note_attachment.toDomainAttachment(): NoteAttachment {
    val parsedKind = when (kind.lowercase()) {
        "photo" -> NoteAttachmentKind.Photo
        "pdf" -> NoteAttachmentKind.Pdf
        else -> NoteAttachmentKind.Photo
    }

    return NoteAttachment(
        id = id,
        noteId = NoteId(noteId),
        kind = parsedKind,
        uri = uri,
        label = label,
        isPrimary = isPrimary == 1L,
        createdAtEpochMs = createdAt
    )
}

private fun String?.toAttachmentKindOrNull(): NoteAttachmentKind? = when (this?.lowercase()) {
    "photo" -> NoteAttachmentKind.Photo
    "pdf" -> NoteAttachmentKind.Pdf
    else -> null
}
