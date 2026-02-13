package com.muratcangzm.nerva.feature.schedule

import com.muratcangzm.nerva.feature.schedule.model.ScheduleDefaults
import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem
import kotlin.math.max
import kotlin.math.min

object ScheduleReducer {

    fun reduce(state: ScheduleState, event: ScheduleEvent): ScheduleState {
        return when (event) {
            is ScheduleEvent.SelectDay -> state.copy(selectedDay = event.day)
            is ScheduleEvent.SetViewMode -> state.copy(viewMode = event.mode) // ✅ EKLE
            is ScheduleEvent.Upsert -> state.copy(items = upsert(state.items, event.item))
            is ScheduleEvent.Delete -> state.copy(items = state.items.filterNot { it.id == event.id })
            is ScheduleEvent.MoveByMinutes ->
                state.copy(items = state.items.map { if (it.id == event.id) move(it, event.deltaMinutes) else it })
            is ScheduleEvent.ResizeTopByMinutes ->
                state.copy(items = state.items.map { if (it.id == event.id) resizeTop(it, event.deltaMinutes) else it })
            is ScheduleEvent.ResizeBottomByMinutes ->
                state.copy(items = state.items.map { if (it.id == event.id) resizeBottom(it, event.deltaMinutes) else it })
        }
    }

    private fun upsert(list: List<ScheduleItem>, item: ScheduleItem): List<ScheduleItem> {
        val idx = list.indexOfFirst { it.id == item.id }
        return if (idx == -1) list + item else list.toMutableList().apply { set(idx, item) }
    }

    private fun snapDelta(deltaMinutes: Int, step: Int = ScheduleDefaults.SnapMinutes): Int {
        if (deltaMinutes == 0) return 0
        val steps = deltaMinutes / step // toward zero
        return steps * step
    }

    private fun move(item: ScheduleItem, rawDelta: Int): ScheduleItem {
        val delta = snapDelta(rawDelta)
        if (delta == 0) return item

        val duration = item.endMinute - item.startMinute
        val newStart = item.startMinute + delta
        val newEnd = item.endMinute + delta

        val clampedStart = max(ScheduleDefaults.DayStartMinute, min(newStart, ScheduleDefaults.DayEndMinute - duration))
        val clampedEnd = clampedStart + duration
        return item.copy(startMinute = clampedStart, endMinute = clampedEnd)
    }

    private fun resizeTop(item: ScheduleItem, rawDelta: Int): ScheduleItem {
        val delta = snapDelta(rawDelta)
        if (delta == 0) return item

        val proposedStart = item.startMinute + delta
        val maxStart = item.endMinute - ScheduleDefaults.MinDurationMinutes
        val clampedStart = proposedStart.coerceIn(ScheduleDefaults.DayStartMinute, maxStart)
        return item.copy(startMinute = clampedStart)
    }

    private fun resizeBottom(item: ScheduleItem, rawDelta: Int): ScheduleItem {
        val delta = snapDelta(rawDelta)
        if (delta == 0) return item

        val proposedEnd = item.endMinute + delta
        val minEnd = item.startMinute + ScheduleDefaults.MinDurationMinutes
        val clampedEnd = proposedEnd.coerceIn(minEnd, ScheduleDefaults.DayEndMinute)
        return item.copy(endMinute = clampedEnd)
    }
}
