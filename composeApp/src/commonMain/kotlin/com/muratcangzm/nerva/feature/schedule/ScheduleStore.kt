package com.muratcangzm.nerva.feature.schedule

import com.muratcangzm.nerva.feature.schedule.data.ScheduleRepository
import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleStore(
    private val repository: ScheduleRepository,
    externalScope: CoroutineScope? = null,
) {
    private val internalJob = SupervisorJob()
    private val scope: CoroutineScope =
        externalScope ?: CoroutineScope(internalJob + Dispatchers.Main)

    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    init {
        scope.launch {
            repository.observeAll().collect { items ->
                _state.update { it.copy(items = items) }
            }
        }
    }

    fun dispatch(event: ScheduleEvent) {
        when (event) {
            is ScheduleEvent.Upsert -> scope.launch { repository.upsert(event.item) }
            is ScheduleEvent.Delete -> scope.launch { repository.delete(event.id) }
            is ScheduleEvent.SelectDay -> _state.update { ScheduleReducer.reduce(it, event) }
            is ScheduleEvent.SetViewMode -> _state.update { ScheduleReducer.reduce(it, event) }
            is ScheduleEvent.MoveByMinutes -> applyAndPersist(event.id) { ScheduleReducer.reduce(it, event) }
            is ScheduleEvent.ResizeTopByMinutes -> applyAndPersist(event.id) { ScheduleReducer.reduce(it, event) }
            is ScheduleEvent.ResizeBottomByMinutes -> applyAndPersist(event.id) { ScheduleReducer.reduce(it, event) }
        }
    }

    private fun applyAndPersist(id: String, transform: (ScheduleState) -> ScheduleState) {
        val newState = transform(_state.value)
        _state.value = newState
        val updated: ScheduleItem? = newState.items.firstOrNull { it.id == id }
        if (updated != null) scope.launch { repository.upsert(updated) }
    }

    fun close() {
        if (scope.coroutineContext[internalJob.key] != null) {
            internalJob.cancel()
        }
    }
}
