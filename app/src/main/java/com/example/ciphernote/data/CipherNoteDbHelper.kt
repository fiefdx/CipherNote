package com.example.ciphernote.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

private const val DATABASE_NAME = "CipherNote.db"
private const val DATABASE_VERSION = 1
private const val TABLE_NOTES = "notes"
private const val COLUMN_ID = "id"
private const val COLUMN_TITLE = "title"
private const val COLUMN_CREATED_AT = "created_at"
private const val COLUMN_CONTENT = "content"

class CipherNoteDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_NOTES (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_TITLE TEXT NOT NULL, " +
                    "$COLUMN_CONTENT TEXT NOT NULL DEFAULT '', " +
                    "$COLUMN_CREATED_AT TEXT NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // For now drop and recreate
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    /** Insert a note and return the generated id */
    fun insert(note: Note): Long {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_CREATED_AT, note.createdAt)
        }
        writableDatabase.insert(TABLE_NOTES, null, values).also { return it }
    }

    /** Retrieve all notes */
    fun getAll(): List<Note> {
        val list = mutableListOf<Note>()
        val cursor = readableDatabase.query(
            TABLE_NOTES,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_CREATED_AT),
            null, null, null, null,
            "$COLUMN_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT))
                val createdAt = it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                list.add(Note(id, title, content, createdAt))
            }
        }
        return list
    }

    /** Update a note */
    fun update(note: Note): Int {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_CREATED_AT, note.createdAt)
        }
        return writableDatabase.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(note.id.toString())
        )
    }

    /** Delete a note by id */
    fun delete(id: Int): Int {
        return writableDatabase.delete(TABLE_NOTES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}
