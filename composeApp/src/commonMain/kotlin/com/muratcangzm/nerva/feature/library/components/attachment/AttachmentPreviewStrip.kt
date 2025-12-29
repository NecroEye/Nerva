package com.muratcangzm.nerva.feature.library.components.attachment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AttachmentPreviewStrip(
    attachmentsCount: Int,
    modifier: Modifier = Modifier
) {
    if (attachmentsCount <= 0) return

    val shown = minOf(3, attachmentsCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(shown) { index ->
            val remaining = attachmentsCount - shown

            Surface(
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                val text = when {
                    index == shown - 1 && remaining > 0 -> "+$remaining"
                    else -> "📎"
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
