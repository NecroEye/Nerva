package com.muratcangzm.nerva.feature.note.shared

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.UUID

@Composable
actual fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onMessage: suspend (String) -> Unit,
): AttachmentPicker {
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onPicked(
                PickedAttachment(
                    id = UUID.randomUUID().toString(),
                    kind = NoteAttachmentKind.Image,
                    displayName = context.displayName(it) ?: "image",
                    uri = it.toString()
                )
            )
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onPicked(
                PickedAttachment(
                    id = UUID.randomUUID().toString(),
                    kind = NoteAttachmentKind.Pdf,
                    displayName = context.displayName(it) ?: "document.pdf",
                    uri = it.toString()
                )
            )
        }
    }

    return remember {
        object : AttachmentPicker {
            override fun pickImage() = imageLauncher.launch("image/*")
            override fun pickPdf() = pdfLauncher.launch("application/pdf")
        }
    }
}

private fun Context.displayName(uri: Uri): String? {
    val c: Cursor? = contentResolver.query(uri, null, null, null, null)
    c?.use {
        val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && it.moveToFirst()) return it.getString(idx)
    }
    return null
}
