package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserProgressEntity
import com.example.data.model.PddCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.PddViewModel
import com.example.ui.viewmodel.ScreenType

@Composable
fun HomeScreen(
    viewModel: PddViewModel,
    userProgress: List<UserProgressEntity>,
    mistakesCount: Int,
    bookmarksCount: Int,
    category: PddCategory
) {
    val totalQuestions = 800
    val solvedCount = userProgress.count { it.isAnswered }
    val correctCount = userProgress.count { it.isAnswered && it.isCorrect }
    val readinessPercent = if (solvedCount > 0) ((correctCount.toFloat() / totalQuestions) * 100).toInt().coerceIn(0, 100) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VibrantBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Progress Banner - Vibrant Blue Container (#D3E3FD)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = VibrantBlueContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ваш прогресс",
                        color = VibrantOnBlueContainer.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$readinessPercent%",
                        color = VibrantOnBlueContainer,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Решено: $solvedCount из $totalQuestions ($category)",
                        color = VibrantOnBlueContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Circular Progress Indicator Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = readinessPercent / 100f,
                        modifier = Modifier.fillMaxSize(),
                        color = VibrantOnBlueContainer,
                        trackColor = Color.White,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = category.code,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = VibrantOnBlueContainer
                    )
                }
            }
        }

        // Section Title
        Text(
            text = "Режимы обучения",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = VibrantOnBackground,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Main Study Modes Grid - Vibrant Pastel Containers
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VibrantModeCard(
                    title = "Билеты ПДД",
                    subtitle = "Все 40 билетов ГИБДД",
                    badge = "40",
                    icon = Icons.Default.School,
                    containerColor = VibrantPurpleContainer,
                    contentColor = VibrantOnPurpleContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenType.TICKET_LIST) }
                )
                VibrantModeCard(
                    title = "Марафон 800",
                    subtitle = "Без ошибок",
                    badge = "800",
                    icon = Icons.Default.HistoryEdu,
                    containerColor = VibrantPeachContainer,
                    contentColor = VibrantOnPeachContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.startMarathonQuiz() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VibrantModeCard(
                    title = "По темам",
                    subtitle = "20 разделов ПДД",
                    badge = "20 тем",
                    icon = Icons.Default.Category,
                    containerColor = VibrantGreenContainer,
                    contentColor = VibrantOnGreenContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenType.TOPIC_LIST) }
                )
                VibrantModeCard(
                    title = "Ошибки",
                    subtitle = if (mistakesCount > 0) "$mistakesCount на повторение" else "Нет ошибок",
                    badge = "$mistakesCount",
                    icon = Icons.Default.ErrorOutline,
                    containerColor = if (mistakesCount > 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                    contentColor = if (mistakesCount > 0) Color(0xFF991B1B) else Color(0xFF475569),
                    modifier = Modifier.weight(1f),
                    onClick = { if (mistakesCount > 0) viewModel.startMistakesQuiz() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VibrantModeCard(
                    title = "Избранное",
                    subtitle = "$bookmarksCount вопросов",
                    badge = "$bookmarksCount",
                    icon = Icons.Default.Star,
                    containerColor = VibrantAmberContainer,
                    contentColor = VibrantOnAmberContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { if (bookmarksCount > 0) viewModel.startBookmarksQuiz() }
                )
                VibrantModeCard(
                    title = "Справочник",
                    subtitle = "Правила и Знаки 2026",
                    badge = "2026",
                    icon = Icons.Default.MenuBook,
                    containerColor = VibrantBlueContainer,
                    contentColor = VibrantOnBlueContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenType.HANDBOOK) }
                )
            }
        }

        // Expert Comments & AI Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Комментарии экспертов",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VibrantOnBackground
                    )
                    TextButton(onClick = { viewModel.navigateTo(ScreenType.AI_CONSULTANT) }) {
                        Text("Спросить AI", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF7F9FC))
                        .clickable { viewModel.navigateTo(ScreenType.AI_CONSULTANT) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = Color(0xFF2563EB)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Разбор спорных билетов ПДД",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground
                        )
                        Text(
                            text = "Получите подробные разъяснения от AI эксперта...",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Dark Primary Exam Button
        Button(
            onClick = { viewModel.startExamQuiz() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VibrantDarkPrimary,
                contentColor = VibrantOnDarkPrimary
            ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(vertical = 18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ПРОДОЛЖИТЬ ТЕСТ (ЭКЗАМЕН)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun VibrantModeCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = contentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = contentColor.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

