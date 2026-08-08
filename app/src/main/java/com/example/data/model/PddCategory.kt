package com.example.data.model

enum class PddCategory(val code: String, val title: String, val description: String) {
    ABM("ABM", "Категории A, B, M", "Легковые автомобили, мотоциклы и мопеды (A, B, M, A1, B1)"),
    CD("CD", "Категории C, D", "Грузовые автомобили и автобусы (C, D, C1, D1)")
}
