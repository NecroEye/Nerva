package com.muratcangzm.nerva.app.nav

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun NervaBottomBar(
    selected: TopTab,
    onSelected: (TopTab) -> Unit
) {
    val items = listOf(
        BottomItem(
            tab = TopTab.Library,
            label = "Library",
            icon = "📚"
        ),
        BottomItem(
            tab = TopTab.Schedule,
            label = "Schedule",
            icon = "🗓️"
        )
    )

    val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    val indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)

    NavigationBar(
        containerColor = containerColor,
        tonalElevation = 6.dp
    ) {
        items.forEach { item ->
            val isSelected = selected == item.tab

            val iconScale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isSelected) 1.10f else 1.00f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.85f,
                    stiffness = 520f
                ),
                label = "nav_icon_scale"
            )

            val labelAlpha by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isSelected) 1.00f else 0.78f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.95f,
                    stiffness = 700f
                ),
                label = "nav_label_alpha"
            )

            val itemColors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = indicatorColor
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelected(item.tab) },
                icon = {
                    Text(
                        text = item.icon,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.graphicsLayer {
                            alpha = labelAlpha
                        }
                    )
                },
                colors = itemColors
            )
        }
    }
}

private data class BottomItem(
    val tab: TopTab,
    val label: String,
    val icon: String
)