package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ExamHistoryEntity
import com.example.data.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PddDao {

    @Query("SELECT * FROM user_progress WHERE category = :category")
    fun getUserProgressList(category: String): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE questionId = :questionId")
    suspend fun getProgressForQuestion(questionId: Int): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProgress(progress: UserProgressEntity)

    @Query("UPDATE user_progress SET isBookmarked = :isBookmarked WHERE questionId = :questionId")
    suspend fun setBookmarked(questionId: Int, isBookmarked: Boolean)

    @Query("SELECT * FROM user_progress WHERE isBookmarked = 1 AND category = :category")
    fun getBookmarks(category: String): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE isMistake = 1 AND category = :category")
    fun getMistakes(category: String): Flow<List<UserProgressEntity>>

    @Query("UPDATE user_progress SET isMistake = 0 WHERE questionId = :questionId")
    suspend fun clearMistake(questionId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(exam: ExamHistoryEntity)

    @Query("SELECT * FROM exam_history WHERE category = :category ORDER BY timestamp DESC")
    fun getExamHistory(category: String): Flow<List<ExamHistoryEntity>>

    @Query("DELETE FROM user_progress WHERE category = :category")
    suspend fun resetCategoryProgress(category: String)
}
