package com.muratcangzm.nerva.feature.note.noteEditor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.muratcangzm.nerva.feature.library.components.background.BrandGradientBackground
import com.muratcangzm.nerva.feature.library.components.chip.TagChip
import com.muratcangzm.nerva.feature.note.shared.NoteAttachmentUi
import com.muratcangzm.nerva.feature.note.shared.rememberAttachmentPicker
import com.muratcangzm.nerva.feature.note.shared.rememberKoinNavViewModel
import com.muratcangzm.nerva.feature.note.shared.rememberPdfThumbnailPainter
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
                    navigationIcon = {
                        TextButton(onClick = onClose) {
                            Text(
                                text = "✕",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    text = { Text("🖼️ Image") },
                    onClick = { menu = false; onAddImage() }
                )
                DropdownMenuItem(
                    text = { Text("📄 PDF") },
                    onClick = { menu = false; onAddPdf() }
                )
            }
        }

        KindRow(attachments = attachments)

        if (attachments.isEmpty()) {
            Text(
                text = "No attachments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(
                    items = attachments,
                    key = { it.id }
                ) { a ->
                    AttachmentChipItem(a = a, onRemove = { onRemove(a.id) })
                }
            }
        }
    }
}

@Composable
private fun KindRow(attachments: List<NoteAttachmentUi>) {
    val kinds = remember(attachments) { attachments.map { it.kind }.distinct() }
    if (kinds.isEmpty()) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Kind",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        kinds.forEach { k ->
            TagChip(
                text = k.label,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
            )
        }
    }
}

@Composable
private fun AttachmentChipItem(
    a: NoteAttachmentUi,
    onRemove: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val pdfPainter =
        if (a.kind == com.muratcangzm.nerva.feature.note.shared.NoteAttachmentKind.Pdf) {
            rememberPdfThumbnailPainter(a.uri)
        } else null

    TextButton(
        onClick = onRemove,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                when (a.kind) {
                    com.muratcangzm.nerva.feature.note.shared.NoteAttachmentKind.Image -> {
                        Image(
                            painter = rememberAsyncImagePainter(a.uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    com.muratcangzm.nerva.feature.note.shared.NoteAttachmentKind.Pdf -> {
                        if (pdfPainter != null) {
                            Image(
                                painter = pdfPainter,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("📄")
                        }
                    }
                }
            }

            Column(modifier = Modifier.width(150.dp)) {
                Text(
                    text = a.displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                TagChip(
                    text = a.kind.label,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                )
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
