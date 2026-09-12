package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PddQuestion
import org.json.JSONArray

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val ticketNumber: Int,
    val questionNumber: Int,
    val category: String,
    val topicTitle: String,
    val questionText: String,
    val optionsJson: String,
    val correctAnswerIndex: Int,
    val expertComment: String,
    val diagramType: String? = null,
    val imageUrl: String? = null
) {
    fun toPddQuestion(): PddQuestion {
        val optionsList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(optionsJson)
            for (i in 0 until jsonArray.length()) {
                optionsList.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // fallback
        }
        return PddQuestion(
            id = id,
            ticketNumber = ticketNumber,
            questionNumber = questionNumber,
            category = category,
            topicTitle = topicTitle,
            questionText = questionText,
            options = optionsList,
            correctAnswerIndex = correctAnswerIndex,
            expertComment = expertComment,
            diagramType = diagramType,
            imageUrl = imageUrl
        )
    }
}

fun PddQuestion.toEntity(): QuestionEntity {
    val jsonArray = JSONArray(options)
    return QuestionEntity(
        id = id,
        ticketNumber = ticketNumber,
        questionNumber = questionNumber,
        category = category,
        topicTitle = topicTitle,
        questionText = questionText,
        optionsJson = jsonArray.toString(),
        correctAnswerIndex = correctAnswerIndex,
        expertComment = expertComment,
        diagramType = diagramType,
        imageUrl = imageUrl
    )
}
