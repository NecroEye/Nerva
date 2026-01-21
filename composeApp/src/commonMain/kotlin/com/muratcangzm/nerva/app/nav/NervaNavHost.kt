package com.muratcangzm.nerva.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.muratcangzm.nerva.feature.library.LibraryScreen
import com.muratcangzm.nerva.feature.note.noteEditor.NoteEditorScreen

@Composable
fun NervaNavHost(
    start: AppRoute = AppRoute.Library,
) {
    val backStack = remember { mutableStateListOf<AppRoute>(start) }
    val navigator = remember(backStack) { AppNavigator(backStack) }

    PlatformBackHandler(
        enabled = navigator.canPop,
        onBack = { navigator.pop() }
    )

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<AppRoute.Library> {
                LibraryScreen(
                    onOpenNote = { id -> navigator.push(AppRoute.NoteEdit(id)) },
                    onCreateNote = { navigator.push(AppRoute.NoteCreate) }
                )
            }

            entry<AppRoute.NoteCreate> {
                NoteEditorScreen(
                    mode = NoteEditorScreen.Mode.Create,
                    onClose = { navigator.pop() },
                    onSaved = { navigator.pop() }
                )
            }

            entry<AppRoute.NoteEdit> { key ->
                NoteEditorScreen(
                    mode = NoteEditorScreen.Mode.Edit(key.noteId),
                    onClose = { navigator.pop() },
                    onSaved = { navigator.pop() }
                )
            }
        }
    )
}
