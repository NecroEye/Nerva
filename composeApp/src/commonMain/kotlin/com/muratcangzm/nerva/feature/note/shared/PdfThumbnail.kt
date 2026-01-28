package com.muratcangzm.nerva.feature.note.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
expect fun rememberPdfThumbnailPainter(uri: String): Painter?