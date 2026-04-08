package com.example.ciphernote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ciphernote.data.Note
import com.example.ciphernote.ui.screens.editnote.EditNoteScreen
import com.example.ciphernote.ui.screens.initial.InitialScreen
import com.example.ciphernote.ui.screens.noteslist.NotesListScreen
import com.example.ciphernote.ui.screens.noteslist.OpenNoteDialog
import com.example.ciphernote.ui.theme.CipherNoteTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CipherNoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    InitialScreen()
//                    NotesListScreen()
                    NotesApp()
                }
            }
        }
    }
}

sealed class Screen {
    object List : Screen()
    data class Edit(val note: Note) : Screen()
}

@Composable
fun NotesApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val notes = remember {
        mutableStateListOf(
            Note(1, "Title 1", "2026-02-21 08:00:00")
        )
    }

    when (val screen = currentScreen) {

        is Screen.List -> {
            NotesListScreen(
                notes = notes,
                onNoteClick = {
                    selectedNote = it
                    showOpenDialog = true
                }
            )
        }

        is Screen.Edit -> {
            EditNoteScreen(
                note = screen.note,
                onBack = { currentScreen = Screen.List },
                onSave = { updated ->
                    val index = notes.indexOfFirst { it.id == updated.id }
                    if (index != -1) notes[index] = updated
                    currentScreen = Screen.List
                },
                onDelete = {
                    notes.removeIf { it.id == screen.note.id }
                    currentScreen = Screen.List
                }
            )
        }
    }

    // Open dialog
    if (showOpenDialog && selectedNote != null) {
        OpenNoteDialog(
            note = selectedNote!!,
            onDismiss = { showOpenDialog = false },
            onOpen = { password ->
                if (password == "1234") { // TODO replace with real encryption check
                    showOpenDialog = false
                    currentScreen = Screen.Edit(selectedNote!!)
                } else {
                    showOpenDialog = false
                    showErrorDialog = true
                }
            }
        )
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Warning") },
            text = { Text("Open failed with wrong password") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}