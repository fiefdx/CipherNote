package com.example.ciphernote.ui.screens.noteslist

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ciphernote.data.Note
import org.junit.Rule
import org.junit.Test

class NotesListScreenInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testNotes = listOf(
        Note(1, "First Note", "Content 1", "2023-01-01", "2023-01-01"),
        Note(2, "Second Note", "Content 2", "2023-01-02", "2023-01-02")
    )

    @Test
    fun testNotesListDisplay() {
        composeTestRule.setContent {
            NotesListScreen(
                modifier = androidx.compose.ui.Modifier,
                notes = testNotes,
                onNoteClick = {},
                onAddNote = { _, _ -> },
                onOpenNote = { _, _ -> }
            )
        }

        // Verify that note titles are displayed in the list
        composeTestRule.onNodeWithText("First Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Note").assertIsDisplayed()
    }

    @Test
    fun testSearchFunctionality() {
        var searchQuery = ""
        composeTestRule.setContent {
            // We need to wrap it in a way that we can observe the search query if needed, 
            // but for this UI test, we just interact with the TextField.
            NotesListScreen(
                modifier = androidx.compose.ui.Modifier,
                notes = testNotes,
                onNoteClick = {},
                onAddNote = { _, _ -> },
                onOpenNote = { _, _ -> }
            )
        }

        // Find the search text field by its placeholder
        val searchField = composeTestRule.onNodeWithText("Search your notes...")
        searchField.performTextInput("First")

        // Verify that only "First Note" is displayed (Second Note should be filtered out)
        composeTestRule.onNodeWithText("First Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Note").assertDoesNotExist()
    }

    @Test
    fun testAddNoteButtonExists() {
        composeTestRule.setContent {
            NotesListScreen(
                modifier = androidx.compose.ui.Modifier,
                notes = emptyList(),
                onNoteClick = {},
                onAddNote = { _, _ -> },
                onOpenNote = { _, _ -> }
            )
        }

        // Verify the Floating Action Button with "Add Note" content description exists
        composeTestRule.onNodeWithContentDescription("Add Note").assertIsDisplayed()
    }
}
