package com.example.ciphernote.ui.screens.editnote

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.example.ciphernote.data.Note
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 20.dp, 8.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save
            IconButton(onClick = { onSave(note.copy(title = title, content = content)) }) {
                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(32.dp))
            }
        }

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = MaterialTheme.typography.titleLarge
        )

        // Content
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }

    // 🗑 Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = {
                Column {
                    Text(note.title)
                    Text(note.createdAt)
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
