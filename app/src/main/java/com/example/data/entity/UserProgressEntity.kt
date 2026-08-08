package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val questionId: Int,
    val category: String,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val lastSelectedOption: Int = -1,
    val isBookmarked: Boolean = false,
    val isMistake: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
