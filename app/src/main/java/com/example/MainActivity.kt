package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ExamHistoryEntity
import com.example.data.entity.UserProgressEntity
import com.example.data.model.PddCategory
import com.example.data.model.PddQuestion
import com.example.ui.components.PddBottomNav
import com.example.ui.components.PddTopBar
import com.example.ui.screens.*
import com.example.ui.theme.PddAppTheme
import com.example.ui.viewmodel.PddViewModel
import com.example.ui.viewmodel.QuizState
import com.example.ui.viewmodel.ScreenType

class MainActivity : ComponentActivity() {

    private val viewModel: PddViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PddAppTheme {
                PddMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PddMainApp(viewModel: PddViewModel) {
    val currentCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsStateWithLifecycle()
    val mistakeQuestions by viewModel.mistakeQuestions.collectAsStateWithLifecycle()
    val examHistory by viewModel.examHistory.collectAsStateWithLifecycle()

    val bookmarkedIds = bookmarkedQuestions.map { it.id }.toSet()

    // Custom Back Handling
    BackHandler(enabled = currentScreen != ScreenType.HOME) {
        viewModel.navigateTo(ScreenType.HOME)
    }

    val screenTitle = when (currentScreen) {
        ScreenType.HOME -> "ПДД Россия 2026-2027"
        ScreenType.TICKET_LIST -> "Выбор билета"
        ScreenType.TOPIC_LIST -> "Вопросы по темам"
        ScreenType.QUIZ -> quizState.title
        ScreenType.MISTAKES -> "Работа над ошибками"
        ScreenType.BOOKMARKS -> "Избранные вопросы"
        ScreenType.HANDBOOK -> "Справочник ПДД"
        ScreenType.STATS -> "Статистика успеваемости"
        ScreenType.AI_CONSULTANT -> "AI Консультант ПДД"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen != ScreenType.QUIZ) {
                PddTopBar(
                    title = screenTitle,
                    currentCategory = currentCategory,
                    showBackButton = currentScreen != ScreenType.HOME,
                    onBackClick = { viewModel.navigateTo(ScreenType.HOME) },
                    onCategorySelect = { viewModel.selectCategory(it) }
                )
            }
        },
        bottomBar = {
            PddBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                ScreenType.HOME -> HomeScreen(
                    viewModel = viewModel,
                    userProgress = userProgress,
                    mistakesCount = mistakeQuestions.size,
                    bookmarksCount = bookmarkedQuestions.size,
                    category = currentCategory
                )
                ScreenType.TICKET_LIST -> TicketListScreen(
                    viewModel = viewModel,
                    userProgress = userProgress
                )
                ScreenType.TOPIC_LIST -> TopicListScreen(
                    viewModel = viewModel,
                    userProgress = userProgress
                )
                ScreenType.QUIZ -> QuizScreen(
                    viewModel = viewModel,
                    quizState = quizState,
                    bookmarkedIds = bookmarkedIds
                )
                ScreenType.HANDBOOK -> HandbookScreen(viewModel = viewModel)
                ScreenType.STATS -> StatsScreen(
                    viewModel = viewModel,
                    userProgress = userProgress,
                    examHistory = examHistory
                )
                ScreenType.AI_CONSULTANT -> AiConsultantScreen(viewModel = viewModel)
                else -> HomeScreen(
                    viewModel = viewModel,
                    userProgress = userProgress,
                    mistakesCount = mistakeQuestions.size,
                    bookmarksCount = bookmarkedQuestions.size,
                    category = currentCategory
                )
            }
        }
    }
}
