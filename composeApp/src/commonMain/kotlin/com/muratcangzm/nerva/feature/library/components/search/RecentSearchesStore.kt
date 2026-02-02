package com.muratcangzm.nerva.feature.library.components.search

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

interface RecentSearchesStore {
    val recent: Flow<List<String>>
    suspend fun record(query: String)
    suspend fun clear()
}

class SettingsRecentSearchesStore(
    private val settings: Settings,
) : RecentSearchesStore {

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    private val key = "library_recent_searches_v1"
    private val maxSize = 12

    private val _recent = MutableStateFlow(readFromDisk())
    override val recent: Flow<List<String>> = _recent

    override suspend fun record(query: String) {
        val token = query
            .trim()
            .split(Regex("\\s+"))
            .lastOrNull()
            ?.trim()
            .orEmpty()

        if (token.isBlank()) return

        mutex.withLock {
            val current = _recent.value
            val head = current.firstOrNull()

            if (head != null && head.startsWith(token, ignoreCase = true) && head.length > token.length) {
                return
            }

            val filtered = current
                .filterNot { it.equals(token, ignoreCase = true) }
                .filterNot { token.startsWith(it, ignoreCase = true) }

            val updated = (listOf(token) + filtered).take(maxSize)
            _recent.value = updated
            writeToDisk(updated)
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            _recent.value = emptyList()
            settings.remove(key)
        }
    }

    private fun readFromDisk(): List<String> {
        val raw = settings.getStringOrNull(key) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), raw)
        }.getOrNull().orEmpty()
    }

    private fun writeToDisk(list: List<String>) {
        val raw = json.encodeToString(ListSerializer(String.serializer()), list)
        settings.putString(key, raw)
    }
}
