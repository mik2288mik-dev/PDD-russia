package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun TicketListScreen(
    viewModel: PddViewModel,
    userProgress: List<UserProgressEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Билеты ПДД 2026-2027",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "40 официальных билетов ГИБДД по 20 вопросов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items((1..40).toList()) { ticketNum ->
                val ticketQuestions = viewModel.getQuestionsForTicket(viewModel.selectedCategory.value.code, ticketNum)
                val ticketQuestionIds = ticketQuestions.map { it.id }.toSet()
                val answered = userProgress.filter { it.questionId in ticketQuestionIds }
                val solvedCount = answered.size
                val correctCount = answered.count { it.isCorrect }
                val isCompleted = solvedCount == 20
                val isPassed = isCompleted && (correctCount >= 18)

                val cardContainerColor = when {
                    isPassed -> MaterialTheme.colorScheme.tertiaryContainer
                    isCompleted -> MaterialTheme.colorScheme.errorContainer
                    solvedCount > 0 -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                val cardContentColor = when {
                    isPassed -> MaterialTheme.colorScheme.onTertiaryContainer
                    isCompleted -> MaterialTheme.colorScheme.onErrorContainer
                    solvedCount > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                ElevatedCard(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { viewModel.startTicketQuiz(ticketNum) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = cardContainerColor,
                        contentColor = cardContentColor
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Билет",
                                style = MaterialTheme.typography.labelSmall,
                                color = cardContentColor.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$ticketNum",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = cardContentColor
                            )

                            if (solvedCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = cardContentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "$correctCount/20",
                                        color = cardContentColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


