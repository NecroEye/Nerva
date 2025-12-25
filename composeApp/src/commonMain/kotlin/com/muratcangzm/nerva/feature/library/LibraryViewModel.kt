package com.muratcangzm.nerva.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteId
import com.muratcangzm.data.repo.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val noteRepository: NoteRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private val _effects = Channel<LibraryEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<LibraryEffect> = _effects.receiveAsFlow()

    init {
        state
            .map { it.query }
            .distinctUntilChanged()
            .debounce(250)
            .flatMapLatest { q ->
                noteRepository.observeNotes(query = q.takeIf { it.isNotBlank() })
            }
            .onEach { notes ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        notes = notes.map(Note::toUi)
                    )
                }
            }
            .catch { t ->
                _state.update { it.copy(isLoading = false, errorMessage = t.message ?: "Unknown error") }
                _effects.trySend(LibraryEffect.ShowMessage("Notes load failed"))
            }
            .launchIn(viewModelScope)
    }

    fun dispatch(action: LibraryAction) {
        when (action) {
            is LibraryAction.QueryChanged -> {
                _state.update { it.copy(query = action.value, isLoading = true) }
            }

            LibraryAction.CreateNote -> {
                createNote()
            }

            is LibraryAction.OpenNote -> {
                _effects.trySend(LibraryEffect.NavigateToNote(action.id))
            }

            is LibraryAction.TogglePin -> {
                togglePin(id = action.id, pinned = action.pinned)
            }

            is LibraryAction.DeleteNote -> {
                deleteNote(id = action.id)
            }
        }
    }

    private fun createNote() {
        viewModelScope.launch(dispatchers.io) {
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val id = NoteId(generateId())

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
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            noteRepository.setPinned(NoteId(id), pinned, now)
        }
    }

    private fun deleteNote(id: String) {
        viewModelScope.launch(dispatchers.io) {
            noteRepository.deleteById(NoteId(id))
            _effects.trySend(LibraryEffect.ShowMessage("Deleted"))
        }
    }

    // Şimdilik: basit id üretimi (istersen bir sonraki adımda expect/actual UUID’ye geçiririz)
    private fun generateId(): String {
        // Kotlin/JVM’de UUID daha iyi; KMP için bir sonraki adımda expect/actual yapacağız.
        return kotlin.time.Clock.System.now().toEpochMilliseconds().toString() + "-" + kotlin.random.Random.nextInt()
    }
}

private fun Note.toUi(): LibraryNoteItem = LibraryNoteItem(
    id = id.value,
    title = title.ifBlank { "Untitled" },
    preview = content.take(120),
    pinned = pinned,
    updatedAtEpochMs = updatedAtEpochMs
)