package com.example.ciphernote.ui.screens.noteslist

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ciphernote.data.Note
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesListScreenAddNoteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAddNoteFlow() {
        var addedTitle: String? = null
        var addedPassword: String? = null
        val notes = mutableStateListOf<Note>()

        composeTestRule.setContent {
            NotesListScreen(
                modifier = Modifier,
                notes = notes,
                onNoteClick = { /* noop */ },
                onAddNote = { title, password ->
                    addedTitle = title
                    addedPassword = password
                    // Simulate the app adding the new note to the list
                    notes.add(Note(id = notes.size + 1, title = title, content = "", createdAt = "", modifiedAt = ""))
                },
                onOpenNote = { _, _ -> /* noop */ }
            )
        }

        // Open the create dialog via FAB
        composeTestRule.onNodeWithContentDescription("Add Note")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify dialog is displayed
        composeTestRule.onNodeWithText("New Note")
            .assertIsDisplayed()

        // Fill title and password fields
        composeTestRule.onNodeWithText("Title")
            .performTextInput("Test Note")
        composeTestRule.onNodeWithText("Password")
            .performTextInput("1234")

        // Click the Create button
        composeTestRule.onNodeWithText("Create")
            .performClick()

        // Verify callback captured correct values
        assertEquals("Test Note", addedTitle)
        assertEquals("1234", addedPassword)

        // Verify the new note appears in the list
        composeTestRule.onNodeWithText("Test Note")
            .assertIsDisplayed()
    }
}
