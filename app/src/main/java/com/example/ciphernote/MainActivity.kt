package com.example.ciphernote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ciphernote.ui.screens.initial.InitialScreen
import com.example.ciphernote.ui.screens.noteslist.NotesListScreen
import com.example.ciphernote.ui.theme.CipherNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CipherNoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    InitialScreen()
                    NotesListScreen()
                }
            }
        }
    }
}