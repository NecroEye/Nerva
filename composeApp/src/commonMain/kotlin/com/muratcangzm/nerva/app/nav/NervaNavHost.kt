package com.muratcangzm.nerva.app.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.muratcangzm.nerva.feature.library.LibraryScreen
import com.muratcangzm.nerva.feature.note.NoteScreen

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

    val duration = 260
    val fadeDuration = 120

    val forwardTransitionSpec: AnimatedContentTransitionScope<Scene<AppRoute>>.() -> ContentTransform =
        {
            val dir = AnimatedContentTransitionScope.SlideDirection.Left
            (slideIntoContainer(towards = dir, animationSpec = tween(durationMillis = duration)) +
                    fadeIn(animationSpec = tween(durationMillis = fadeDuration)))
                .togetherWith(
                    slideOutOfContainer(
                        towards = dir,
                        animationSpec = tween(durationMillis = duration)
                    ) +
                            fadeOut(animationSpec = tween(durationMillis = fadeDuration))
                )
                .using(SizeTransform(clip = false))
        }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<AppRoute>>.() -> ContentTransform =
        {
            val dir = AnimatedContentTransitionScope.SlideDirection.Right
            (slideIntoContainer(towards = dir, animationSpec = tween(durationMillis = duration)) +
                    fadeIn(animationSpec = tween(durationMillis = fadeDuration)))
                .togetherWith(
                    slideOutOfContainer(
                        towards = dir,
                        animationSpec = tween(durationMillis = duration)
                    ) +
                            fadeOut(animationSpec = tween(durationMillis = fadeDuration))
                )
                .using(SizeTransform(clip = false))
        }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = forwardTransitionSpec,
        popTransitionSpec = popTransitionSpec,
        entryProvider = entryProvider {
            entry<AppRoute.Library> {
                LibraryScreen(
                    onOpenNote = { id ->
                        navigator.push(AppRoute.NoteDetails(id))
                    }
                )
            }

            entry<AppRoute.NoteDetails> { key ->
                NoteScreen(
                    noteId = key.noteId,
                    onBack = { navigator.pop() }
                )
            }
        }
    )
}