package com.muratcangzm.nerva.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.common.extension.toPinnedBoolean
import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteAttachmentKind
import com.muratcangzm.data.model.NoteAttachmentPreview
import com.muratcangzm.data.model.NoteId
import com.muratcangzm.data.repo.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val noteRepository: NoteRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState(isLoading = true))
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private val _effects = Channel<LibraryEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<LibraryEffect> = _effects.receiveAsFlow()

    private val orderState = MutableStateFlow(LibraryOrderState())

    init {
        val queryFlow = state
            .map { it.query.trim() }
            .distinctUntilChanged()
            .debounce(250)

        val repoNotesFlow = queryFlow
            .flatMapLatest { q ->
                noteRepository.observeNotes(query = q.takeIf { it.isNotBlank() })
            }

        repoNotesFlow
            .combine(orderState) { notes, order -> order.apply(notes) }
            .onEach { ordered ->
                val pinned = ordered.filter { it.pinned.toPinnedBoolean() }
                val normal = ordered.filterNot { it.pinned.toPinnedBoolean() }

                val uiPinned = pinned.mapIndexed { index, note ->
                    note.toUi(orderIndex = index.toLong())
                }
                val uiNormal = normal.mapIndexed { index, note ->
                    note.toUi(orderIndex = index.toLong())
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        notes = uiPinned + uiNormal
                    )
                }
            }
            .catch { t ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Unknown error",
                        notes = emptyList()
                    )
                }
                _effects.trySend(LibraryEffect.ShowMessage("Notes load failed"))
            }
            .launchIn(viewModelScope)
    }

    fun dispatch(action: LibraryAction) {
        when (action) {
            is LibraryAction.QueryChanged -> _state.update { it.copy(query = action.value, isLoading = true) }
            LibraryAction.CreateNote -> createNote()
            is LibraryAction.OpenNote -> _effects.trySend(LibraryEffect.NavigateToNote(action.id))
            is LibraryAction.TogglePin -> togglePin(id = action.id, pinned = action.pinned)
            is LibraryAction.DeleteNote -> deleteNote(id = action.id)
            is LibraryAction.Reorder -> orderState.update { it.reorder(action.section, action.fromIndex, action.toIndex) }
        }
    }

    private fun createNote() {
        viewModelScope.launch(dispatchers.io) {
            val now = Clock.System.now().toEpochMilliseconds()
            val id = NoteId(newId(now))

            val note = Note(
                id = id,
                title = "New note",
                content = "",
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
                pinned = 0L
            )

            noteRepository.upsert(note)
            _effects.trySend(LibraryEffect.NavigateToNote(id.value))
        }
    }

    private fun togglePin(id: String, pinned: Long) {
        viewModelScope.launch(dispatchers.io) {
            val now = Clock.System.now().toEpochMilliseconds()
            noteRepository.setPinned(
                id = NoteId(id),
                pinned = pinned,
                updatedAtEpochMs = now
            )
        }
    }

    private fun deleteNote(id: String) {
        viewModelScope.launch(dispatchers.io) {
            _state.update { it.copy(pendingDeletionIds = it.pendingDeletionIds + id) }
            delay(180)
            noteRepository.deleteById(NoteId(id))
            _state.update { it.copy(pendingDeletionIds = it.pendingDeletionIds - id) }
            _effects.trySend(LibraryEffect.ShowMessage("Deleted"))
        }
    }

    private fun newId(now: Long): String {
        val r = Random.nextLong().toString(16)
        return "$now-$r"
    }
}

private data class LibraryOrderState(
    val pinnedOrder: List<String> = emptyList(),
    val normalOrder: List<String> = emptyList()
) {
    fun apply(notes: List<Note>): List<Note> {
        val pinned = notes.filter { it.pinned.toPinnedBoolean() }
        val normal = notes.filterNot { it.pinned.toPinnedBoolean() }

        val pinnedSorted = sortByOrder(
            notes = pinned,
            order = pinnedOrder,
            idOf = { it.id.value },
            fallbackComparator = compareByDescending<Note> { it.updatedAtEpochMs }
        )

        val normalSorted = sortByOrder(
            notes = normal,
            order = normalOrder,
            idOf = { it.id.value },
            fallbackComparator = compareByDescending<Note> { it.updatedAtEpochMs }
        )

        return pinnedSorted + normalSorted
    }

    fun reorder(section: LibraryReorderSection, fromIndex: Int, toIndex: Int): LibraryOrderState {
        return when (section) {
            LibraryReorderSection.Pinned -> copy(pinnedOrder = pinnedOrder.moved(fromIndex, toIndex))
            LibraryReorderSection.Normal -> copy(normalOrder = normalOrder.moved(fromIndex, toIndex))
        }
    }

    private fun <T> sortByOrder(
        notes: List<T>,
        order: List<String>,
        idOf: (T) -> String,
        fallbackComparator: Comparator<T>
    ): List<T> {
        if (notes.isEmpty()) return emptyList()

        val byId = notes.associateBy(idOf)
        val existing = order.filter { it in byId }

        val missing = notes
            .filter { idOf(it) !in existing }
            .sortedWith(fallbackComparator)
            .map(idOf)

        val finalOrder = existing + missing
        return finalOrder.mapNotNull(byId::get)
    }

    private fun List<String>.moved(from: Int, to: Int): List<String> {
        if (from !in indices || to !in indices || from == to) return this
        val mutable = toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        return mutable.toList()
    }
}

private fun Note.toUi(orderIndex: Long): LibraryNoteItem = LibraryNoteItem(
    id = id.value,
    title = title.ifBlank { "Untitled" },
    preview = content.lineSequence().joinToString(" ").trim().take(140),
    pinned = pinned,
    updatedAtEpochMs = updatedAtEpochMs,
    orderIndex = orderIndex,
    tags = emptyList(),
    attachmentsCount = attachmentsCount,
    attachmentPreviews = primaryAttachment?.let { listOf(it.toUi()) }.orEmpty()
)

private fun NoteAttachmentPreview.toUi(): LibraryAttachmentPreview = LibraryAttachmentPreview(
    id = id,
    kind = when (kind) {
        NoteAttachmentKind.Photo -> LibraryAttachmentKind.Photo
        NoteAttachmentKind.Pdf -> LibraryAttachmentKind.Pdf
    },
    uri = uri,
    label = label
)
