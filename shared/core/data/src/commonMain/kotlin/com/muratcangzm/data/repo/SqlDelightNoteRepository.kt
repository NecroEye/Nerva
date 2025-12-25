package com.muratcangzm.data.repo


import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteId
import com.muratcangzm.database.NervaDatabase
import com.muratcangzm.database.Note as DbNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightNoteRepository(
    private val db: NervaDatabase,
    private val dispatchers: AppDispatchers
) : NoteRepository {

    override fun observeNotes(query: String?): Flow<List<Note>> {
        val q = query?.trim().orEmpty()
        val sqlQuery = if (q.isEmpty()) {
            db.noteQueries.selectAll()
        } else {
            db.noteQueries.search(q)
        }

        return sqlQuery
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.map(DbNote::toDomain) }
    }

    override suspend fun getById(id: NoteId): Note? {
        return withContext(dispatchers.io) {
            db.noteQueries.selectById(id.value)
                .asFlow()
                .mapToOneOrNull(dispatchers.io)
                .map { it?.toDomain() }
        }.let { flow ->
            withContext(dispatchers.io) {
                db.noteQueries.selectById(id.value).executeAsOneOrNull()?.toDomain()
            }
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

    override suspend fun updateContent(
        id: NoteId,
        title: String,
        content: String,
        updatedAtEpochMs: Long
    ) {
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

private fun DbNote.toDomain(): Note = Note(
    id = NoteId(id),
    title = title,
    content = content,
    createdAtEpochMs = createdAt,
    updatedAtEpochMs = updatedAt,
    pinned = pinned
)