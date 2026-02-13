package com.muratcangzm.nerva.app.nav

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

@Stable
class AppNavigator(
    private val selectedTabState: androidx.compose.runtime.MutableState<TopTab>,
    private val libraryStack: SnapshotStateList<AppRoute>,
    private val scheduleStack: SnapshotStateList<AppRoute>,
) {
    val selectedTab: TopTab get() = selectedTabState.value

    val backStack: SnapshotStateList<AppRoute>
        get() = when (selectedTabState.value) {
            TopTab.Library -> libraryStack
            TopTab.Schedule -> scheduleStack
        }

    val current: AppRoute get() = backStack.last()

    val canPop: Boolean get() = backStack.size > 1

    fun push(route: AppRoute) {
        backStack.add(route)
    }

    fun pop(): Boolean {
        if (!canPop) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun replaceTop(route: AppRoute) {
        if (backStack.isEmpty()) {
            backStack.add(route)
            return
        }
        backStack[backStack.lastIndex] = route
    }

    fun popToRoot(tab: TopTab = selectedTab) {
        val stack = stackOf(tab)
        val root = rootRouteOf(tab)
        stack.clear()
        stack.add(root)
    }

    fun selectTab(tab: TopTab) {
        if (selectedTabState.value == tab) {
            popToRoot(tab)
        } else {
            selectedTabState.value = tab
        }
    }

    private fun stackOf(tab: TopTab): SnapshotStateList<AppRoute> {
        return when (tab) {
            TopTab.Library -> libraryStack
            TopTab.Schedule -> scheduleStack
        }
    }

    private fun rootRouteOf(tab: TopTab): AppRoute {
        return when (tab) {
            TopTab.Library -> AppRoute.Library
            TopTab.Schedule -> AppRoute.Schedule
        }
    }

    companion object {
        fun createSelectedTabState(start: TopTab): androidx.compose.runtime.MutableState<TopTab> {
            return mutableStateOf(start)
        }
    }
}
