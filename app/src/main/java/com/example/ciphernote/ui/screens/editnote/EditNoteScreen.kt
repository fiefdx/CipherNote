package com.example.ciphernote.ui.screens.editnote

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
    
    LaunchedEffect(key1 = note.id) {
        title = note.title
        content = note.content
    }
    
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(96.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                
                // Actions
                IconButton(onClick = { onSave(note.copy(title = title, content = content)) }) {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.error)
                }
            }

            // Title Input
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // Content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = Int.MAX_VALUE
                    )
                }

                // Custom scrollbar
                val thumbHeightDp = 50.dp
                val density = LocalDensity.current
                val thumbHeightPx = with(density) { thumbHeightDp.toPx() }
                val cornerRadiusPx = with(density) { 4.dp.toPx() }
                val scrollbarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

                Canvas(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .width(6.dp)
                        .fillMaxHeight()
                ) {
                    val maxScroll = scrollState.maxValue.toFloat()
                    if (maxScroll > 0f) {
                        val fraction = scrollState.value / maxScroll
                        val availableSpace = size.height - thumbHeightPx
                        val thumbTop = (availableSpace * fraction).coerceIn(0f, availableSpace)
                        drawRoundRect(
                            color = scrollbarColor,
                            topLeft = Offset(0f, thumbTop),
                            size = Size(width = size.width, height = thumbHeightPx),
                            cornerRadius = CornerRadius(cornerRadiusPx)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
