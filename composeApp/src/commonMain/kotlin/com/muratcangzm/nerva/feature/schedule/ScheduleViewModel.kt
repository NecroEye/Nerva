package com.muratcangzm.nerva.feature.schedule

import com.muratcangzm.common.coroutines.AppDispatchers
import com.muratcangzm.nerva.feature.schedule.data.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

class ScheduleViewModel(
    scheduleRepository: ScheduleRepository,
    dispatchers: AppDispatchers,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)

    private val store = ScheduleStore(
        repository = scheduleRepository,
        externalScope = scope
    )

    val state: StateFlow<ScheduleState> = store.state

    fun onEvent(event: ScheduleEvent) {
        store.dispatch(event)
    }

    fun close() {
        scope.cancel()
        store.close()
    }
}
