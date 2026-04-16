package com.example.ciphernote.ui.screens.editnote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.ciphernote.data.Note

/**
 * ViewModel that survives configuration changes and holds the editing state for a note.
 * It uses [SavedStateHandle] so that the state can also survive process recreation.
 */
class EditNoteViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var title: String by mutableStateOf("")
        private set
    var content: String by mutableStateOf("")
        private set

    /** Initialise with the provided note only if the fields are empty. */
    fun init(note: Note) {
        if (title.isEmpty() && content.isEmpty()) {
            title = note.title
            content = note.content
            savedStateHandle["title"] = title
            savedStateHandle["content"] = content
        }
    }

    fun updateTitle(newTitle: String) {
        title = newTitle
        savedStateHandle["title"] = newTitle
    }

    fun updateContent(newContent: String) {
        content = newContent
        savedStateHandle["content"] = newContent
    }
}
