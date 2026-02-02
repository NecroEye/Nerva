package com.muratcangzm.nerva.feature.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.muratcangzm.common.extension.toPinnedBoolean
import com.muratcangzm.nerva.design.NervaBranding
import com.muratcangzm.nerva.feature.library.LibraryAttachmentKind
import com.muratcangzm.nerva.feature.library.LibraryAttachmentPreview
import com.muratcangzm.nerva.feature.library.LibraryNoteItem
import com.muratcangzm.nerva.feature.library.components.chip.TagChip
import com.muratcangzm.nerva.feature.library.components.search.HighlightStyle
import com.muratcangzm.nerva.feature.library.components.search.highlightText
import com.muratcangzm.nerva.feature.library.util.formatShortDateTime
import com.muratcangzm.nerva.feature.note.shared.rememberPdfThumbnailPainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryNoteCard(
    item: LibraryNoteItem,
    query: String,
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

    val highlight = HighlightStyle(
        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
        foreground = MaterialTheme.colorScheme.onSurface
    )

    val titleText = highlightText(
        text = item.title.ifBlank { "Untitled" },
        query = query,
        style = highlight
    )

    val previewText = highlightText(
        text = item.preview,
        query = query,
        style = highlight
    )

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

        Row(modifier = Modifier.fillMaxWidth()) {
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = titleText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )

                        Text(
                            text = formatShortDateTime(item.updatedAtEpochMs),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = { menuExpanded = true }) {
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

                if (primary != null) {
                    AttachmentHeroPreview(
                        preview = primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TagChip(
                            text = primary.kind.toChipLabel(),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
                        )
                        if (isPinned) {
                            TagChip(
                                text = "Pinned",
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
                            )
                        }
                    }
                } else {
                    if (item.preview.isNotBlank()) {
                        PreviewBubble(
                            text = previewText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isPinned) {
                        TagChip(
                            text = "Pinned",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewBubble(
    text: androidx.compose.ui.text.AnnotatedString,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AttachmentHeroPreview(
    preview: LibraryAttachmentPreview,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    val pdfPainter = if (preview.kind == LibraryAttachmentKind.Pdf) {
        rememberPdfThumbnailPainter(preview.uri)
    } else null

    val photoPainter = if (preview.kind == LibraryAttachmentKind.Photo) {
        rememberAsyncImagePainter(preview.uri)
    } else null

    Box(
        modifier = modifier
            .clip(shape)
            .aspectRatio(1.55f)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (preview.kind) {
            LibraryAttachmentKind.Photo -> {
                Image(
                    painter = photoPainter!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            LibraryAttachmentKind.Pdf -> {
                if (pdfPainter != null) {
                    Image(
                        painter = pdfPainter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("📄", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

private fun LibraryAttachmentKind.toChipLabel(): String = when (this) {
    LibraryAttachmentKind.Photo -> "Image"
    LibraryAttachmentKind.Pdf -> "PDF"
}
