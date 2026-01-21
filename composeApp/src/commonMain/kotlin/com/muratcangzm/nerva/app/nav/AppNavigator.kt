package com.muratcangzm.nerva.app.nav

class AppNavigator<T : Any>(
    private val backStack: MutableList<T>
) {
    val stack: List<T> get() = backStack
    val canPop: Boolean get() = backStack.size > 1

    fun push(route: T) {
        backStack.add(route)
    }

    fun pop(): Boolean {
        if (!canPop) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun replaceTop(route: T) {
        if (backStack.isEmpty()) {
            backStack.add(route)
        } else {
            backStack[backStack.lastIndex] = route
        }
    }
}
