package com.muratcangzm.nerva.feature.library.components.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun MasonryGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.padding(contentPadding),
        content = content
    ) { measurables, constraints ->
        require(columns > 0)

        val hSpacePx = horizontalSpacing.roundToPx()
        val vSpacePx = verticalSpacing.roundToPx()

        val availableWidth = (constraints.maxWidth - hSpacePx * (columns - 1)).coerceAtLeast(0)
        val columnWidth = (availableWidth / columns).coerceAtLeast(0)

        val itemConstraints = constraints.copy(
            minWidth = columnWidth,
            maxWidth = columnWidth
        )

        val placeables = measurables.map { it.measure(itemConstraints) }

        val colHeights = IntArray(columns) { 0 }
        val positionsX = IntArray(placeables.size)
        val positionsY = IntArray(placeables.size)

        placeables.forEachIndexed { index, p ->
            var targetCol = 0
            var minHeight = colHeights[0]
            for (c in 1 until columns) {
                val h = colHeights[c]
                if (h < minHeight) {
                    minHeight = h
                    targetCol = c
                }
            }

            val x = targetCol * (columnWidth + hSpacePx)
            val y = colHeights[targetCol]

            positionsX[index] = x
            positionsY[index] = y

            colHeights[targetCol] = y + p.height + vSpacePx
        }

        val rawHeight = (colHeights.maxOrNull() ?: 0)
        val trimmedHeight = if (placeables.isEmpty()) 0 else (rawHeight - vSpacePx).coerceAtLeast(0)
        val height = max(trimmedHeight, constraints.minHeight)

        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { index, p ->
                p.placeRelative(positionsX[index], positionsY[index])
            }
        }
    }
}
