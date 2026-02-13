package com.muratcangzm.nerva.feature.schedule.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.muratcangzm.nerva.feature.schedule.ScheduleEvent
import com.muratcangzm.nerva.feature.schedule.ScheduleState
import com.muratcangzm.nerva.feature.schedule.ScheduleViewModel
import com.muratcangzm.nerva.feature.schedule.model.ScheduleDefaults
import com.muratcangzm.nerva.feature.schedule.model.ScheduleItem
import com.muratcangzm.nerva.feature.schedule.model.ScheduleItemKind
import com.muratcangzm.nerva.feature.schedule.ui.layout.computeDayLayout
import com.muratcangzm.nerva.feature.schedule.util.newScheduleId
import kotlinx.datetime.DayOfWeek
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: ScheduleViewModel = koinInject()
    val scheduleState by viewModel.state.collectAsState()

    DisposableEffect(viewModel) {
        onDispose { viewModel.close() }
    }

    ScheduleRoute(
        scheduleState = scheduleState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleRoute(
    scheduleState: ScheduleState,
    onEvent: (ScheduleEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayItems = remember(scheduleState.items, scheduleState.selectedDay) {
        scheduleState.items.filter { scheduleItem -> scheduleItem.dayOfWeek == scheduleState.selectedDay }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Schedule") },
                actions = {
                    TextButton(
                        onClick = {
                            val scheduleItem = ScheduleItem(
                                id = newScheduleId(),
                                kind = ScheduleItemKind.COURSE,
                                title = "New Course",
                                dayOfWeek = scheduleState.selectedDay,
                                startMinute = 9 * 60,
                                endMinute = 10 * 60,
                                colorArgb = 0xFF6A5ACDu,
                                location = "Room 101",
                                teacher = "Teacher",
                                notes = null,
                            )
                            onEvent(ScheduleEvent.Upsert(scheduleItem))
                        }
                    ) { Text("Add") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 24.dp
                )
                .fillMaxSize()
        ) {
            ScheduleWeekHeader(
                selectedDay = scheduleState.selectedDay,
                onSelectDay = { dayOfWeek -> onEvent(ScheduleEvent.SelectDay(dayOfWeek)) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            ScheduleTimelineDayView(
                items = dayItems,
                onMoveByMinutes = { scheduleItemId, deltaMinutes ->
                    onEvent(ScheduleEvent.MoveByMinutes(scheduleItemId, deltaMinutes))
                },
                onResizeTopByMinutes = { scheduleItemId, deltaMinutes ->
                    onEvent(ScheduleEvent.ResizeTopByMinutes(scheduleItemId, deltaMinutes))
                },
                onResizeBottomByMinutes = { scheduleItemId, deltaMinutes ->
                    onEvent(ScheduleEvent.ResizeBottomByMinutes(scheduleItemId, deltaMinutes))
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ScheduleWeekHeader(
    selectedDay: DayOfWeek,
    onSelectDay: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = remember {
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    }

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        days.forEach { dayOfWeek ->
            val selected = dayOfWeek == selectedDay

            val containerColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.04f)
                },
                label = "ScheduleDayChipContainer"
            )

            val borderColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                },
                label = "ScheduleDayChipBorder"
            )

            val labelColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
                },
                label = "ScheduleDayChipLabel"
            )

            FilterChip(
                selected = selected,
                onClick = { onSelectDay(dayOfWeek) },
                label = {
                    Text(
                        text = dayLabel(dayOfWeek),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                },
                shape = RoundedCornerShape(999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = containerColor,
                    containerColor = containerColor,
                    selectedLabelColor = labelColor,
                    labelColor = labelColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = borderColor,
                    selectedBorderColor = borderColor,
                    borderWidth = 0.8.dp,
                    selectedBorderWidth = 0.9.dp
                )
            )
        }
    }
}

private fun dayLabel(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    else -> dayOfWeek.name.take(3)
}

@Composable
private fun ScheduleTimelineDayView(
    items: List<ScheduleItem>,
    onMoveByMinutes: (String, Int) -> Unit,
    onResizeTopByMinutes: (String, Int) -> Unit,
    onResizeBottomByMinutes: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hourHeight = 96.dp
    val density = LocalDensity.current
    val verticalScrollState = rememberScrollState()

    val pixelsPerMinute = remember(hourHeight, density) {
        with(density) { hourHeight.toPx() / 60f }
    }

    val dayStartMinute = 0
    val dayEndMinute = 24 * 60
    val totalMinutes = dayEndMinute - dayStartMinute

    val placedEvents = remember(items) { computeDayLayout(items) }

    val visibleLaneCount = remember(placedEvents) {
        val maximumLaneCount = placedEvents.maxOfOrNull { placedEvent -> placedEvent.laneCount } ?: 1
        maximumLaneCount.coerceAtMost(3).coerceAtLeast(1)
    }

    val previousIdsState = remember { mutableSetOf<String>() }

    androidx.compose.runtime.LaunchedEffect(items) {
        val previousIds = previousIdsState.toSet()
        val currentIds = items.mapTo(mutableSetOf()) { it.id }

        val addedId = (currentIds - previousIds).firstOrNull()
        if (addedId != null) {
            val addedItem = items.firstOrNull { it.id == addedId }
            if (addedItem != null) {
                val rawTargetPx = (addedItem.startMinute - dayStartMinute) * pixelsPerMinute
                val paddingPx = with(density) { 64.dp.toPx() }

                val target = (rawTargetPx - paddingPx)
                    .roundToInt()
                    .coerceIn(0, verticalScrollState.maxValue)

                verticalScrollState.animateScrollTo(target)
            }
        }

        previousIdsState.clear()
        previousIdsState.addAll(currentIds)
    }

    Row(modifier) {
        TimeGutterAllDay(
            hourHeight = hourHeight,
            modifier = Modifier
                .width(64.dp)
                .verticalScroll(verticalScrollState)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(end = 12.dp)
        ) {
            val contentHeight = remember(totalMinutes, hourHeight) {
                val hours = totalMinutes / 60
                hourHeight * hours
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentHeight)
            ) {
                val viewportWidthPixels = constraints.maxWidth.toFloat()
                val laneWidthPixels = viewportWidthPixels / visibleLaneCount

                TimeGridBackgroundAllDay(
                    hourHeight = hourHeight,
                    modifier = Modifier.matchParentSize()
                )

                placedEvents.forEach { placedEvent ->
                    val cappedLaneIndex = placedEvent.laneIndex.coerceAtMost(2)
                    val leftPixels = laneWidthPixels * cappedLaneIndex

                    val topPixels =
                        (placedEvent.item.startMinute - dayStartMinute) * pixelsPerMinute
                    val heightPixels =
                        (placedEvent.item.endMinute - placedEvent.item.startMinute) * pixelsPerMinute

                    EventBlock(
                        item = placedEvent.item,
                        laneLeftPx = leftPixels,
                        laneWidthPx = laneWidthPixels,
                        topPx = topPixels,
                        heightPx = heightPixels,
                        pxPerMinute = pixelsPerMinute,
                        onMoveByMinutes = onMoveByMinutes,
                        onResizeTopByMinutes = onResizeTopByMinutes,
                        onResizeBottomByMinutes = onResizeBottomByMinutes,
                    )
                }

                ScrollEdgeFade(
                    scrollValue = verticalScrollState.value,
                    scrollMax = verticalScrollState.maxValue,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
private fun TimeGutterAllDay(
    hourHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(top = 2.dp)) {
        for (hour in 0..24) {
            Box(Modifier.height(hourHeight)) {
                Text(
                    text = "${(hour % 24).toString().padStart(2, '0')}:00",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 2.dp, start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeGridBackgroundAllDay(
    hourHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        for (hour in 0 until 24) {
            Box(Modifier.height(hourHeight)) {
                HorizontalDivider(modifier = Modifier.align(Alignment.TopStart))
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun ScrollEdgeFade(
    scrollValue: Int,
    scrollMax: Int,
    modifier: Modifier = Modifier,
) {
    val edgeHeight = 28.dp
    val showTop = scrollValue > 0
    val showBottom = scrollValue < scrollMax

    val topAlpha by animateColorAsState(
        targetValue = if (showTop) Color.Black.copy(alpha = 0.14f) else Color.Transparent,
        label = "ScheduleFadeTop"
    )

    val bottomAlpha by animateColorAsState(
        targetValue = if (showBottom) Color.Black.copy(alpha = 0.14f) else Color.Transparent,
        label = "ScheduleFadeBottom"
    )

    Box(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(edgeHeight)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(topAlpha, Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(edgeHeight)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, bottomAlpha)
                    )
                )
        )
    }
}

@Composable
private fun EventBlock(
    item: ScheduleItem,
    laneLeftPx: Float,
    laneWidthPx: Float,
    topPx: Float,
    heightPx: Float,
    pxPerMinute: Float,
    onMoveByMinutes: (String, Int) -> Unit,
    onResizeTopByMinutes: (String, Int) -> Unit,
    onResizeBottomByMinutes: (String, Int) -> Unit,
) {
    val roundedCornerShape = RoundedCornerShape(12.dp)
    val surfaceColor = Color(item.colorArgb)

    var pendingMovePixels by remember(item.id) { mutableFloatStateOf(0f) }
    var pendingResizeTopPixels by remember(item.id) { mutableFloatStateOf(0f) }
    var pendingResizeBottomPixels by remember(item.id) { mutableFloatStateOf(0f) }

    fun dispatchSnapped(pendingPixels: Float, dispatch: (Int) -> Unit): Float {
        val pendingMinutes = pendingPixels / pxPerMinute
        val steps = (pendingMinutes / ScheduleDefaults.SnapMinutes).toInt()
        val deltaMinutes = steps * ScheduleDefaults.SnapMinutes
        if (deltaMinutes != 0) dispatch(deltaMinutes)
        return pendingPixels - (deltaMinutes * pxPerMinute)
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(x = laneLeftPx.roundToInt(), y = topPx.roundToInt()) }
            .width(with(LocalDensity.current) { laneWidthPx.toDp() })
            .height(with(LocalDensity.current) { heightPx.toDp() })
            .padding(horizontal = 4.dp)
    ) {
        Surface(
            color = surfaceColor.copy(alpha = 0.92f),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = roundedCornerShape,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(item.id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            pendingMovePixels += dragAmount.y
                            pendingMovePixels = dispatchSnapped(pendingMovePixels) { deltaMinutes ->
                                onMoveByMinutes(item.id, deltaMinutes)
                            }
                        }
                    )
                }
        ) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val metaText = buildString {
                        if (!item.location.isNullOrBlank()) append(item.location)
                        if (!item.teacher.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append(item.teacher)
                        }
                    }

                    if (metaText.isNotBlank()) {
                        Text(
                            text = metaText,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                ResizeHandle(
                    modifier = Modifier.align(Alignment.TopCenter),
                    onDeltaPx = { deltaPixels ->
                        pendingResizeTopPixels += deltaPixels
                        pendingResizeTopPixels = dispatchSnapped(pendingResizeTopPixels) { deltaMinutes ->
                            onResizeTopByMinutes(item.id, deltaMinutes)
                        }
                    }
                )

                ResizeHandle(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onDeltaPx = { deltaPixels ->
                        pendingResizeBottomPixels += deltaPixels
                        pendingResizeBottomPixels = dispatchSnapped(pendingResizeBottomPixels) { deltaMinutes ->
                            onResizeBottomByMinutes(item.id, deltaMinutes)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ResizeHandle(
    modifier: Modifier = Modifier,
    onDeltaPx: (Float) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(horizontal = 10.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDeltaPx(dragAmount.y)
                    }
                )
            }
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(44.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(999.dp))
        )
    }
}
