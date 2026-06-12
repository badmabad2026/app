package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieReviewDao {
    @Query("SELECT * FROM movie_reviews WHERE movieTitle = :movieTitle ORDER BY timestamp DESC")
    fun getReviewsForMovie(movieTitle: String): Flow<List<MovieReviewEntity>>

    @Query("SELECT * FROM movie_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<MovieReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: MovieReviewEntity)

    @Query("DELETE FROM movie_reviews WHERE id = :id")
    suspend fun deleteReviewById(id: Int)
}
