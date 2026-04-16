package com.example.ciphernote

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ciphernote.data.Note
import com.example.ciphernote.data.CipherNoteDbHelper
import com.example.ciphernote.ui.screens.editnote.EditNoteScreen
import com.example.ciphernote.ui.screens.initial.InitialScreen
import com.example.ciphernote.ui.screens.noteslist.NotesListScreen
import com.example.ciphernote.ui.screens.noteslist.OpenNoteDialog
import com.example.ciphernote.utils.SecurityUtils
import com.example.ciphernote.utils.tea.TeaEncrypt
import com.example.ciphernote.ui.theme.CipherNoteTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Initial) }

    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var needToSort by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dbHelper = remember { CipherNoteDbHelper(context) }
    val notes = remember {
        mutableStateListOf<Note>().apply {
            addAll(dbHelper.getAll())
        }
    }

    fun formatNow(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }

    when (val screen = currentScreen) {
        is Screen.Initial -> {
            InitialScreen()
            LaunchedEffect(key1 = screen) {
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
                onAddNote = { title, password ->
                    val keyHex = SecurityUtils.md5ToHex(password)
                    val encryptedContent = TeaEncrypt.encryptString("crypt", keyHex)
                    val now = formatNow()
                    val newNote = Note(id = 0, title = title, content = encryptedContent, createdAt = now, modifiedAt = now)
                    val dbId = dbHelper.insert(newNote).toInt()
                    notes.add(0, newNote.copy(id = dbId))
                },
                onOpenNote = { note, password ->
                    needToSort = false
                    val keyHex = SecurityUtils.md5ToHex(password)
                    try {
                        val decryptedContent = TeaEncrypt.decryptString(note.content, keyHex)
                        // A simple check: if decryption fails, it might return something that doesn't look like text 
                        // or TEA might throw an error. For this implementation, we assume if it doesn't throw, it's okay.
                        // In a real app, you'd verify a MAC or similar.
                        showOpenDialog = false
                        if (decryptedContent.startsWith("crypt")) {
                            val decryptedNote = note.copy(content = decryptedContent.substring(5))
                            currentScreen = Screen.Edit(decryptedNote, password)
                        } else {
                            showErrorDialog = true
                        }
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
                    val keyHex = SecurityUtils.md5ToHex(screen.password)
                    val rawContent = "crypt" + updated.content
                    val encryptedContent = TeaEncrypt.encryptString(rawContent, keyHex)
                    val now = formatNow()
                    val newUpdated = updated.copy(content = encryptedContent, modifiedAt = now)
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
