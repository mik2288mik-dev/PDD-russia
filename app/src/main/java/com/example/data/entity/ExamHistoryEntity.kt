package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_history")
data class ExamHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val correctAnswers: Int,
    val totalQuestions: Int,
    val mistakesCount: Int,
    val isPassed: Boolean,
    val durationSeconds: Int
)
