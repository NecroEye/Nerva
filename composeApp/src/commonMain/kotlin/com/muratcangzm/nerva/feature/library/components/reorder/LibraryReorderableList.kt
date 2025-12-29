package com.muratcangzm.nerva.feature.library.components.reorder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.muratcangzm.common.extension.toPinnedBoolean
import com.muratcangzm.nerva.feature.library.LibraryNoteItem
import com.muratcangzm.nerva.feature.library.LibraryReorderSection
import com.muratcangzm.nerva.feature.library.components.LibraryNoteCard

private sealed interface Entry {
    val key: String

    data class Header(val title: String, val section: LibraryReorderSection) : Entry {
        override val key: String = "header-$section"
    }

    data class Note(val item: LibraryNoteItem, val section: LibraryReorderSection) : Entry {
        override val key: String = "note-${item.id}"
    }
}

private fun IntOffset.toOffset(): Offset = Offset(x.toFloat(), y.toFloat())

@Stable
private class GridReorderState(
    private val gridState: LazyGridState,
    private val canMove: (from: Int, to: Int) -> Boolean,
    private val onMove: (from: Int, to: Int) -> Unit
) {
    var draggingIndex by mutableStateOf<Int?>(null)
    var draggingOffsetX by mutableFloatStateOf(0f)
    var draggingOffsetY by mutableFloatStateOf(0f)

    fun startDragging(index: Int) {
        draggingIndex = index
        draggingOffsetX = 0f
        draggingOffsetY = 0f
    }

    fun stopDragging() {
        draggingIndex = null
        draggingOffsetX = 0f
        draggingOffsetY = 0f
    }

    fun dragBy(delta: Offset) {
        val fromIndex = draggingIndex ?: return

        draggingOffsetX += delta.x
        draggingOffsetY += delta.y

        val fromInfo =
            gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == fromIndex } ?: return
        val fromTopLeft = fromInfo.offset.toOffset() + Offset(draggingOffsetX, draggingOffsetY)
        val fromCenter = fromTopLeft + Offset(fromInfo.size.width / 2f, fromInfo.size.height / 2f)

        val target = gridState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
            if (info.index == fromIndex) return@firstOrNull false
            val rect = Rect(
                offset = info.offset.toOffset(),
                size = Size(info.size.width.toFloat(), info.size.height.toFloat())
            )
            fromCenter.x in rect.left..rect.right && fromCenter.y in rect.top..rect.bottom
        } ?: return

        val toIndex = target.index
        if (!canMove(fromIndex, toIndex)) return

        onMove(fromIndex, toIndex)

        // offset'i stabilize et (zıplamayı azaltır)
        val newFromInfo = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == toIndex }
        if (newFromInfo != null) {
            val deltaOffset = fromInfo.offset - newFromInfo.offset
            draggingOffsetX += deltaOffset.x
            draggingOffsetY += deltaOffset.y
        }

        draggingIndex = toIndex
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryReorderableGrid(
    notes: List<LibraryNoteItem>,
    pendingDeletionIds: Set<String>,
    onOpen: (String) -> Unit,
    onTogglePin: (id: String, pinned: Long) -> Unit,
    onDelete: (String) -> Unit,
    onReorder: (section: LibraryReorderSection, fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pinned = notes.filter { it.pinned.toPinnedBoolean() }
    val normal = notes.filterNot { it.pinned.toPinnedBoolean() }

    val entries = buildList {
        if (pinned.isNotEmpty()) {
            add(Entry.Header("📌 Pinned", LibraryReorderSection.Pinned))
            pinned.forEach { add(Entry.Note(it, LibraryReorderSection.Pinned)) }
        }
        add(Entry.Header("📝 Notes", LibraryReorderSection.Normal))
        normal.forEach { add(Entry.Note(it, LibraryReorderSection.Normal)) }
    }

    val gridState = rememberLazyGridState()

    val sectionIndexPinned = remember(pinned) { pinned.map { it.id } }
    val sectionIndexNormal = remember(normal) { normal.map { it.id } }

    fun isNote(index: Int): Boolean = entries.getOrNull(index) is Entry.Note
    fun entrySection(index: Int): LibraryReorderSection? =
        (entries.getOrNull(index) as? Entry.Note)?.section

    val reorderState = remember(gridState, entries, pinned, normal) {
        GridReorderState(
            gridState = gridState,
            canMove = { from, to ->
                isNote(from) && isNote(to) && entrySection(from) == entrySection(to)
            },
            onMove = { from, to ->
                val fromEntry = entries[from] as Entry.Note
                val toEntry = entries[to] as Entry.Note

                val section = fromEntry.section
                val fromId = fromEntry.item.id
                val toId = toEntry.item.id

                val fromIndex = when (section) {
                    LibraryReorderSection.Pinned -> sectionIndexPinned.indexOf(fromId)
                    LibraryReorderSection.Normal -> sectionIndexNormal.indexOf(fromId)
                }
                val toIndex = when (section) {
                    LibraryReorderSection.Pinned -> sectionIndexPinned.indexOf(toId)
                    LibraryReorderSection.Normal -> sectionIndexNormal.indexOf(toId)
                }

                if (fromIndex >= 0 && toIndex >= 0) onReorder(section, fromIndex, toIndex)
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp, start = 12.dp, end = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = entries,
            key = { _, e -> e.key },
            span = { _, e -> if (e is Entry.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1) }
        ) { index, entry ->
            when (entry) {
                is Entry.Header -> {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                is Entry.Note -> {
                    val isDragging = reorderState.draggingIndex == index
                    val dragOffset = if (isDragging) Offset(
                        reorderState.draggingOffsetX,
                        reorderState.draggingOffsetY
                    ) else Offset.Zero

                    AnimatedVisibility(
                        visible = entry.item.id !in pendingDeletionIds,
                        enter = fadeIn() + scaleIn(initialScale = 0.98f),
                        exit = fadeOut() + scaleOut(targetScale = 0.98f)
                    ) {
                        LibraryNoteCard(
                            item = entry.item,
                            onOpen = { onOpen(entry.item.id) },
                            onTogglePin = {
                                val pinnedNow = entry.item.pinned.toPinnedBoolean()
                                onTogglePin(entry.item.id, if (pinnedNow) 0L else 1L)
                            },
                            onDelete = { onDelete(entry.item.id) },
                            modifier = Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    translationX = dragOffset.x
                                    translationY = dragOffset.y
                                    if (isDragging) {
                                        shadowElevation = 18f
                                        scaleX = 1.01f
                                        scaleY = 1.01f
                                    }
                                }
                                .pointerInput(entry.key) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            reorderState.startDragging(index)
                                        },
                                        onDragCancel = {
                                            reorderState.stopDragging()
                                        },
                                        onDragEnd = {
                                            reorderState.stopDragging()
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            reorderState.dragBy(dragAmount)
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}
