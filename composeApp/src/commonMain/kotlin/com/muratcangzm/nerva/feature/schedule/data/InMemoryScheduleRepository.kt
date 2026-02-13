package com.muratcangzm.nerva.feature.schedule.data

import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class InMemoryScheduleRepository(
    seed: List<ScheduleItem> = emptyList()
) : ScheduleRepository {

    private val state = MutableStateFlow(seed)

    override fun observeAll(): Flow<List<ScheduleItem>> = state

    override suspend fun upsert(item: ScheduleItem) {
        state.update { list ->
            val idx = list.indexOfFirst { it.id == item.id }
            if (idx == -1) list + item else list.toMutableList().apply { set(idx, item) }
        }
    }

    override suspend fun delete(id: String) {
        state.update { it.filterNot { x -> x.id == id } }
    }

    override suspend fun replaceAll(items: List<ScheduleItem>) {
        state.value = items
    }
}
