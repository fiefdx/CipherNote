package com.example.ciphernote.ui.screens.editnote

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ciphernote.data.Note
import org.junit.Rule
import org.junit.Test

class EditNoteScreenInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testNote = Note(1, "Existing Title", "Existing Content", "2023-01-01", "2023-01-01")

    @Test
    fun testEditNoteDisplay() {
        composeTestRule.setContent {
            EditNoteScreen(
                note = testNote,
                password = "test_password",
                onBack = {},
                onSave = {},
                onDelete = {}
            )
        }

        // Verify that the title and content are displayed in the text fields
        composeTestRule.onNodeWithText("Existing Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Existing Content").assertIsDisplayed()
    }

    @Test
    fun testEditTitleAndContent() {
        var savedNote: Note? = null
        composeTestRule.setContent {
            EditNoteScreen(
                note = testNote,
                password = "test_password",
                onBack = {},
                onSave = { savedNote = it },
                onDelete = {}
            )
        }

        // Edit the title
        composeTestRule.onNodeWithText("Existing Title").performTextReplacement("New Title")
        // Edit the content
        composeTestRule.onNodeWithText("Existing Content").performTextReplacement("New Content")

        // Click Save (Icon with description "Save")
        composeTestRule.onNodeWithContentDescription("Save").performClick()

        // Verify that onSave was called with updated values
        assert(savedNote != null)
        assert(savedNote?.title == "New Title")
        assert(savedNote?.content == "New Content")
    }

    @Test
    fun testDeleteButtonExists() {
        composeTestRule.setContent {
            EditNoteScreen(
                note = testNote,
                password = "test_password",
                onBack = {},
                onSave = {},
                onDelete = {}
            )
        }

        // Verify the Delete button exists via its content description
        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }
}
