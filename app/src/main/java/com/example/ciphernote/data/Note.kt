package com.example.ciphernote.data

data class Note(
    val id: Int,
    val title: String,
    val content: String = "",
    val createdAt: String,
    val modifiedAt: String,
)
