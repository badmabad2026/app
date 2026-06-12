package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedWord::class, UserEntity::class, CustomMovieEntity::class, MovieReviewEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedWordDao(): SavedWordDao
    abstract fun memberDao(): MemberDao
    abstract fun movieReviewDao(): MovieReviewDao
}
