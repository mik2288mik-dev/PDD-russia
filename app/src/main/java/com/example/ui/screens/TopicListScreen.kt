package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserProgressEntity
import com.example.ui.viewmodel.PddViewModel

@Composable
fun TopicListScreen(
    viewModel: PddViewModel,
    userProgress: List<UserProgressEntity>
) {
    val topics = listOf(
        "Общие положения",
        "Обязанности водителей",
        "Применение спецсигналов",
        "Обязанности пешеходов",
        "Сигналы светофора и регулировщика",
        "Аварийная сигнализация",
        "Начало движения, маневрирование",
        "Расположение ТС на проезжей части",
        "Скорость движения",
        "Обгон, опережение, встречный разъезд",
        "Остановка и стоянка",
        "Проезд перекрестков",
        "Пешеходные переходы и остановки",
        "Движение через ж/д пути",
        "Движение по автомагистрали",
        "Жилые зоны и приоритет маршрутных ТС",
        "Пользование внешними световыми приборами",
        "Буксировка и учебная езда",
        "Перевозка людей и грузов",
        "Первая помощь и ответственность"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Темы ПДД 2026-2027",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Изучайте вопросы, сгруппированные по разделам ПДД",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(topics) { topic ->
                val questions = viewModel.getQuestionsForTopic(viewModel.selectedCategory.value.code, topic)
                val questionIds = questions.map { it.id }.toSet()
                val answered = userProgress.filter { it.questionId in questionIds }
                val solvedCount = answered.size
                val total = questions.size

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startTopicQuiz(topic) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = if (total > 0) solvedCount.toFloat() / total else 0f,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$solvedCount из $total",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
