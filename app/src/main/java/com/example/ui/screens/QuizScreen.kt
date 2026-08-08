package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PddQuestion
import com.example.ui.components.QuestionDiagramView
import com.example.ui.theme.PddGreenCorrect
import com.example.ui.theme.PddRedWrong
import com.example.ui.viewmodel.QuizMode
import com.example.ui.viewmodel.QuizState
import com.example.ui.viewmodel.ScreenType
import com.example.ui.viewmodel.PddViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: PddViewModel,
    quizState: QuizState,
    bookmarkedIds: Set<Int>
) {
    if (quizState.questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Вопросы отсутствуют", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateTo(ScreenType.HOME) }) {
                    Text("На главную")
                }
            }
        }
        return
    }

    val currentQuestion = quizState.questions.getOrNull(quizState.currentIndex) ?: quizState.questions.first()
    val isBookmarked = bookmarkedIds.contains(currentQuestion.id)
    val selectedOptionIndex = quizState.userAnswers[currentQuestion.id]
    val isAnswered = selectedOptionIndex != null

    var showExpertComment by remember(currentQuestion.id) { mutableStateOf(false) }

    // Automatically expand comment on error
    LaunchedEffect(selectedOptionIndex) {
        if (selectedOptionIndex != null && selectedOptionIndex != currentQuestion.correctAnswerIndex) {
            showExpertComment = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = quizState.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Вопрос ${quizState.currentIndex + 1} из ${quizState.questions.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenType.HOME) }) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                },
                actions = {
                    // Timer for Exam Mode
                    if (quizState.mode == QuizMode.EXAM) {
                        val minutes = quizState.timeRemainingSeconds / 60
                        val seconds = quizState.timeRemainingSeconds % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)

                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (quizState.timeRemainingSeconds < 180) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (quizState.timeRemainingSeconds < 180) Color(0xFFDC2626) else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timeStr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (quizState.timeRemainingSeconds < 180) Color(0xFFDC2626) else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Bookmark Star
                    IconButton(onClick = { viewModel.toggleBookmark(currentQuestion.id) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Избранное",
                            tint = if (isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (quizState.isFinished) {
            QuizResultView(
                quizState = quizState,
                onRestart = { viewModel.startExamQuiz() },
                onHomeClick = { viewModel.navigateTo(ScreenType.HOME) },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Question Pagination Strip
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(quizState.questions) { index, question ->
                        val ans = quizState.userAnswers[question.id]
                        val isCurrent = index == quizState.currentIndex

                        val bgColor = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            ans != null && ans == question.correctAnswerIndex -> Color(0xFF16A34A)
                            ans != null -> Color(0xFFDC2626)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = when {
                            isCurrent || ans != null -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable { viewModel.jumpToQuestion(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Question Content Scrollable Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Topic Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentQuestion.topicTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Question Text
                    Text(
                        text = currentQuestion.questionText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 24.sp
                    )

                    // Optional Traffic Diagram View
                    QuestionDiagramView(diagramType = currentQuestion.diagramType)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options List
                    currentQuestion.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = selectedOptionIndex == optIndex
                        val isCorrectOption = optIndex == currentQuestion.correctAnswerIndex

                        val containerColor = when {
                            isAnswered && isSelected && isCorrectOption -> Color(0xFFDCFCE7)
                            isAnswered && isSelected && !isCorrectOption -> Color(0xFFFEE2E2)
                            isAnswered && isCorrectOption -> Color(0xFFDCFCE7)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val borderColor = when {
                            isAnswered && isCorrectOption -> Color(0xFF16A34A)
                            isAnswered && isSelected && !isCorrectOption -> Color(0xFFDC2626)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                                .clickable(enabled = !isAnswered) {
                                    viewModel.answerQuestion(currentQuestion.id, optIndex)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isAnswered && isCorrectOption) Color(0xFF16A34A)
                                            else if (isAnswered && isSelected) Color(0xFFDC2626)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAnswered && isCorrectOption) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    } else if (isAnswered && isSelected) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = "${optIndex + 1}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = optionText,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Expert Comment Section
                    if (isAnswered) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showExpertComment = !showExpertComment },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Комментарий эксперта ПДД",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Icon(
                                        imageVector = if (showExpertComment) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                AnimatedVisibility(visible = showExpertComment) {
                                    Column {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = currentQuestion.expertComment,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Pagination Navigation Bar
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousQuestion() },
                            enabled = quizState.currentIndex > 0,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                            Text("Назад")
                        }

                        Button(
                            onClick = { viewModel.nextQuestion() },
                            enabled = quizState.currentIndex < quizState.questions.size - 1,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Далее")
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultView(
    quizState: QuizState,
    onRestart: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = quizState.questions.size
    val errors = quizState.errorsCount
    val correct = total - errors
    val passed = quizState.isPassed

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(if (passed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (passed) Color(0xFF16A34A) else Color(0xFFDC2626),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (passed) "ЭКЗАМЕН СДАН!" else "ЭКЗАМЕН НЕ СДАН",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (passed) Color(0xFF16A34A) else Color(0xFFDC2626)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (passed) "Поздравляем! Вы отлично знаете правила ПДД!" else "Допущено ошибок: $errors. Не расстраивайтесь, повторите материал!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Правильно", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$correct", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ошибок", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$errors", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Всего", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$total", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Пройти еще раз", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Вернуться на главную")
        }
    }
}
