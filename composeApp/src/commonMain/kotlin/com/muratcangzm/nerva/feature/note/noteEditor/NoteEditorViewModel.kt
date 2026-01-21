package com.muratcangzm.nerva.feature.note.noteEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteAttachmentKind
import com.muratcangzm.data.model.NoteId
import com.muratcangzm.data.repo.NoteRepository
import com.muratcangzm.nerva.feature.note.shared.NoteAttachmentKind as UiKind
import com.muratcangzm.nerva.feature.note.shared.NoteAttachmentUi
import com.muratcangzm.nerva.feature.note.shared.PickedAttachment
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

class NoteEditorViewModel(
    private val noteRepository: NoteRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorState())
    val state: StateFlow<NoteEditorState> = _state

    private val _effects = Channel<NoteEditorEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var boundMode: NoteEditorScreen.Mode? = null
    private var bindJob: Job? = null

    fun bind(mode: NoteEditorScreen.Mode) {
        if (boundMode == mode) return
        boundMode = mode
        bindJob?.cancel()

        when (mode) {
            NoteEditorScreen.Mode.Create -> {
                _state.value = NoteEditorState(
                    noteId = null,
                    title = "",
                    content = "",
                    attachments = emptyList(),
                    isLoading = false,
                    isSaving = false
                )
            }

            is NoteEditorScreen.Mode.Edit -> {
                _state.update { it.copy(isLoading = true, noteId = mode.noteId) }

                bindJob = viewModelScope.launch {
                    val id = NoteId(mode.noteId)

                    noteRepository.observeNote(id)
                        .combine(noteRepository.observeAttachments(id)) { note, atts ->
                            note to atts
                        }
                        .collect { (note, atts) ->
                            if (note == null) {
                                _effects.trySend(NoteEditorEffect.Message("Note not found"))
                                return@collect
                            }

                            _state.update {
                                it.copy(
                                    noteId = note.id.value,
                                    title = note.title,
                                    content = note.content,
                                    attachments = atts.map { a ->
                                        NoteAttachmentUi(
                                            id = a.id,
                                            kind = when (a.kind) {
                                                NoteAttachmentKind.Photo -> UiKind.Image
                                                NoteAttachmentKind.Pdf -> UiKind.Pdf
                                            },
                                            displayName = a.label ?: a.uri,
                                            uri = a.uri
                                        )
                                    },
                                    isLoading = false
                                )
                            }
                        }
                }
            }
        }
    }

    fun onPickedAttachment(picked: PickedAttachment) {
        val ui = NoteAttachmentUi(
            id = picked.id,
            kind = picked.kind,
            displayName = picked.displayName,
            uri = picked.uri
        )
        _state.update { it.copy(attachments = it.attachments + ui) }
    }

    fun dispatch(action: NoteEditorAction) {
        when (action) {
            is NoteEditorAction.TitleChanged -> _state.update { it.copy(title = action.value) }
            is NoteEditorAction.ContentChanged -> _state.update { it.copy(content = action.value) }
            NoteEditorAction.AddImageClicked -> _effects.trySend(NoteEditorEffect.PickImage)
            NoteEditorAction.AddPdfClicked -> _effects.trySend(NoteEditorEffect.PickPdf)

            is NoteEditorAction.RemoveAttachment -> {
                _state.update { it.copy(attachments = it.attachments.filterNot { a -> a.id == action.id }) }
            }

            NoteEditorAction.SaveClicked -> save()
        }
    }

    private fun save() {
        val mode = boundMode ?: return
        val title = _state.value.title.trim()
        val content = _state.value.content.trim()

        if (title.isBlank() && content.isBlank()) {
            _effects.trySend(NoteEditorEffect.Message("Title or content required"))
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _state.update { it.copy(isSaving = true) }

            val now = Clock.System.now().toEpochMilliseconds()
            val noteId = when (mode) {
                NoteEditorScreen.Mode.Create -> NoteId(newId(now))
                is NoteEditorScreen.Mode.Edit -> NoteId(mode.noteId)
            }

            val existing = noteRepository.getById(noteId)
            val createdAt = existing?.createdAtEpochMs ?: now
            val pinned = existing?.pinned ?: 0L

            val note = Note(
                id = noteId,
                title = title,
                content = content,
                createdAtEpochMs = createdAt,
                updatedAtEpochMs = now,
                pinned = pinned
            )

            noteRepository.upsert(note)

            val attachments = _state.value.attachments.map { ui ->
                com.muratcangzm.data.model.NoteAttachmentUpsert(
                    id = ui.id,
                    kind = when (ui.kind) {
                        UiKind.Image -> NoteAttachmentKind.Photo
                        UiKind.Pdf -> NoteAttachmentKind.Pdf
                    },
                    uri = ui.uri,
                    label = ui.displayName
                )
            }

            noteRepository.replaceAttachments(noteId, attachments, now)

            _state.update { it.copy(isSaving = false, noteId = noteId.value) }
            _effects.send(NoteEditorEffect.Saved(noteId.value))
        }
    }

    private fun newId(now: Long): String {
        val r = Random.nextLong().toString(16)
        return "$now-$r"
    }
}
