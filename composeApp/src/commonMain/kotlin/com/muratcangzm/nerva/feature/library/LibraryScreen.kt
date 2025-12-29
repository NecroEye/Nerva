    package com.muratcangzm.nerva.feature.library

    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.grid.GridCells
    import androidx.compose.foundation.lazy.grid.GridItemSpan
    import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
    import androidx.compose.foundation.lazy.grid.items
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
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.rememberUpdatedState
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import com.muratcangzm.common.extension.toPinnedBoolean
    import com.muratcangzm.nerva.feature.library.components.LibraryNoteCard
    import com.muratcangzm.nerva.feature.library.components.background.BrandGradientBackground
    import com.muratcangzm.nerva.feature.library.components.reorder.LibraryReorderableGrid
    import kotlinx.coroutines.flow.collectLatest
    import org.koin.compose.koinInject

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LibraryScreen(
        onOpenNote: (String) -> Unit = {}
    ) {
        val onOpenNoteState = rememberUpdatedState(onOpenNote)

        val viewModel: LibraryViewModel = koinInject()
        val state by viewModel.state.collectAsState()

        val snackBarHostState = remember { SnackbarHostState() }

        LaunchedEffect(viewModel) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    is LibraryEffect.NavigateToNote -> onOpenNoteState.value(effect.id)
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
                    FloatingActionButton(onClick = { viewModel.dispatch(LibraryAction.CreateNote) }) {
                        Text(text = "+")
                    }
                }
            ) { padding ->
                Content(
                    padding = padding,
                    state = state,
                    onQueryChange = { viewModel.dispatch(LibraryAction.QueryChanged(it)) },
                    onOpen = { id -> viewModel.dispatch(LibraryAction.OpenNote(id)) },
                    onTogglePin = { id, pinned -> viewModel.dispatch(LibraryAction.TogglePin(id, pinned)) },
                    onDelete = { id -> viewModel.dispatch(LibraryAction.DeleteNote(id)) },
                    onReorder = { section, from, to ->
                        viewModel.dispatch(LibraryAction.Reorder(section = section, fromIndex = from, toIndex = to))
                    }
                )
            }
        }
    }

    @Composable
    private fun Content(
        padding: PaddingValues,
        state: LibraryState,
        onQueryChange: (String) -> Unit,
        onOpen: (String) -> Unit,
        onTogglePin: (id: String, pinned: Long) -> Unit,
        onDelete: (String) -> Unit,
        onReorder: (section: LibraryReorderSection, fromIndex: Int, toIndex: Int) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = "Search") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            LibraryReorderableGrid(
                notes = state.notes,
                pendingDeletionIds = state.pendingDeletionIds,
                onOpen = onOpen,
                onTogglePin = onTogglePin,
                onDelete = onDelete,
                onReorder = onReorder,
                modifier = Modifier.weight(1f)
            )
        }
    }
