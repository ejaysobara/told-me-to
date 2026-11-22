package com.example.someonetoldmeto.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDbHelper(context: Context, name: String? = "toldmeto.db") : SQLiteOpenHelper(context, name, null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE person (
                person_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE COLLATE NOCASE CHECK (length(name) BETWEEN 1 AND 40),
                created_at INTEGER NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE recommendation (
                rec_id INTEGER PRIMARY KEY AUTOINCREMENT,
                person_id INTEGER NOT NULL REFERENCES person(person_id) ON DELETE RESTRICT,
                title TEXT NOT NULL CHECK (length(title) BETWEEN 1 AND 100),
                type TEXT NOT NULL DEFAULT 'Other' CHECK (type IN ('Show','Song','Place','Book','Other')),
                note TEXT CHECK (note IS NULL OR length(note) <= 500),
                link TEXT CHECK (link IS NULL OR length(link) <= 500),
                status TEXT NOT NULL DEFAULT 'untried' CHECK (status IN ('untried','liked','disliked')),
                created_at INTEGER NOT NULL)"""
        )
        db.execSQL("CREATE INDEX idx_rec_person ON recommendation(person_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
