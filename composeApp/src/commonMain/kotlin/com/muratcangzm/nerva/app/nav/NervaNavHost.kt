package com.muratcangzm.nerva.app.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.muratcangzm.nerva.feature.library.LibraryScreen
import com.muratcangzm.nerva.feature.note.noteEditor.NoteEditorScreen
import com.muratcangzm.nerva.feature.schedule.ui.ScheduleScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NervaNavHost(
    start: AppRoute = AppRoute.Library,
) {
    val startTab = when (start) {
        AppRoute.Schedule -> TopTab.Schedule
        else -> TopTab.Library
    }

    val selectedTabName = rememberSaveable { androidx.compose.runtime.mutableStateOf(startTab.name) }
    val selectedTab = TopTab.valueOf(selectedTabName.value)

    val libraryStack = remember(start) {
        mutableStateListOf<AppRoute>().also { stack ->
            stack.add(AppRoute.Library)
            when (start) {
                is AppRoute.NoteEdit -> stack.add(start)
                AppRoute.NoteCreate -> stack.add(AppRoute.NoteCreate)
                else -> Unit
            }
        }
    }

    val scheduleStack = remember {
        mutableStateListOf<AppRoute>(AppRoute.Schedule)
    }

    val selectedTabState = rememberSaveable { AppNavigator.createSelectedTabState(selectedTab) }
    androidx.compose.runtime.LaunchedEffect(selectedTabName.value) {
        selectedTabState.value = TopTab.valueOf(selectedTabName.value)
    }

    val navigator = remember(libraryStack, scheduleStack) {
        AppNavigator(
            selectedTabState = selectedTabState,
            libraryStack = libraryStack,
            scheduleStack = scheduleStack
        )
    }

    PlatformBackHandler(
        enabled = navigator.canPop,
        onBack = { navigator.pop() }
    )

    val showBottomBar = when (navigator.current) {
        AppRoute.Library, AppRoute.Schedule -> true
        else -> false
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NervaBottomBar(
                    selected = navigator.selectedTab,
                    onSelected = { tab ->
                        selectedTabName.value = tab.name
                        navigator.selectTab(tab)
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavDisplay(
                backStack = navigator.backStack,
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

                    entry<AppRoute.Schedule> {
                            ScheduleScreen()
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
    }
}


