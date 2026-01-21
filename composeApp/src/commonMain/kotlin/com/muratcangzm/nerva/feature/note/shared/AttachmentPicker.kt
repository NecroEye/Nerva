package com.muratcangzm.nerva.feature.note.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface AttachmentPicker {
    fun pickImage()
    fun pickPdf()
}

@Composable
expect fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onMessage: suspend (String) -> Unit,
): AttachmentPicker
