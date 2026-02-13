package com.muratcangzm.nerva.feature.schedule

import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem
import kotlinx.datetime.DayOfWeek

enum class ScheduleViewMode { DAY, WEEK }

data class ScheduleState(
    val selectedDay: DayOfWeek = DayOfWeek.MONDAY,
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAY,
    val items: List<ScheduleItem> = emptyList(),
)

sealed interface ScheduleEvent {
    data class SelectDay(val day: DayOfWeek) : ScheduleEvent
    data class SetViewMode(val mode: ScheduleViewMode) : ScheduleEvent

    data class MoveByMinutes(val id: String, val deltaMinutes: Int) : ScheduleEvent
    data class ResizeTopByMinutes(val id: String, val deltaMinutes: Int) : ScheduleEvent
    data class ResizeBottomByMinutes(val id: String, val deltaMinutes: Int) : ScheduleEvent

    data class Upsert(val item: ScheduleItem) : ScheduleEvent
    data class Delete(val id: String) : ScheduleEvent
}
