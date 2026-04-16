package com.example.ciphernote

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesAppInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialScreenNavigatesToNotesListAfterDelay() {
        // Freeze the clock to control time manually
        composeTestRule.mainClock.autoAdvance = false

        // Set the entire app UI
        composeTestRule.setContent {
            NotesApp(modifier = Modifier)
        }

        // Let the InitialScreen LaunchedEffect run (no delay) and animation complete
        composeTestRule.mainClock.advanceTimeBy(1L)
        composeTestRule.waitForIdle()
        // Advance a bit to let the AnimatedVisibility animation finish (default ~300ms)
        composeTestRule.mainClock.advanceTimeBy(500L)
        composeTestRule.waitForIdle()

        // Splash screen should be visible initially
        composeTestRule.onNodeWithText("CipherNote")
            .assertIsDisplayed()

        // Advance time by the 2 seconds delay defined in MainActivity
        composeTestRule.mainClock.advanceTimeBy(2000L)
        composeTestRule.waitForIdle()

        // After delay the notes list UI appears (FAB is a reliable hook)
        composeTestRule.onNodeWithContentDescription("Add Note")
            .assertIsDisplayed()
    }
}
