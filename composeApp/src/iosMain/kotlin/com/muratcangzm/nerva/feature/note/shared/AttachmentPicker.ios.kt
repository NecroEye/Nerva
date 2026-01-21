package com.muratcangzm.nerva.feature.note.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onMessage: suspend (String) -> Unit,
): AttachmentPicker {
    return remember {
        object : AttachmentPicker {
            override fun pickImage() { /* iOS picker ekleyince burası dolacak */ }
            override fun pickPdf() { /* iOS picker ekleyince burası dolacak */ }
        }
    }
}