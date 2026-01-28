package com.muratcangzm.nerva.feature.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import com.muratcangzm.nerva.feature.library.util.formatShortDateTime
import com.muratcangzm.nerva.feature.note.shared.rememberPdfThumbnailPainter

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
                        verticalArrangement = Arrangement.spacedBy(4.dp)
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

                // ✅ HERO PREVIEW: sağdaki yazı kalktı, görsel büyük.
                if (primary != null) {
                    AttachmentHeroPreview(preview = primary)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TagChip(
                            text = when (primary.kind) {
                                LibraryAttachmentKind.Photo -> "Image"
                                LibraryAttachmentKind.Pdf -> "PDF"
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                        )

                        if (item.attachmentsCount > 1) {
                            TagChip(
                                text = "+${item.attachmentsCount - 1}",
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                            )
                        }
                    }
                } else if (item.preview.isNotBlank()) {
                    Text(
                        text = item.preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isPinned) {
                    TagChip(
                        text = "Pinned",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentHeroPreview(
    preview: LibraryAttachmentPreview,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.45f)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        when (preview.kind) {
            LibraryAttachmentKind.Photo -> {
                Image(
                    painter = rememberAsyncImagePainter(preview.uri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            LibraryAttachmentKind.Pdf -> {
                val pdfPainter = rememberPdfThumbnailPainter(preview.uri)
                if (pdfPainter != null) {
                    Image(
                        painter = pdfPainter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("📄", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
