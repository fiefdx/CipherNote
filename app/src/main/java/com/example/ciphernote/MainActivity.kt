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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ciphernote.data.CipherNoteDbHelper
import com.example.ciphernote.data.Note
import com.example.ciphernote.security.CryptoManager
import com.example.ciphernote.ui.screens.editnote.EditNoteScreen
import com.example.ciphernote.ui.screens.initial.InitialScreen
import com.example.ciphernote.ui.screens.noteslist.NotesListScreen
import com.example.ciphernote.ui.theme.CipherNoteTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Allow rotation; removed forced portrait orientation
        enableEdgeToEdge()
        setContent {
            CipherNoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotesApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

sealed class Screen {
    object Initial : Screen()
    object List : Screen()
    data class Edit(val note: Note, val password: String) : Screen()
}

@Composable
fun NotesApp(
    modifier: Modifier
) {
    val notesViewModel: NotesViewModel = viewModel()
    var currentScreen by notesViewModel.currentScreen

    var selectedNote by notesViewModel.selectedNote
    var showOpenDialog by notesViewModel.showOpenDialog
    var showErrorDialog by notesViewModel.showErrorDialog
    var needToSort by notesViewModel.needToSort

    val context = LocalContext.current
    val dbHelper = remember { CipherNoteDbHelper(context) }
    val notes = remember {
        mutableStateListOf<Note>().apply {
            addAll(dbHelper.getAll())
        }
    }

    val crypto = remember { CryptoManager() }

    fun formatNow(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }

    when (val screen = currentScreen) {
        is Screen.Initial -> {
            InitialScreen()
            LaunchedEffect(screen) {
                delay(2000L)
                currentScreen = Screen.List
            }
        }

        is Screen.List -> {
            NotesListScreen(
                modifier = modifier,
                notes = notes,

                onNoteClick = {
                    selectedNote = it
                },

                onAddNote = { title, _ ->
                    val encryptedContent = crypto.encrypt("")
                    val now = formatNow()

                    val newNote = Note(
                        id = 0,
                        title = title,
                        content = encryptedContent,
                        createdAt = now,
                        modifiedAt = now
                    )

                    val dbId = dbHelper.insert(newNote).toInt()
                    notes.add(0, newNote.copy(id = dbId))
                },

                onOpenNote = { note, _ ->
                    needToSort = false

                    try {
                        showOpenDialog = false

                        val decryptedContent = crypto.decrypt(note.content)

                        val decryptedNote = note.copy(content = decryptedContent)
                        currentScreen = Screen.Edit(decryptedNote, "")

                    } catch (e: Exception) {
                        showOpenDialog = false
                        showErrorDialog = true
                    }
                }
            )
        }

        is Screen.Edit -> {
            EditNoteScreen(
                note = screen.note,
                password = screen.password,

                onBack = {
                    currentScreen = Screen.List
                    if (needToSort) {
                        notes.sortByDescending { it.modifiedAt }
                    }
                },

                onSave = { updated ->
                    val encryptedContent = crypto.encrypt(updated.content)
                    val now = formatNow()

                    val newUpdated = updated.copy(
                        content = encryptedContent,
                        modifiedAt = now
                    )

                    val index = notes.indexOfFirst { it.id == newUpdated.id }
                    if (index != -1) {
                        notes[index] = newUpdated
                        dbHelper.update(newUpdated)
                    }

                    needToSort = true
                },

                onDelete = {
                    notes.removeIf { it.id == screen.note.id }
                    dbHelper.delete(screen.note.id)
                    currentScreen = Screen.List
                }
            )
        }
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
