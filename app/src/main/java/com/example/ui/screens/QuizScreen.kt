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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Вопросы отсутствуют",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { viewModel.navigateTo(ScreenType.HOME) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("На главную", style = MaterialTheme.typography.labelLarge)
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
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = quizState.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Вопрос ${quizState.currentIndex + 1} из ${quizState.questions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenType.HOME) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Timer for Exam Mode
                    if (quizState.mode == QuizMode.EXAM) {
                        val minutes = quizState.timeRemainingSeconds / 60
                        val seconds = quizState.timeRemainingSeconds % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)
                        val isUrgent = quizState.timeRemainingSeconds < 180

                        val timerBg = if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        val timerContent = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer

                        Surface(
                            shape = CircleShape,
                            color = timerBg,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = timerContent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = timerContent
                                )
                            }
                        }
                    }

                    // Bookmark Star
                    IconButton(onClick = { viewModel.toggleBookmark(currentQuestion.id) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Избранное",
                            tint = if (isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                    .background(MaterialTheme.colorScheme.background)
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
                            ans != null && ans == question.correctAnswerIndex -> MaterialTheme.colorScheme.tertiary
                            ans != null -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = when {
                            isCurrent -> MaterialTheme.colorScheme.onPrimary
                            ans != null && ans == question.correctAnswerIndex -> MaterialTheme.colorScheme.onTertiary
                            ans != null -> MaterialTheme.colorScheme.onError
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
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Question Content Scrollable Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Topic Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = currentQuestion.topicTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Question Text
                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 24.sp
                    )

                    // Traffic Diagram / Ticket Image View
                    QuestionDiagramView(
                        imageUrl = currentQuestion.imageUrl,
                        diagramType = currentQuestion.diagramType
                    )

                    // Options List with 12dp spacing
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = selectedOptionIndex == optIndex
                            val isCorrectOption = optIndex == currentQuestion.correctAnswerIndex

                            val containerColor = when {
                                isAnswered && isCorrectOption -> MaterialTheme.colorScheme.tertiaryContainer
                                isAnswered && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val borderColor = when {
                                isAnswered && isCorrectOption -> MaterialTheme.colorScheme.tertiary
                                isAnswered && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }

                            val badgeBg = when {
                                isAnswered && isCorrectOption -> MaterialTheme.colorScheme.tertiary
                                isAnswered && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val badgeTextColor = when {
                                isAnswered && isCorrectOption -> MaterialTheme.colorScheme.onTertiary
                                isAnswered && isSelected && !isCorrectOption -> MaterialTheme.colorScheme.onError
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isAnswered) {
                                        viewModel.answerQuestion(currentQuestion.id, optIndex)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isAnswered && isCorrectOption) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = badgeTextColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else if (isAnswered && isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = badgeTextColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${optIndex + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeTextColor
                                            )
                                        }
                                    }

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    // Expert Comment Section
                    if (isAnswered) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showExpertComment = !showExpertComment },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Комментарий эксперта ПДД",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    IconButton(
                                        onClick = { showExpertComment = !showExpertComment },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showExpertComment) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = showExpertComment) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            text = currentQuestion.expertComment,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Pagination Navigation Bar
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Назад", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = { viewModel.nextQuestion() },
                            enabled = quizState.currentIndex < quizState.questions.size - 1,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Далее", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
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

    val resultColor = if (passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val resultBg = if (passed) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer

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
                .size(96.dp)
                .clip(CircleShape)
                .background(resultBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = resultColor,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (passed) "ЭКЗАМЕН СДАН!" else "ЭКЗАМЕН НЕ СДАН",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = resultColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (passed) "Поздравляем! Вы отлично знаете правила ПДД!" else "Допущено ошибок: $errors. Не расстраивайтесь, повторите ошибки и попробуйте снова!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Верно", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$correct", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Ошибок", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$errors", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Всего", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Пройти еще раз", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Вернуться на главную", style = MaterialTheme.typography.labelLarge)
        }
    }
}

