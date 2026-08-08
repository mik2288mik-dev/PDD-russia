package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.PddViewModel

@Composable
fun TicketListScreen(
    viewModel: PddViewModel,
    userProgress: List<UserProgressEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VibrantBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Билеты ПДД 2026-2027",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = VibrantOnBackground
        )
        Text(
            text = "40 официальных билетов ГИБДД по 20 вопросов",
            fontSize = 13.sp,
            color = VibrantOnBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { viewModel.startTicketQuiz(ticketNum) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isPassed -> VibrantGreenContainer
                            isCompleted -> VibrantPeachContainer
                            solvedCount > 0 -> VibrantBlueContainer
                            else -> Color.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Билет",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = VibrantOnBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "$ticketNum",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantOnBackground
                            )

                            if (solvedCount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isPassed -> VibrantOnGreenContainer
                                                isCompleted -> VibrantOnPeachContainer
                                                else -> VibrantOnBlueContainer
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$correctCount/20",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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

