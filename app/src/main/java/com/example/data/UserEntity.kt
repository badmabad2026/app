package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val password: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVipRequested: Boolean = false,
    val vipTxId: String = "",
    val isVipApproved: Boolean = false
)
