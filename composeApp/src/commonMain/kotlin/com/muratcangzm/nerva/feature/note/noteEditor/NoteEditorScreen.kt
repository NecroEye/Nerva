package com.muratcangzm.nerva.feature.note.noteEditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muratcangzm.nerva.feature.library.components.background.BrandGradientBackground
import com.muratcangzm.nerva.feature.note.shared.NoteAttachmentUi
import com.muratcangzm.nerva.feature.note.shared.rememberAttachmentPicker
import com.muratcangzm.nerva.feature.note.shared.rememberKoinNavViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    mode: Mode,
    onClose: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val vm: NoteEditorViewModel = rememberKoinNavViewModel()
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(mode) { vm.bind(mode) }

    val picker = rememberAttachmentPicker(
        onPicked = { vm.onPickedAttachment(it) },
        onMessage = { msg -> snack.showSnackbar(msg) }
    )

    LaunchedEffect(vm) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                NoteEditorEffect.PickImage -> picker.pickImage()
                NoteEditorEffect.PickPdf -> picker.pickPdf()
                is NoteEditorEffect.Saved -> onSaved(eff.noteId)
                is NoteEditorEffect.Message -> snack.showSnackbar(eff.text)
            }
        }
    }

    BrandGradientBackground(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snack) },
            topBar = {
                TopAppBar(
                    title = { Text(if (mode is NoteEditorScreen.Mode.Create) "New note" else "Edit note") },
                    navigationIcon = { TextButton(onClick = onClose) { Text("Close") } },
                    actions = {
                        TextButton(
                            onClick = { vm.dispatch(NoteEditorAction.SaveClicked) },
                            enabled = !state.isSaving && !state.isLoading
                        ) { Text("Save") }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isLoading || state.isSaving) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                OutlinedTextField(
                    value = state.title,
                    onValueChange = { vm.dispatch(NoteEditorAction.TitleChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.content,
                    onValueChange = { vm.dispatch(NoteEditorAction.ContentChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    label = { Text("Content") }
                )

                AttachmentSection(
                    attachments = state.attachments,
                    onRemove = { vm.dispatch(NoteEditorAction.RemoveAttachment(it)) },
                    onAddImage = { vm.dispatch(NoteEditorAction.AddImageClicked) },
                    onAddPdf = { vm.dispatch(NoteEditorAction.AddPdfClicked) },
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AttachmentSection(
    attachments: List<NoteAttachmentUi>,
    onRemove: (String) -> Unit,
    onAddImage: () -> Unit,
    onAddPdf: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Attachments",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        var menu by remember { mutableStateOf(false) }
        TextButton(onClick = { menu = true }) { Text("Add") }

        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text("Image") },
                onClick = { menu = false; onAddImage() }
            )
            DropdownMenuItem(
                text = { Text("PDF") },
                onClick = { menu = false; onAddPdf() }
            )
        }
    }

    if (attachments.isEmpty()) {
        Text(
            text = "No attachments",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items = attachments, key = { it.id }) { a ->
            TextButton(onClick = { onRemove(a.id) }) {
                Text("${a.kind.label}: ${a.displayName}")
            }
        }
    }
}

object NoteEditorScreen {
    sealed interface Mode {
        data object Create : Mode
        data class Edit(val noteId: String) : Mode
    }
}

typealias Mode = NoteEditorScreen.Mode
