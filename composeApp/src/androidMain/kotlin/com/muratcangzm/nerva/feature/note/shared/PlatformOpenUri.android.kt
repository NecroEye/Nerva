package com.muratcangzm.nerva.feature.note.shared

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable

@Composable
private fun ctx() = LocalContext.current

actual fun PlatformOpenUri(uri: String) {}