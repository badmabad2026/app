package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_reviews")
data class MovieReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val movieTitle: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val authorName: String,
    val authorEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
