package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExamHistoryEntity
import com.example.data.entity.UserProgressEntity
import com.example.ui.viewmodel.PddViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    viewModel: PddViewModel,
    userProgress: List<UserProgressEntity>,
    examHistory: List<ExamHistoryEntity>
) {
    var showResetDialog by remember { mutableStateOf(false) }

    val total = 800
    val solvedCount = userProgress.count { it.isAnswered }
    val correctCount = userProgress.count { it.isAnswered && it.isCorrect }
    val wrongCount = userProgress.count { it.isAnswered && !it.isCorrect }
    val accuracyPercent = if (solvedCount > 0) ((correctCount.toFloat() / solvedCount) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Статистика и успехи",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Ваш прогресс подготовки по категории ${viewModel.selectedCategory.value.code}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Summary Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Точность", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("$accuracyPercent%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Правильно", fontSize = 12.sp, color = Color(0xFF166534))
                    Text("$correctCount", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF166534))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ошибок", fontSize = 12.sp, color = Color(0xFF991B1B))
                    Text("$wrongCount", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF991B1B))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Exam History Title
        Text(
            text = "История сдачи экзаменов",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (examHistory.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Вы пока не проходили симуляцию экзамена ГИБДД",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            examHistory.forEach { exam ->
                val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(exam.timestamp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (exam.isPassed) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (exam.isPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (exam.isPassed) "Экзамен сдан" else "Экзамен не сдан",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (exam.isPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                                Text(text = dateStr, fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Text(
                            text = "${exam.correctAnswers}/${exam.totalQuestions}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Reset Button
        OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Сбросить весь прогресс категории")
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Сброс прогресса") },
                text = { Text("Вы уверены, что хотите обнулить всю статистику и отвеченные вопросы для категории ${viewModel.selectedCategory.value.code}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetCategoryProgress()
                            showResetDialog = false
                        }
                    ) {
                        Text("Сбросить", color = Color(0xFFDC2626))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
