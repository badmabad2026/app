package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    // --- USER MANAGEMENT ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY timestamp DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    // --- CUSTOM CINEMA SLIDES ---
    @Query("SELECT * FROM custom_movies ORDER BY timestamp DESC")
    fun getAllCustomMovies(): Flow<List<CustomMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomMovie(movie: CustomMovieEntity)

    @Query("DELETE FROM custom_movies WHERE id = :id")
    suspend fun deleteCustomMovieById(id: Int)
}
