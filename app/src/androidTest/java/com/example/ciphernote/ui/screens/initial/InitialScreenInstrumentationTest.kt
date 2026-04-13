package com.example.ciphernote.ui.screens.initial

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class InitialScreenInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialScreenDisplaysTitle() {
        composeTestRule.setContent {
            InitialScreen()
        }

        // Verify that the app title "CipherNote" is displayed
        composeTestRule.onNodeWithText("CipherNote").assertIsDisplayed()
    }

    @Test
    fun testInitialScreenDisplaysIcon() {
        composeTestRule.setContent {
            InitialScreen()
        }

        // Verify that the lock icon (represented by text "🔒") is displayed
        composeTestRule.onNodeWithText("🔒").assertIsDisplayed()
    }
}
