package com.muratcangzm.nerva.feature.note.shared

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

@Composable
actual fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onMessage: suspend (String) -> Unit,
): AttachmentPicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun show(msg: String) {
        scope.launch { onMessage(msg) }
    }

    fun persistReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure {
            show("Permission failed: ${it.message ?: "unknown"}")
        }
    }

    fun newId(): String {
        val t = Clock.System.now().toEpochMilliseconds()
        val r = Random.nextInt(100_000, 999_999)
        return "$t-$r"
    }

    fun displayNameOf(uri: Uri): String {
        val cr = context.contentResolver
        val name = runCatching {
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
            }
        }.getOrNull()
        return name ?: uri.lastPathSegment ?: "attachment"
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        persistReadPermission(uri)
        onPicked(
            PickedAttachment(
                id = newId(),
                kind = NoteAttachmentKind.Image,
                displayName = displayNameOf(uri),
                uri = uri.toString()
            )
        )
    }

    val pickPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        persistReadPermission(uri)
        onPicked(
            PickedAttachment(
                id = newId(),
                kind = NoteAttachmentKind.Pdf,
                displayName = displayNameOf(uri),
                uri = uri.toString()
            )
        )
    }

    return remember {
        object : AttachmentPicker {
            override fun pickImage() = pickImageLauncher.launch(arrayOf("image/*"))
            override fun pickPdf() = pickPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }
}
