package com.muratcangzm.nerva.feature.library.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.muratcangzm.data.model.Note
import com.muratcangzm.data.model.NoteAttachmentKind
import com.muratcangzm.data.model.NoteAttachmentPreview
import com.muratcangzm.nerva.feature.library.DEFAULT_TAG_COLOR_ARGB
import com.muratcangzm.nerva.feature.library.LibraryAttachmentKind
import com.muratcangzm.nerva.feature.library.LibraryAttachmentPreview
import com.muratcangzm.nerva.feature.library.LibraryNoteItem

internal fun Note.toLibraryItem(): LibraryNoteItem {
    val preview = content
        .lineSequence()
        .joinToString(" ")
        .trim()
        .take(140)

    val previews = primaryAttachment
        ?.let { listOf(it.toLibraryPreview()) }
        .orEmpty()

    return LibraryNoteItem(
        id = id.value,
        title = title.ifBlank { "Untitled" },
        preview = preview,
        pinned = pinned,
        updatedAtEpochMs = updatedAtEpochMs,
        tags = emptyList(),
        attachmentsCount = attachmentsCount,
        tagColorArgb = DEFAULT_TAG_COLOR_ARGB,
        attachmentPreviews = previews
    )
}

private fun NoteAttachmentPreview.toLibraryPreview(): LibraryAttachmentPreview {
    return LibraryAttachmentPreview(
        id = id,
        kind = kind.toLibraryKind(),
        uri = uri,
        label = label,
    )
}

private fun NoteAttachmentKind.toLibraryKind(): LibraryAttachmentKind = when (this) {
    NoteAttachmentKind.Photo -> LibraryAttachmentKind.Photo
    NoteAttachmentKind.Pdf -> LibraryAttachmentKind.Pdf
}

fun formatShortDateTime(epochMs: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(TimeZone.currentSystemDefault())
    val hh = dt.hour.toString().padStart(2, '0')
    val mm = dt.minute.toString().padStart(2, '0')
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val mo = dt.monthNumber.toString().padStart(2, '0')
    return "$dd.$mo  $hh:$mm"
}
