package com.example.ciphernote.ui.screens.noteslist

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import com.example.ciphernote.data.Note
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun NotesListScreen() {
    var searchQuery by remember { mutableStateOf("") }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showOpenDialog by remember { mutableStateOf<Note?>(null) }

    val notes = remember {
        mutableStateListOf(
            Note(1, "Title 1", "2026-02-21 08:00:00"),
            Note(2, "Title 2", "2026-02-20 08:00:00"),
            Note(3, "Title 3", "2026-02-10 08:00:00"),
        )
    }

    val filteredNotes = notes.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(5.dp, 20.dp, 5.dp, 5.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus() // lose focus
            }
    ) {

        // Top Bar (Search + Add)
        TopBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onAddClick = { showCreateDialog = true }
        )

        // Notes List
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredNotes) { note ->
                NoteItem(note = note) {
                    showOpenDialog = note
                }
            }
        }
    }

    // Create Note Dialog
    if (showCreateDialog) {
        CreateNoteDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, password ->
                val newNote = Note(
                    id = notes.size + 1,
                    title = title,
                    createdAt = getCurrentTime()
                )
                notes.add(0, newNote)
                showCreateDialog = false

                // TODO: encrypt & save note
            }
        )
    }

    // Open Note Dialog
    showOpenDialog?.let { note ->
        OpenNoteDialog(
            note = note,
            onDismiss = { showOpenDialog = null },
            onOpen = { password ->
                showOpenDialog = null

                // TODO: decrypt note
            }
        )
    }
}

@Composable
fun TopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val view = LocalView.current

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)

            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val isKeyboardOpen = keypadHeight > screenHeight * 0.15

            // keyboard closed while field was focused
            if (!isKeyboardOpen && isFocused) {
                focusManager.clearFocus()
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    isFocused = it.isFocused
                },
            singleLine = true,
            placeholder = { Text("Search") },

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = note.createdAt,
            style = MaterialTheme.typography.bodySmall
        )
    }

    Divider()
}

@Composable
fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, password: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Note") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onCreate(title, password)
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OpenNoteDialog(
    note: Note,
    onDismiss: () -> Unit,
    onOpen: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Open Note") },
        text = {
            Column {
                Text(text = note.title)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onOpen(password)
            }) {
                Text("Open")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getCurrentTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.now().format(formatter)
}
