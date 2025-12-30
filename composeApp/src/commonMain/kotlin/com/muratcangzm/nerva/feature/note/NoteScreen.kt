package com.muratcangzm.nerva.feature.note

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    noteId: String,
    onBack: () -> Unit,
    padding: PaddingValues = PaddingValues()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { inner ->
        Text(
            text = "Note details: $noteId",
            modifier = Modifier
                .padding(inner)
                .padding(padding)
                .fillMaxSize()
        )
    }
}
