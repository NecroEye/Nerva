package com.muratcangzm.data.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteAttachmentKind
import com.muratcangzm.data.model.NoteAttachmentPreview
import com.muratcangzm.data.model.NoteId
import com.muratcangzm.data.model.toDomainNoAttachment
import com.muratcangzm.database.NervaDatabase
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
                .map { rows -> rows.map(SelectAllWithPrimaryAttachment::toDomain) }
        } else {
            db.noteQueries
                .searchWithPrimaryAttachment(value_ = q)
                .asFlow()
                .mapToList(dispatchers.io)
                .map { rows -> rows.map(SearchWithPrimaryAttachment::toDomain) }
        }
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
        withContext(dispatchers.io) { db.noteQueries.deleteById(id.value) }
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
}

private fun SelectAllWithPrimaryAttachment.toDomain(): Note = Note(
    id = NoteId(id),
    title = title,
    content = content,
    createdAtEpochMs = createdAt,
    updatedAtEpochMs = updatedAt,
    pinned = pinned,
    attachmentsCount = attachmentsCount.toInt(),
    primaryAttachment = toPrimaryAttachmentOrNull()
)

private fun SearchWithPrimaryAttachment.toDomain(): Note = Note(
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

private fun String?.toAttachmentKindOrNull(): NoteAttachmentKind? = when (this?.lowercase()) {
    "photo" -> NoteAttachmentKind.Photo
    "pdf" -> NoteAttachmentKind.Pdf
    else -> null
}
