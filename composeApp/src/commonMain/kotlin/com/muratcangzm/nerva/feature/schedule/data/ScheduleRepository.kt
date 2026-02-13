package com.muratcangzm.nerva.feature.schedule.data

import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeAll(): Flow<List<ScheduleItem>>
    suspend fun upsert(item: ScheduleItem)
    suspend fun delete(id: String)
    suspend fun replaceAll(items: List<ScheduleItem>)
}