package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_movies")
data class CustomMovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val titleMn: String,
    val genre: String,
    val level: String,
    val year: String,
    val accent: String,
    val durationText: String = "02:15",
    val cardColorHex: Long = 0xFFECB1FF,
    val visualPrompt: String = "",
    val creatorEmail: String,
    val vocabListJson: String,  // Serialized list of VocabPreset
    val subtitlesJson: String,  // Serialized list of SubtitleLine
    val timestamp: Long = System.currentTimeMillis()
)
