package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_words")
data class SavedWord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val translation: String,
    val partOfSpeech: String = "",
    val movieName: String = "",
    val sentence: String = "",
    val sentenceTranslation: String = "",
    val isLearned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
