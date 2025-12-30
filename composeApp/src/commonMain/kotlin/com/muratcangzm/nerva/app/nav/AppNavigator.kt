package com.muratcangzm.nerva.app.nav

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

@Stable
class AppNavigator<T : Any>(
    private val backStack: SnapshotStateList<T>
) {
    var lastAction by mutableStateOf(NavAction.Push)
        private set

    val stack: List<T> get() = backStack
    val canPop: Boolean get() = backStack.size > 1

    fun push(route: T) {
        lastAction = NavAction.Push
        backStack.add(route)
    }

    fun pop(): Boolean {
        if (!canPop) return false
        lastAction = NavAction.Pop
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}
