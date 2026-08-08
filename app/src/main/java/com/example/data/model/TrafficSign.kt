package com.example.data.model

data class TrafficSign(
    val code: String,        // e.g. "1.1", "2.1", "3.27"
    val title: String,       // Название знака
    val group: String,       // "Предупреждающие", "Знаки приоритета", "Запрещающие", etc.
    val description: String, // Описание действия знака
    val iconType: String     // For visual representation
)

data class PddRuleSection(
    val sectionNumber: Int,
    val title: String,
    val content: String,
    val summary: String
)
