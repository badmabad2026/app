package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedWordDao {
    @Query("SELECT * FROM saved_words ORDER BY timestamp DESC")
    fun getAllWords(): Flow<List<SavedWord>>

    @Query("SELECT * FROM saved_words ORDER BY timestamp DESC")
    suspend fun getAllWordsList(): List<SavedWord>

    @Query("SELECT * FROM saved_words WHERE word = :word LIMIT 1")
    suspend fun getWordByValue(word: String): SavedWord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: SavedWord)

    @Update
    suspend fun updateWord(word: SavedWord)

    @Query("UPDATE saved_words SET isLearned = :isLearned WHERE id = :id")
    suspend fun updateLearnedStatus(id: Int, isLearned: Boolean)

    @Query("DELETE FROM saved_words WHERE id = :id")
    suspend fun deleteWordById(id: Int)

    @Query("DELETE FROM saved_words")
    suspend fun clearAllWords()
}
