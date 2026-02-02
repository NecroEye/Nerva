package com.muratcangzm.nerva.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muratcangzm.common.extension.toPinnedBoolean
import com.muratcangzm.nerva.feature.library.components.LibraryNoteCard
import com.muratcangzm.nerva.feature.library.components.background.BrandGradientBackground
import com.muratcangzm.nerva.feature.library.components.list.MasonryGrid
import com.muratcangzm.nerva.feature.library.components.search.RecentSearchesRow
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenNote: (String) -> Unit = {},
    onCreateNote: () -> Unit = {},
) {
    val onOpenNoteState = rememberUpdatedState(onOpenNote)

    val viewModel: LibraryViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is LibraryEffect.NavigateToEdit -> onOpenNoteState.value(effect.id)
                is LibraryEffect.ShowMessage -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    BrandGradientBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = { TopAppBar(title = { Text(text = "Library") }) },
            floatingActionButton = {
                FloatingActionButton(onClick = onCreateNote) {
                    Text(text = "+")
                }
            }
        ) { padding ->
            Content(
                padding = padding,
                state = state,
                onQueryChange = { viewModel.dispatch(LibraryAction.QueryChanged(it)) },
                onClearRecent = { viewModel.dispatch(LibraryAction.ClearRecentSearches) },
                onOpen = { id -> viewModel.dispatch(LibraryAction.OpenNote(id)) },
                onTogglePin = { id, pinned -> viewModel.dispatch(LibraryAction.TogglePin(id, pinned)) },
                onDelete = { id -> viewModel.dispatch(LibraryAction.DeleteNote(id)) },
            )
        }
    }
}

@Composable
private fun Content(
    padding: PaddingValues,
    state: LibraryState,
    onQueryChange: (String) -> Unit,
    onClearRecent: () -> Unit,
    onOpen: (String) -> Unit,
    onTogglePin: (id: String, pinned: Long) -> Unit,
    onDelete: (String) -> Unit,
) {
    val pinned = state.notes.filter { it.pinned.toPinnedBoolean() }
    val normal = state.notes.filterNot { it.pinned.toPinnedBoolean() }

    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = "Search") }
            )
        }

        if (state.query.isBlank()) {
            item {
                RecentSearchesRow(
                    recent = state.recentSearches,
                    onClick = { q -> onQueryChange(q) },
                    onClear = onClearRecent
                )
            }
        }

        if (state.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        if (pinned.isNotEmpty()) {
            item {
                Text(
                    text = "📌 Pinned",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                MasonryGrid(
                    columns = 2,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pinned.forEach { note ->
                        key(note.id) {
                            val visible = note.id !in state.pendingDeletionIds
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + scaleIn(initialScale = 0.98f),
                                exit = fadeOut() + scaleOut(targetScale = 0.98f)
                            ) {
                                LibraryNoteCard(
                                    item = note,
                                    query = state.query,
                                    onOpen = { onOpen(note.id) },
                                    onTogglePin = {
                                        val pinnedNow = note.pinned.toPinnedBoolean()
                                        onTogglePin(note.id, if (pinnedNow) 0L else 1L)
                                    },
                                    onDelete = { onDelete(note.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "📝 Notes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            MasonryGrid(
                columns = 2,
                horizontalSpacing = 12.dp,
                verticalSpacing = 12.dp,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                normal.forEach { note ->
                    key(note.id) {
                        val visible = note.id !in state.pendingDeletionIds
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + scaleIn(initialScale = 0.98f),
                            exit = fadeOut() + scaleOut(targetScale = 0.98f)
                        ) {
                            LibraryNoteCard(
                                item = note,
                                query = state.query,
                                onOpen = { onOpen(note.id) },
                                onTogglePin = {
                                    val pinnedNow = note.pinned.toPinnedBoolean()
                                    onTogglePin(note.id, if (pinnedNow) 0L else 1L)
                                },
                                onDelete = { onDelete(note.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(96.dp))
        }
    }
}
