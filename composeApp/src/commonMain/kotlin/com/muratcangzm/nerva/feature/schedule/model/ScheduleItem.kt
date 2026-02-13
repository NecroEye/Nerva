package com.muratcangzm.nerva.feature.schedule.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
enum class ScheduleItemKind { COURSE, EVENT }

@Serializable
@Immutable
data class ScheduleItem(
    val id: String,
    val kind: ScheduleItemKind,
    val title: String,
    val dayOfWeek: DayOfWeek,
    val startMinute: Int,
    val endMinute: Int,
    val colorArgb: ULong,
    val location: String? = null,
    val teacher: String? = null,
    val notes: String? = null,
) {
    init {
        require(endMinute > startMinute) { "endMinute must be > startMinute" }
    }
}



object ScheduleDefaults {
    const val DayStartMinute: Int = 0
    const val DayEndMinute: Int = 24 * 60
    const val SnapMinutes: Int = 5
    const val MinDurationMinutes: Int = 15
}
