package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.ExamHistoryEntity
import com.example.data.entity.UserProgressEntity
import com.example.data.local.PddDatabase
import com.example.data.model.PddCategory
import com.example.data.model.PddQuestion
import com.example.data.repository.PddRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenType {
    HOME,
    TICKET_LIST,
    TOPIC_LIST,
    QUIZ,
    MISTAKES,
    BOOKMARKS,
    HANDBOOK,
    STATS,
    AI_CONSULTANT
}

enum class QuizMode(val title: String) {
    TICKET("Билет"),
    EXAM("Экзамен ГИБДД"),
    TOPIC("Тема"),
    MARATHON("Марафон"),
    MISTAKES("Работа над ошибками"),
    BOOKMARKS("Избранное")
}

data class QuizState(
    val mode: QuizMode = QuizMode.TICKET,
    val title: String = "",
    val questions: List<PddQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionId -> selectedIndex
    val isFinished: Boolean = false,
    val timeRemainingSeconds: Int = 1200, // 20 minutes countdown for Exam
    val errorsCount: Int = 0,
    val isPassed: Boolean = false,
    val extraQuestionsAdded: Int = 0
)

class PddViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PddRepository

    init {
        val dao = PddDatabase.getDatabase(application).pddDao()
        repository = PddRepository(dao)
    }

    // Active Category
    private val _selectedCategory = MutableStateFlow(PddCategory.ABM)
    val selectedCategory: StateFlow<PddCategory> = _selectedCategory.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow(ScreenType.HOME)
    val currentScreen: StateFlow<ScreenType> = _currentScreen.asStateFlow()

    // Active Quiz state
    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    // User progress from Room
    val userProgress: StateFlow<List<UserProgressEntity>> = selectedCategory.flatMapLatest { cat ->
        repository.getUserProgressList(cat.code)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val bookmarkedQuestions: StateFlow<List<PddQuestion>> = selectedCategory.flatMapLatest { cat ->
        repository.getBookmarkedQuestions(cat.code)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mistakeQuestions: StateFlow<List<PddQuestion>> = selectedCategory.flatMapLatest { cat ->
        repository.getMistakeQuestions(cat.code)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val examHistory: StateFlow<List<ExamHistoryEntity>> = selectedCategory.flatMapLatest { cat ->
        repository.getExamHistory(cat.code)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var timerJob: Job? = null

    fun selectCategory(category: PddCategory) {
        _selectedCategory.value = category
    }

    fun navigateTo(screen: ScreenType) {
        _currentScreen.value = screen
    }

    fun startTicketQuiz(ticketNumber: Int) {
        val questions = repository.getQuestionsForTicket(selectedCategory.value.code, ticketNumber)
        _quizState.value = QuizState(
            mode = QuizMode.TICKET,
            title = "Билет $ticketNumber",
            questions = questions
        )
        _currentScreen.value = ScreenType.QUIZ
    }

    fun startTopicQuiz(topicTitle: String) {
        val questions = repository.getQuestionsForTopic(selectedCategory.value.code, topicTitle)
        _quizState.value = QuizState(
            mode = QuizMode.TOPIC,
            title = topicTitle,
            questions = questions
        )
        _currentScreen.value = ScreenType.QUIZ
    }

    fun startExamQuiz() {
        val allCategoryQuestions = repository.getQuestionsForCategory(selectedCategory.value.code)
        // ГИБДД Exam: 20 random questions (1 from each topic block or random selection)
        val examQuestions = allCategoryQuestions.shuffled().take(20)
        _quizState.value = QuizState(
            mode = QuizMode.EXAM,
            title = "Экзамен ГИБДД 2026",
            questions = examQuestions,
            timeRemainingSeconds = 1200 // 20 mins
        )
        _currentScreen.value = ScreenType.QUIZ
        startTimer()
    }

    fun startMarathonQuiz() {
        val questions = repository.getQuestionsForCategory(selectedCategory.value.code)
        _quizState.value = QuizState(
            mode = QuizMode.MARATHON,
            title = "Марафон (Все ${questions.size} вопросов)",
            questions = questions
        )
        _currentScreen.value = ScreenType.QUIZ
    }

    fun startMistakesQuiz() {
        viewModelScope.launch {
            val mistakes = mistakeQuestions.value
            if (mistakes.isNotEmpty()) {
                _quizState.value = QuizState(
                    mode = QuizMode.MISTAKES,
                    title = "Работа над ошибками",
                    questions = mistakes
                )
                _currentScreen.value = ScreenType.QUIZ
            }
        }
    }

    fun startBookmarksQuiz() {
        viewModelScope.launch {
            val bookmarks = bookmarkedQuestions.value
            if (bookmarks.isNotEmpty()) {
                _quizState.value = QuizState(
                    mode = QuizMode.BOOKMARKS,
                    title = "Тренировка по избранным",
                    questions = bookmarks
                )
                _currentScreen.value = ScreenType.QUIZ
            }
        }
    }

    fun answerQuestion(questionId: Int, selectedOptionIndex: Int) {
        val state = _quizState.value
        if (state.userAnswers.containsKey(questionId) || state.isFinished) return

        val currentQuestion = state.questions.find { it.id == questionId } ?: return
        val isCorrect = selectedOptionIndex == currentQuestion.correctAnswerIndex

        val newAnswers = state.userAnswers + (questionId to selectedOptionIndex)
        var newErrors = state.errorsCount
        var extraQuestions = state.extraQuestionsAdded
        var questionsList = state.questions

        if (!isCorrect) {
            newErrors++
            // In ГИБДД exam mode: if 1 error in block, add +5 extra questions from same topic
            if (state.mode == QuizMode.EXAM && newErrors <= 2 && extraQuestions < 10) {
                val extraTopicQuestions = repository.getQuestionsForTopic(selectedCategory.value.code, currentQuestion.topicTitle)
                    .filter { q -> q.id !in questionsList.map { it.id } }
                    .shuffled()
                    .take(5)
                if (extraTopicQuestions.isNotEmpty()) {
                    questionsList = questionsList + extraTopicQuestions
                    extraQuestions += extraTopicQuestions.size
                }
            }
        }

        // Check if finished
        val allAnswered = questionsList.all { newAnswers.containsKey(it.id) }
        val isExamFailed = state.mode == QuizMode.EXAM && (newErrors > 2 || (extraQuestions > 0 && newErrors > 2))
        val isFinished = allAnswered || isExamFailed

        val passed = if (state.mode == QuizMode.EXAM) {
            !isExamFailed && allAnswered && newErrors <= 2
        } else {
            (questionsList.size - newErrors) >= (questionsList.size * 0.9)
        }

        _quizState.value = state.copy(
            questions = questionsList,
            userAnswers = newAnswers,
            errorsCount = newErrors,
            extraQuestionsAdded = extraQuestions,
            isFinished = isFinished,
            isPassed = passed
        )

        // Save to Room
        viewModelScope.launch {
            val currentBookmarked = bookmarkedQuestions.value.any { it.id == questionId }
            repository.recordAnswer(
                questionId = questionId,
                category = selectedCategory.value.code,
                selectedOption = selectedOptionIndex,
                isCorrect = isCorrect,
                isBookmarked = currentBookmarked
            )

            if (state.mode == QuizMode.MISTAKES && isCorrect) {
                repository.clearMistake(questionId)
            }

            if (isFinished && state.mode == QuizMode.EXAM) {
                stopTimer()
                val duration = 1200 - _quizState.value.timeRemainingSeconds
                repository.recordExam(
                    category = selectedCategory.value.code,
                    correctAnswers = questionsList.size - newErrors,
                    totalQuestions = questionsList.size,
                    mistakesCount = newErrors,
                    isPassed = passed,
                    durationSeconds = duration
                )
            }
        }
    }

    fun jumpToQuestion(index: Int) {
        if (index in 0 until _quizState.value.questions.size) {
            _quizState.value = _quizState.value.copy(currentIndex = index)
        }
    }

    fun nextQuestion() {
        val state = _quizState.value
        if (state.currentIndex < state.questions.size - 1) {
            _quizState.value = state.copy(currentIndex = state.currentIndex + 1)
        }
    }

    fun previousQuestion() {
        val state = _quizState.value
        if (state.currentIndex > 0) {
            _quizState.value = state.copy(currentIndex = state.currentIndex - 1)
        }
    }

    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            val isBookmarked = bookmarkedQuestions.value.any { it.id == questionId }
            repository.toggleBookmark(questionId, selectedCategory.value.code, isBookmarked)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_quizState.value.timeRemainingSeconds > 0 && !_quizState.value.isFinished) {
                delay(1000)
                val newTime = _quizState.value.timeRemainingSeconds - 1
                if (newTime <= 0) {
                    _quizState.value = _quizState.value.copy(
                        timeRemainingSeconds = 0,
                        isFinished = true,
                        isPassed = false
                    )
                    break
                } else {
                    _quizState.value = _quizState.value.copy(timeRemainingSeconds = newTime)
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resetCategoryProgress() {
        viewModelScope.launch {
            repository.resetCategoryProgress(selectedCategory.value.code)
        }
    }

    fun getTrafficSigns() = repository.getTrafficSigns()

    fun getPddRuleSections() = repository.getPddRuleSections()

    fun getQuestionsForTicket(categoryCode: String, ticketNumber: Int): List<PddQuestion> {
        return repository.getQuestionsForTicket(categoryCode, ticketNumber)
    }

    fun getQuestionsForTopic(categoryCode: String, topicTitle: String): List<PddQuestion> {
        return repository.getQuestionsForTopic(categoryCode, topicTitle)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
