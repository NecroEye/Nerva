package com.muratcangzm.nerva.feature.note.shared

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random
import kotlin.time.Clock

@Composable
actual fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onMessage: suspend (String) -> Unit,
): AttachmentPicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
        return name ?: (uri.lastPathSegment ?: "attachment")
    }

    fun tryPersistReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    suspend fun copyIntoAppStorage(
        ctx: Context,
        source: Uri,
        id: String,
        kind: NoteAttachmentKind,
    ): String {
        val dir = File(ctx.filesDir, "attachments").apply { mkdirs() }

        val ext = when (kind) {
            NoteAttachmentKind.Image -> "jpg"
            NoteAttachmentKind.Pdf -> "pdf"
        }

        val outFile = File(dir, "$id.$ext")
        if (outFile.exists() && outFile.length() > 0L) {
            return Uri.fromFile(outFile).toString()
        }

        val ok = runCatching {
            ctx.contentResolver.openInputStream(source)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("openInputStream returned null")
        }.isSuccess

        return if (ok) Uri.fromFile(outFile).toString() else source.toString()
    }

    fun handlePicked(uri: Uri, kind: NoteAttachmentKind) {
        val id = newId()
        val name = displayNameOf(uri)
        tryPersistReadPermission(uri)

        scope.launch {
            val storedUri = withContext(Dispatchers.IO) {
                copyIntoAppStorage(context, uri, id, kind)
            }
            onPicked(
                PickedAttachment(
                    id = id,
                    kind = kind,
                    displayName = name,
                    uri = storedUri
                )
            )
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch { onMessage("Image selected") }
        handlePicked(uri, NoteAttachmentKind.Image)
    }

    val pickPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch { onMessage("PDF selected") }
        handlePicked(uri, NoteAttachmentKind.Pdf)
    }

    return object : AttachmentPicker {
        override fun pickImage() = pickImageLauncher.launch(arrayOf("image/*"))
        override fun pickPdf() = pickPdfLauncher.launch(arrayOf("application/pdf"))
    }
}
