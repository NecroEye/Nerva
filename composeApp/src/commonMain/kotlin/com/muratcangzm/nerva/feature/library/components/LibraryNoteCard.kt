package com.muratcangzm.nerva.feature.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muratcangzm.common.extension.toPinnedBoolean
import com.muratcangzm.nerva.design.NervaBranding
import com.muratcangzm.nerva.feature.library.LibraryAttachmentKind
import com.muratcangzm.nerva.feature.library.LibraryNoteItem
import com.muratcangzm.nerva.feature.library.components.chip.TagChip
import com.muratcangzm.nerva.feature.library.util.formatShortDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryNoteCard(
    item: LibraryNoteItem,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPinned = item.pinned.toPinnedBoolean()
    val brand = NervaBranding.current
    val accent = Color(item.tagColorArgb)

    val primary = item.attachmentPreviews.firstOrNull()

    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = onOpen,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            brand.gradientStart.copy(alpha = 0.90f),
                            brand.gradientEnd.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title.ifBlank { "Untitled" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }

                        Text(
                            text = formatShortDateTime(item.updatedAtEpochMs),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = { menuExpanded = true }
                        ) {
                            Text(
                                text = "⋯",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isPinned) "📌 Unpin" else "📌 Pin") },
                                onClick = {
                                    menuExpanded = false
                                    onTogglePin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Delete") },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                if (primary != null || item.preview.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (primary != null) {
                            AttachmentThumbnail(primary.kind)
                            Spacer(Modifier.width(10.dp))
                        }

                        if (item.preview.isNotBlank()) {
                            Text(
                                text = item.preview,
                                modifier = Modifier
                                    .weight(1f)
                                    .basicMarquee(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }

                if (isPinned) {
                    TagChip(
                        text = "Pinned",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                        modifier = Modifier
                            .padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentThumbnail(kind: LibraryAttachmentKind) {
    val emoji = when (kind) {
        LibraryAttachmentKind.Photo -> "🖼️"
        LibraryAttachmentKind.Pdf -> "📄"
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji)
    }
}
