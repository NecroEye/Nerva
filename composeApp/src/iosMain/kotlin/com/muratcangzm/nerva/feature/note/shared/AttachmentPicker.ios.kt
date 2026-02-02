package com.muratcangzm.nerva.feature.note.shared

import androidx.compose.runtime.Composable

@Composable
actual fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onMessage: suspend (String) -> Unit,
): AttachmentPicker = object : AttachmentPicker {
    override fun pickImage() {}
    override fun pickPdf() {}
}