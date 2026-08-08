package com.example.data.repository

import com.example.data.entity.ExamHistoryEntity
import com.example.data.entity.UserProgressEntity
import com.example.data.local.PddDao
import com.example.data.local.PddDataProvider
import com.example.data.model.PddQuestion
import com.example.data.model.PddRuleSection
import com.example.data.model.TrafficSign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PddRepository(private val dao: PddDao) {

    fun getQuestionsForCategory(category: String): List<PddQuestion> {
        return PddDataProvider.getQuestions(category)
    }

    fun getQuestionsForTicket(category: String, ticketNumber: Int): List<PddQuestion> {
        return getQuestionsForCategory(category).filter { it.ticketNumber == ticketNumber }
    }

    fun getQuestionsForTopic(category: String, topicTitle: String): List<PddQuestion> {
        return getQuestionsForCategory(category).filter { it.topicTitle == topicTitle }
    }

    fun getQuestionById(category: String, questionId: Int): PddQuestion? {
        return getQuestionsForCategory(category).find { it.id == questionId }
    }

    fun getUserProgressList(category: String): Flow<List<UserProgressEntity>> {
        return dao.getUserProgressList(category)
    }

    fun getBookmarkedQuestions(category: String): Flow<List<PddQuestion>> {
        val allQuestions = getQuestionsForCategory(category)
        return dao.getBookmarks(category).map { progressList ->
            val bookmarkedIds = progressList.map { it.questionId }.toSet()
            allQuestions.filter { it.id in bookmarkedIds }
        }
    }

    fun getMistakeQuestions(category: String): Flow<List<PddQuestion>> {
        val allQuestions = getQuestionsForCategory(category)
        return dao.getMistakes(category).map { progressList ->
            val mistakeIds = progressList.map { it.questionId }.toSet()
            allQuestions.filter { it.id in mistakeIds }
        }
    }

    suspend fun recordAnswer(
        questionId: Int,
        category: String,
        selectedOption: Int,
        isCorrect: Boolean,
        isBookmarked: Boolean
    ) {
        val currentProgress = dao.getProgressForQuestion(questionId)
        val updated = UserProgressEntity(
            questionId = questionId,
            category = category,
            isAnswered = true,
            isCorrect = isCorrect,
            lastSelectedOption = selectedOption,
            isBookmarked = isBookmarked || (currentProgress?.isBookmarked ?: false),
            isMistake = !isCorrect,
            updatedAt = System.currentTimeMillis()
        )
        dao.upsertUserProgress(updated)
    }

    suspend fun toggleBookmark(questionId: Int, category: String, currentBookmarked: Boolean) {
        val existing = dao.getProgressForQuestion(questionId)
        val newStatus = !currentBookmarked
        if (existing == null) {
            dao.upsertUserProgress(
                UserProgressEntity(
                    questionId = questionId,
                    category = category,
                    isBookmarked = newStatus
                )
            )
        } else {
            dao.setBookmarked(questionId, newStatus)
        }
    }

    suspend fun clearMistake(questionId: Int) {
        dao.clearMistake(questionId)
    }

    suspend fun recordExam(
        category: String,
        correctAnswers: Int,
        totalQuestions: Int,
        mistakesCount: Int,
        isPassed: Boolean,
        durationSeconds: Int
    ) {
        dao.insertExamResult(
            ExamHistoryEntity(
                category = category,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                mistakesCount = mistakesCount,
                isPassed = isPassed,
                durationSeconds = durationSeconds
            )
        )
    }

    fun getExamHistory(category: String): Flow<List<ExamHistoryEntity>> {
        return dao.getExamHistory(category)
    }

    suspend fun resetCategoryProgress(category: String) {
        dao.resetCategoryProgress(category)
    }

    fun getTrafficSigns(): List<TrafficSign> = PddDataProvider.getTrafficSigns()

    fun getPddRuleSections(): List<PddRuleSection> = PddDataProvider.getPddRuleSections()
}
