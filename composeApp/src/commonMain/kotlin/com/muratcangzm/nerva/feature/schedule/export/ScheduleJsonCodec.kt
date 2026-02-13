package com.muratcangzm.nerva.feature.schedule.export

import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ScheduleExportPayload(
    val version: Int = 1,
    val items: List<ScheduleItem>,
)

class ScheduleJsonCodec(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
    }
) {
    fun encode(items: List<ScheduleItem>): String =
        json.encodeToString(ScheduleExportPayload.serializer(), ScheduleExportPayload(items = items))

    fun decode(payload: String): List<ScheduleItem> =
        json.decodeFromString(ScheduleExportPayload.serializer(), payload).items
}
