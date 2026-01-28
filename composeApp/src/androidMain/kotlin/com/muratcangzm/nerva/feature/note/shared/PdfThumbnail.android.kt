package com.muratcangzm.nerva.feature.note.shared

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.net.toUri

@Composable
actual fun rememberPdfThumbnailPainter(uri: String): Painter? {
    val context = LocalContext.current

    val bmpState = produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val parsed = uri.toUri()
                val pfd: ParcelFileDescriptor = when (parsed.scheme) {
                    "file", null -> {
                        val path = parsed.path ?: uri
                        ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                    }
                    else -> {
                        context.contentResolver.openFileDescriptor(parsed, "r")
                            ?: return@withContext null
                    }
                }

                pfd.use { fd ->
                    PdfRenderer(fd).use { renderer ->
                        if (renderer.pageCount <= 0) return@withContext null
                        renderer.openPage(0).use { page ->
                            val width = (page.width * 0.40f).toInt().coerceAtLeast(260)
                            val height = (page.height * 0.40f).toInt().coerceAtLeast(340)
                            val bmp = createBitmap(width, height)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bmp
                        }
                    }
                }
            }.getOrNull()
        }
    }

    val bmp = bmpState.value ?: return null
    return BitmapPainter(bmp.asImageBitmap())
}
