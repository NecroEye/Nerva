package com.muratcangzm.nerva.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muratcangzm.common.extension.toPinnedBoolean
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenNote: (String) -> Unit = {}
) {
    val onOpenNoteState = rememberUpdatedState(onOpenNote)

    val libraryViewModel: LibraryViewModel = koinInject()
    val state by libraryViewModel.state.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(libraryViewModel) {
        libraryViewModel.effects.collectLatest { effect ->
            when (effect) {
                is LibraryEffect.NavigateToNote -> onOpenNoteState.value(effect.id)
                is LibraryEffect.ShowMessage -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Library") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { libraryViewModel.dispatch(LibraryAction.CreateNote) }
            ) {
                Text(text = "+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { query ->
                    libraryViewModel.dispatch(LibraryAction.QueryChanged(query))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = "Search") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.notes,
                    key = { note -> note.id }
                ) { note ->
                    NoteRow(
                        item = note,
                        onOpen = {
                            libraryViewModel.dispatch(LibraryAction.OpenNote(note.id))
                        },
                        onTogglePin = {
                            val isPinned = note.pinned.toPinnedBoolean()
                            libraryViewModel.dispatch(
                                LibraryAction.TogglePin(
                                    id = note.id,
                                    pinned = if (isPinned) 0L else 1L
                                )
                            )
                        },
                        onDelete = {
                            libraryViewModel.dispatch(LibraryAction.DeleteNote(note.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    item: LibraryNoteItem,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val isPinned = item.pinned.toPinnedBoolean()

    ElevatedCard(
        onClick = onOpen
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    TextButton(onClick = onTogglePin) {
                        Text(text = if (isPinned) "Unpin" else "Pin")
                    }

                    TextButton(onClick = onDelete) {
                        Text(text = "Delete")
                    }
                }
            }

            if (item.preview.isNotBlank()) {

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
            }
        }
    }
}
