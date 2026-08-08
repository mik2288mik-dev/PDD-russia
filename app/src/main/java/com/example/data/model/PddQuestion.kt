package com.example.data.model

data class PddQuestion(
    val id: Int,
    val ticketNumber: Int,      // 1 to 40
    val questionNumber: Int,    // 1 to 20
    val category: String,       // "ABM" or "CD"
    val topicTitle: String,     // e.g., "Дорожные знаки", "Проезд перекрестков"
    val questionText: String,   // Текст вопроса
    val options: List<String>,  // Варианты ответов
    val correctAnswerIndex: Int,// Индекс правильного ответа (0-indexed)
    val expertComment: String,  // Подробный комментарий эксперта
    val diagramType: String? = null // SVG/Vector diagram hint code or icon type
)
