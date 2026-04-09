package com.example.ciphernote.ui.screens.editnote

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.ciphernote.data.Note

@Composable
fun EditNoteScreen(
    note: Note,
    onBack: () -> Unit,
    onSave: (Note) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    // Sync local state when note changes
    LaunchedEffect(key1 = note.id) {
        title = note.title
        content = note.content
    }
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 20.dp, 8.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onSave(note.copy(title = title, content = content)) }) {
                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(32.dp))
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Content area with vertical scroll bar
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    maxLines = Int.MAX_VALUE,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
            // Simple custom scrollbar using Canvas
            val thumbHeightDp = 50.dp
            val density = LocalDensity.current
            val thumbHeightPx = with(density) { thumbHeightDp.toPx() }
            val cornerRadiusPx = with(density) { 4.dp.toPx() }

            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .width(8.dp)
                    .fillMaxHeight()
            ) {
                val maxScroll = scrollState.maxValue.toFloat()
                if (maxScroll > 0f) {
                    val fraction = scrollState.value / maxScroll
                    val availableSpace = size.height - thumbHeightPx
                    val thumbTop = (availableSpace * fraction).coerceIn(0f, availableSpace)
                    drawRoundRect(
                        color = Color.Gray,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, thumbTop),
                        size = androidx.compose.ui.geometry.Size(width = size.width, height = thumbHeightPx),
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = {
                Column {
                    Text(note.title)
                    Text(note.createdAt.toString())
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
