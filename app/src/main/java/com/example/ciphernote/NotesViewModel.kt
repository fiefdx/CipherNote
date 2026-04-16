package com.example.ciphernote

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.ciphernote.data.Note

/**
 * ViewModel to hold UI navigation and dialog state across configuration changes.
 * It stores the current screen and related UI flags.
 */
class NotesViewModel : ViewModel() {
    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.Initial)
    var selectedNote: MutableState<Note?> = mutableStateOf(null)
    var showOpenDialog: MutableState<Boolean> = mutableStateOf(false)
    var showErrorDialog: MutableState<Boolean> = mutableStateOf(false)
    var needToSort: MutableState<Boolean> = mutableStateOf(false)
}
