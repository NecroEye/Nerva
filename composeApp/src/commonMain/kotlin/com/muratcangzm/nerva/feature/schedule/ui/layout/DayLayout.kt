package com.muratcangzm.nerva.feature.schedule.ui.layout

import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem

data class PlacedEvent(
    val item: ScheduleItem,
    val laneIndex: Int,
    val laneCount: Int,
)

fun computeDayLayout(items: List<ScheduleItem>): List<PlacedEvent> {
    if (items.isEmpty()) return emptyList()

    val sorted = items.sortedWith(compareBy<ScheduleItem> { it.startMinute }.thenBy { it.endMinute })

    val clusters = mutableListOf<List<ScheduleItem>>()
    var current = mutableListOf<ScheduleItem>()
    var currentEnd = Int.MIN_VALUE

    for (e in sorted) {
        if (current.isEmpty()) {
            current.add(e)
            currentEnd = e.endMinute
        } else if (e.startMinute < currentEnd) {
            current.add(e)
            if (e.endMinute > currentEnd) currentEnd = e.endMinute
        } else {
            clusters.add(current)
            current = mutableListOf(e)
            currentEnd = e.endMinute
        }
    }
    if (current.isNotEmpty()) clusters.add(current)

    val result = mutableListOf<PlacedEvent>()
    for (cluster in clusters) {
        val laneEnds = mutableListOf<Int>()
        val assignments = mutableMapOf<String, Int>()

        val clusterSorted = cluster.sortedWith(compareBy<ScheduleItem> { it.startMinute }.thenBy { it.endMinute })
        for (e in clusterSorted) {
            var lane = laneEnds.indexOfFirst { end -> end <= e.startMinute }
            if (lane == -1) {
                laneEnds.add(e.endMinute)
                lane = laneEnds.lastIndex
            } else {
                laneEnds[lane] = e.endMinute
            }
            assignments[e.id] = lane
        }

        val laneCount = laneEnds.size.coerceAtLeast(1)
        clusterSorted.forEach { e ->
            result.add(
                PlacedEvent(
                    item = e,
                    laneIndex = assignments.getValue(e.id),
                    laneCount = laneCount
                )
            )
        }
    }

    return result
}
