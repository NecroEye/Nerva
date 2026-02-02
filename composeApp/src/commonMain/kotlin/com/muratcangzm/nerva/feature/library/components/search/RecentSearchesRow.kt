package com.muratcangzm.nerva.feature.library.components.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muratcangzm.nerva.feature.library.components.chip.TagChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentSearchesRow(
    recent: List<String>,
    onClick: (String) -> Unit,
    onClear: () -> Unit,
) {
    if (recent.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onClear) {
                Text(text = "Clear")
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recent.forEach { q ->
                TagChip(
                    text = q,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                    modifier = Modifier.clickable { onClick(q) }
                )
            }
        }
    }
}
