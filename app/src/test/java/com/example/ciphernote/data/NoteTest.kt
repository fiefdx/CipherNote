package com.example.ciphernote.data

import org.junit.Assert.*
import org.junit.Test

class NoteTest {

    @Test
    fun testNotePropertyIntegrity() {
        val id = 1
        val title = "My Secret Note"
        val content = "This is the note content."
        val createdAt = "2023-10-27T10:00:00Z"
        val modifiedAt = "2023-10-27T11:00:00Z"

        val note = Note(id, title, content, createdAt, modifiedAt)

        assertEquals(id, note.id)
        assertEquals(title, note.title)
        assertEquals(content, note.content)
        assertEquals(createdAt, note.createdAt)
        assertEquals(modifiedAt, note.modifiedAt)
    }

    @Test
    fun testNoteDefaultContent() {
        val id = 2
        val title = "Title Only"
        val createdAt = "2023-10-27T10:00:00Z"
        val modifiedAt = "2023-10-27T10:00:00Z"

        val note = Note(id, title, "", createdAt, modifiedAt) // Explicitly passing empty string to match default behavior if needed or just testing it.
        // Actually the data class has a default value for content. Let's test that.
        val noteWithDefault = Note(id, title, createdAt = createdAt, modifiedAt = modifiedAt)

        assertEquals("", noteWithDefault.content)
    }
}
