package com.muratcangzm.nerva.feature.note.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

@Composable
actual fun rememberPdfThumbnailPainter(uri: String): Painter? {
    val context = LocalContext.current

    val bmpState = produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            PdfThumbDiskCache.getOrCreate(
                context = context,
                uriString = uri,
                targetWidth = 520,
                targetHeight = 700,
                maxFiles = 120
            )
        }
    }

    val bmp = bmpState.value ?: return null
    return BitmapPainter(bmp.asImageBitmap())
}

private object PdfThumbDiskCache {

    fun getOrCreate(
        context: Context,
        uriString: String,
        targetWidth: Int,
        targetHeight: Int,
        maxFiles: Int
    ): Bitmap? {
        val dir = File(context.cacheDir, "pdf_thumbs").apply { mkdirs() }
        val key = sha256("$uriString|$targetWidth|$targetHeight")
        val file = File(dir, "thumb_$key.png")

        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)?.let { return it }
        }

        val rendered = renderFirstPage(context, uriString, targetWidth, targetHeight) ?: return null

        runCatching {
            FileOutputStream(file).use { out ->
                rendered.compress(Bitmap.CompressFormat.PNG, 92, out)
            }
            file.setLastModified(System.currentTimeMillis())
            trim(dir, maxFiles)
        }

        return rendered
    }

    private fun renderFirstPage(
        context: Context,
        uriString: String,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        return runCatching {
            val pfd: ParcelFileDescriptor =
                context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r") ?: return null

            pfd.use {
                PdfRenderer(it).use { renderer ->
                    if (renderer.pageCount <= 0) return null
                    renderer.openPage(0).use { page ->
                        val pageRatio = page.width.toFloat() / page.height.toFloat()
                        val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()

                        val (w, h) = if (pageRatio >= targetRatio) {
                            val w = targetWidth
                            val h = (w / pageRatio).toInt().coerceAtLeast(1)
                            w to h
                        } else {
                            val h = targetHeight
                            val w = (h * pageRatio).toInt().coerceAtLeast(1)
                            w to h
                        }

                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bmp
                    }
                }
            }
        }.getOrNull()
    }

    private fun trim(dir: File, maxFiles: Int) {
        val files = dir.listFiles { f -> f.extension.equals("png", ignoreCase = true) } ?: return
        if (files.size <= maxFiles) return

        files.sortBy { it.lastModified() }
        val toDelete = files.size - maxFiles
        for (i in 0 until toDelete) runCatching { files[i].delete() }
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return buildString(bytes.size * 2) {
            for (b in bytes) append("%02x".format(b))
        }
    }
}