package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MovieDataset
import com.example.data.MovieRepository
import com.example.data.MovieScene
import com.example.data.SavedWord
import com.example.data.VocabPreset
import com.example.data.UserEntity
import com.example.data.CustomMovieEntity
import com.example.data.MoshiHelper
import com.example.data.SubtitleLine
import com.example.data.MovieReviewEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CineLingoViewModel(private val repository: MovieRepository) : ViewModel() {

    // Main App Navigation state
    private val _selectedTab = MutableStateFlow("movies") // "movies" | "saved" | "slides" | "quickquiz" | "members"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Logged in user state
    private val _loggedInUser = MutableStateFlow<UserEntity?>(null)
    val loggedInUser: StateFlow<UserEntity?> = _loggedInUser.asStateFlow()

    private val _registrationStatus = MutableStateFlow<String?>(null)
    val registrationStatus: StateFlow<String?> = _registrationStatus.asStateFlow()

    private val _loginStatus = MutableStateFlow<String?>(null)
    val loginStatus: StateFlow<String?> = _loginStatus.asStateFlow()

    // Combined Movies Flow
    val allMoviesFlow: StateFlow<List<MovieScene>> = repository.customMovies
        .map { customs ->
            val mappedCustoms = customs.map { c ->
                val cardColors = listOf(0xFFECB1FF, 0xFF00F1FD, 0xFF22FF66, 0xFFFFE500, 0xFFFFB4AB)
                val colorHex = cardColors[c.id % cardColors.size]
                MovieScene(
                    id = "custom_${c.id}",
                    title = c.title,
                    titleMn = c.titleMn,
                    genre = c.genre,
                    level = c.level,
                    year = c.year,
                    accent = c.accent,
                    durationText = c.durationText,
                    cardColorHex = colorHex,
                    visualPrompt = c.visualPrompt,
                    vocabList = MoshiHelper.jsonToVocabList(c.vocabListJson),
                    subtitles = MoshiHelper.jsonToSubtitleList(c.subtitlesJson)
                )
            }
            MovieDataset.movies + mappedCustoms
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MovieDataset.movies
        )

    fun registerUser(email: String, name: String, password: String) {
        viewModelScope.launch {
            _registrationStatus.value = "Бүртгэж байна..."
            if (email.isBlank() || name.isBlank() || password.isBlank()) {
                _registrationStatus.value = "Алдаа: Бүх талбарыг бөглөнө үү."
                return@launch
            }
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _registrationStatus.value = "Алдаа: Энэ и-мэйл хаягаар бүртгэл үүссэн байна."
                return@launch
            }
            val newUser = UserEntity(email = email, name = name, password = password)
            repository.registerUser(newUser)
            _loggedInUser.value = newUser
            _registrationStatus.value = "Амжилттай бүртгэгдлээ!"
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = "Шалгаж байна..."
            if (email.isBlank() || password.isBlank()) {
                _loginStatus.value = "Алдаа: И-мэйл болон нууц үгээ оруулна уу."
                return@launch
            }
            val user = repository.getUserByEmail(email)
            if (user == null || user.password != password) {
                _loginStatus.value = "Алдаа: И-мэйл эсвэл нууц үг буруу байна."
                return@launch
            }
            _loggedInUser.value = user
            _loginStatus.value = "Амжилттай нэвтэрлээ!"
        }
    }

    fun logoutUser() {
        _loggedInUser.value = null
        _loginStatus.value = null
        _registrationStatus.value = null
    }

    fun requestVip(txId: String) {
        val currentUser = _loggedInUser.value ?: return
        viewModelScope.launch {
            val updatedUser = currentUser.copy(
                isVipRequested = true,
                vipTxId = txId,
                isVipApproved = false
            )
            repository.saveUser(updatedUser)
            _loggedInUser.value = updatedUser
        }
    }

    fun approveVip(email: String, approve: Boolean) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email) ?: return@launch
            val updatedUser = user.copy(
                isVipApproved = approve,
                isVipRequested = if (approve) false else user.isVipRequested
            )
            repository.saveUser(updatedUser)
            if (_loggedInUser.value?.email == email) {
                _loggedInUser.value = updatedUser
            }
        }
    }

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addNewMovie(
        title: String,
        titleMn: String,
        genre: String,
        level: String,
        accent: String,
        year: String,
        vocabList: List<VocabPreset>,
        subtitles: List<SubtitleLine>
    ) {
        val user = _loggedInUser.value ?: return
        viewModelScope.launch {
            val customMovie = CustomMovieEntity(
                title = title,
                titleMn = titleMn,
                genre = genre,
                level = level,
                accent = accent,
                year = year,
                creatorEmail = user.email,
                vocabListJson = MoshiHelper.vocabListToJson(vocabList),
                subtitlesJson = MoshiHelper.subtitleListToJson(subtitles)
            )
            repository.insertCustomMovie(customMovie)
        }
    }

    fun deleteCustomMovie(customMovieIdStr: String) {
        // String has custom_ prefix
        val idStr = customMovieIdStr.replace("custom_", "")
        val id = idStr.toIntOrNull() ?: return
        viewModelScope.launch {
            repository.deleteCustomMovie(id)
            if (_selectedMovie.value?.id == customMovieIdStr) {
                selectMovie(null)
            }
        }
    }

    // Key settings
    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Movie study state
    private val _selectedMovie = MutableStateFlow<MovieScene?>(null)
    val selectedMovie: StateFlow<MovieScene?> = _selectedMovie.asStateFlow()

    private val _currentSubtitleIndex = MutableStateFlow(0)
    val currentSubtitleIndex: StateFlow<Int> = _currentSubtitleIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(1.0f) // 1.0f, 0.75f, 0.5f
    val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

    private val _showTranslation = MutableStateFlow(true)
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    // Active tapped word info
    private val _activeDialogWord = MutableStateFlow<String?>(null)
    val activeDialogWord: StateFlow<String?> = _activeDialogWord.asStateFlow()

    private val _activeDialogWordRefInfo = MutableStateFlow<SavedWord?>(null)
    val activeDialogWordRefInfo: StateFlow<SavedWord?> = _activeDialogWordRefInfo.asStateFlow()

    private val _aiExplanationText = MutableStateFlow<String?>(null)
    val aiExplanationText: StateFlow<String?> = _aiExplanationText.asStateFlow()

    private val _isLoadingAiExplanation = MutableStateFlow(false)
    val isLoadingAiExplanation: StateFlow<Boolean> = _isLoadingAiExplanation.asStateFlow()

    // Saved words list from Room
    val savedWords: StateFlow<List<SavedWord>> = repository.savedWords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Daily Reminder States
    private val _isReminderEnabled = MutableStateFlow(false)
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(9)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(0)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    // Load configurations from storage
    fun loadReminderSettings(context: android.content.Context) {
        val prefs = context.getSharedPreferences("cinelingo_prefs", android.content.Context.MODE_PRIVATE)
        _isReminderEnabled.value = prefs.getBoolean("reminder_enabled", false)
        _reminderHour.value = prefs.getInt("reminder_hour", 9)
        _reminderMinute.value = prefs.getInt("reminder_minute", 0)
    }

    // Update settings and schedule daily notifications
    fun updateReminderSettings(context: android.content.Context, enabled: Boolean, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences("cinelingo_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("reminder_enabled", enabled)
            putInt("reminder_hour", hour)
            putInt("reminder_minute", minute)
            apply()
        }

        _isReminderEnabled.value = enabled
        _reminderHour.value = hour
        _reminderMinute.value = minute

        if (enabled) {
            com.example.util.ReminderManager.scheduleDailyReminder(context, hour, minute)
        } else {
            com.example.util.ReminderManager.cancelReminder(context)
        }
    }

    // PPT / Slideshow active position
    private val _slidesIndex = MutableStateFlow(0)
    val slidesIndex: StateFlow<Int> = _slidesIndex.asStateFlow()

    // Quiz subgame state
    private val _quizQuestion = MutableStateFlow<QuizQuestion?>(null)
    val quizQuestion: StateFlow<QuizQuestion?> = _quizQuestion.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _quizFeedback = MutableStateFlow<String?>(null)
    val quizFeedback: StateFlow<String?> = _quizFeedback.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private var autoPlayJob: Job? = null

    init {
        // Build a dynamic quiz when we have saved words or presets
        generateNewQuestion()
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
        if (tab == "quickquiz") {
            generateNewQuestion()
        }
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    fun selectMovie(movie: MovieScene?) {
        pauseSimulation()
        _selectedMovie.value = movie
        _currentSubtitleIndex.value = 0
        _activeDialogWord.value = null
        _aiExplanationText.value = null
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pauseSimulation()
        } else {
            startSimulation()
        }
    }

    private fun startSimulation() {
        _isPlaying.value = true
        autoPlayJob?.cancel()
        autoPlayJob = viewModelScope.launch {
            val movie = _selectedMovie.value
            if (movie != null) {
                while (_isPlaying.value) {
                    val currentIndex = _currentSubtitleIndex.value
                    // base delay is 6 seconds per subtitle, adjusted by speed multiplier
                    val delayTime = (6000 / _speedMultiplier.value).toLong()
                    delay(delayTime)
                    if (currentIndex < movie.subtitles.size - 1) {
                        _currentSubtitleIndex.value = currentIndex + 1
                    } else {
                        // Loop movie simulation
                        _currentSubtitleIndex.value = 0
                    }
                }
            }
        }
    }

    private fun pauseSimulation() {
        _isPlaying.value = false
        autoPlayJob?.cancel()
        autoPlayJob = null
    }

    fun skipToSubtitle(index: Int) {
        val movie = _selectedMovie.value ?: return
        if (index in movie.subtitles.indices) {
            _currentSubtitleIndex.value = index
        }
    }

    fun nextSubtitle() {
        val movie = _selectedMovie.value ?: return
        val current = _currentSubtitleIndex.value
        if (current < movie.subtitles.size - 1) {
            _currentSubtitleIndex.value = current + 1
        }
    }

    fun prevSubtitle() {
        val current = _currentSubtitleIndex.value
        if (current > 0) {
            _currentSubtitleIndex.value = current - 1
        }
    }

    fun setSpeed(speed: Float) {
        _speedMultiplier.value = speed
        if (_isPlaying.value) {
            // Restart simulation with new speed delay
            startSimulation()
        }
    }

    fun toggleTranslation() {
        _showTranslation.value = !_showTranslation.value
    }

    private val localDictionary = mapOf(
        "you" to Pair("чи, та", "Tөлөөний үг"),
        "i" to Pair("би", "Tөлөөний үг"),
        "me" to Pair("намайг, надад", "Tөлөөний үг"),
        "my" to Pair("миний", "Tөлөөний үг"),
        "it" to Pair("энэ, тэр", "Tөлөөний үг"),
        "love" to Pair("хайр, хайрлах", "Нэр/Үйл үг"),
        "king" to Pair("хаан", "Нэр үг"),
        "queen" to Pair("хатан хаан", "Нэр үг"),
        "the" to Pair("артик", "Артик"),
        "a" to Pair("артик (нэг)", "Артик"),
        "an" to Pair("артик (нэг)", "Артик"),
        "and" to Pair("ба, бөгөөд", "Холбоос үг"),
        "to" to Pair("-руу, -рүү, чиглэлд", "Угсраа үг"),
        "in" to Pair("дотор, доор", "Угсраа үг"),
        "on" to Pair("дээр, тухай", "Угсраа үг"),
        "is" to Pair("мөн, байна", "Үйл үг"),
        "are" to Pair("мөн, байна", "Үйл үг"),
        "was" to Pair("байсан", "Үйл үг"),
        "were" to Pair("байсан", "Үйл үг"),
        "have" to Pair("байгаа, өөртөө агуулах", "Үйл үг"),
        "has" to Pair("байгаа, өөртөө агуулах", "Үйл үг"),
        "had" to Pair("байсан", "Үйл үг"),
        "for" to Pair("төлөө, хувьд", "Угсраа үг"),
        "of" to Pair("-ын, -ийн", "Угсраа үг"),
        "that" to Pair("тэр, тэгж", "Tөлөөний үг"),
        "this" to Pair("энэ", "Tөлөөний үг"),
        "with" to Pair("хамт, -тай, -той", "Угсраа үг"),
        "he" to Pair("тэр (эрэгтэй)", "Tөлөөний үг"),
        "she" to Pair("тэр (эмэгтэй)", "Tөлөөний үг"),
        "they" to Pair("тэд", "Tөлөөний үг"),
        "we" to Pair("бид", "Tөлөөний үг"),
        "world" to Pair("ертөнц, дэлхий", "Нэр үг"),
        "time" to Pair("цаг хугацаа", "Нэр үг"),
        "go" to Pair("явах, одох", "Үйл үг"),
        "do" to Pair("хийх, гүйцэтгэх", "Үйл үг"),
        "say" to Pair("хэлэх", "Үйл үг"),
        "but" to Pair("гэвч, гэхдээ", "Холбоос үг"),
        "not" to Pair("биш, үгүй", "Үгүйсгэл"),
        "no" to Pair("үгүй", "Үгүйсгэл"),
        "yes" to Pair("тийм", "Зөвшөөрөл"),
        "see" to Pair("харах", "Үйл үг"),
        "look" to Pair("харах, харах байдал", "Үйл үг"),
        "all" to Pair("бүх, бүгд", "Төлөөний үг"),
        "step" to Pair("алхам, гишгэх", "Нэр/Үйл үг"),
        "hold" to Pair("барих, атгах", "Үйл үг"),
        "close" to Pair("хаах, ойрхон", "Үйл үг/Тэмдэг нэр"),
        "right" to Pair("зөв, яг", "Тэмдэг нэр/Дайвар үг"),
        "here" to Pair("энд", "Дайвар үг"),
        "how" to Pair("яаж, хэрхэн", "Асуулт"),
        "who" to Pair("хэн", "Асуулт"),
        "get" to Pair("авах, болох", "Үйл үг"),
        "care" to Pair("санаа тавих, халамжлах", "Үйл үг/Нэр үг"),
        "dream" to Pair("зүүд, мөрөөдөл", "Нэр үг"),
        "beyond" to Pair("цаагуур, хязгаараас гадагш", "Угсраа үг"),
        "huge" to Pair("асар том", "Тэмдэг нэр"),
        "never" to Pair("хэзээ ч үгүй", "Дайвар үг"),
        "always" to Pair("үргэлж, дандаа", "Дайвар үг"),
        "again" to Pair("дахин, ахиад", "Дайвар үг")
    )

    private fun fallbackTranslate(word: String): Pair<String, String> {
        val wordLower = word.lowercase()
        return when {
            wordLower.endsWith("ing") -> Pair("${wordLower.substringBefore("ing")}-ж буй, -ж байна", "Үйл үг (Одоо цаг)")
            wordLower.endsWith("ed") -> Pair("${wordLower.substringBefore("ed")}-сан, -сэн", "Үйл үг (Өнгөрсөн цаг)")
            wordLower.endsWith("s") -> Pair("${wordLower.substringBeforeLast("s")}-ууд, -үүд", "Нэр үг (Олон тоо)")
            wordLower.endsWith("ly") -> Pair("${wordLower.substringBefore("ly")}-р, -ээр", "Дайвар үг")
            else -> Pair("'$word' (Шууд орчуулгыг ачаалахад Gemini API холболт шаардлагатай)", "Үг")
        }
    }

    // Interactive word tapping
    fun tapWord(rawWord: String, englishSentence: String, mongolianSentence: String) {
        val cleanedWord = rawWord.replace(Regex("[^a-zA-Z']"), "").lowercase().trim()
        if (cleanedWord.isEmpty()) return

        _activeDialogWord.value = cleanedWord
        _aiExplanationText.value = null

        viewModelScope.launch {
            // Check Room if word is already saved
            val existing = repository.getWordByValue(cleanedWord)
            if (existing != null) {
                _activeDialogWordRefInfo.value = existing
            } else {
                // Not saved yet, look up in active movie vocabulary presets
                val preset = _selectedMovie.value?.vocabList?.find { it.English.lowercase() == cleanedWord }
                val initialSavedWord = SavedWord(
                    word = cleanedWord,
                    translation = preset?.Mongolian ?: "Орчуулга ачаалж байна...",
                    partOfSpeech = preset?.pos ?: "Word",
                    movieName = _selectedMovie.value?.title ?: "Кино",
                    sentence = englishSentence,
                    sentenceTranslation = mongolianSentence
                )
                _activeDialogWordRefInfo.value = initialSavedWord

                if (preset == null) {
                    val localMatch = localDictionary[cleanedWord]
                    if (localMatch != null) {
                        _activeDialogWordRefInfo.value = initialSavedWord.copy(
                            translation = localMatch.first,
                            partOfSpeech = localMatch.second
                        )
                    } else {
                        val result = repository.translateSingleWordWithAI(cleanedWord, englishSentence)
                        if (_activeDialogWord.value == cleanedWord) {
                            if (result.first != "Офлайн (Тохиргоо шаардлагатай)") {
                                _activeDialogWordRefInfo.value = initialSavedWord.copy(
                                    translation = result.first,
                                    partOfSpeech = result.second
                                )
                            } else {
                                val mockedTranslation = fallbackTranslate(cleanedWord)
                                _activeDialogWordRefInfo.value = initialSavedWord.copy(
                                    translation = mockedTranslation.first,
                                    partOfSpeech = mockedTranslation.second
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun closeWordDialog() {
        _activeDialogWord.value = null
        _activeDialogWordRefInfo.value = null
        _aiExplanationText.value = null
    }

    // AI logic via Repository + Gemini Client
    fun loadAiExplanation() {
        val word = _activeDialogWord.value ?: return
        val refInfo = _activeDialogWordRefInfo.value ?: return
        
        _isLoadingAiExplanation.value = true
        _aiExplanationText.value = null

        viewModelScope.launch {
            val response = repository.explainWordWithAI(
                word = word,
                sentence = refInfo.sentence,
                movieTitle = refInfo.movieName
            )
            _aiExplanationText.value = response
            _isLoadingAiExplanation.value = false
        }
    }

    // Add vocabulary word
    fun saveWord(word: SavedWord) {
        viewModelScope.launch {
            repository.saveWord(word)
            // Refresh info reference so the UI glows green as saved!
            val updated = repository.getWordByValue(word.word)
            _activeDialogWordRefInfo.value = updated ?: word
        }
    }

    fun deleteWord(id: Int) {
        viewModelScope.launch {
            repository.deleteWord(id)
            // Reset dialog if active
            val activeWord = _activeDialogWord.value
            if (activeWord != null) {
                val existing = repository.getWordByValue(activeWord)
                if (existing == null) {
                    _activeDialogWordRefInfo.value = _activeDialogWordRefInfo.value?.copy(id = 0)
                }
            }
        }
    }

    fun toggleSavedWordLearned(id: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateWordLearned(id, !currentStatus)
        }
    }

    fun clearWordBank() {
        viewModelScope.launch {
            repository.clearWordBank()
        }
    }

    // PPT slides navigation
    fun setSlidesIndex(index: Int) {
        _slidesIndex.value = index
    }

    fun nextSlide(maxSize: Int) {
        if (_slidesIndex.value < maxSize - 1) {
            _slidesIndex.value += 1
        }
    }

    fun prevSlide() {
        if (_slidesIndex.value > 0) {
            _slidesIndex.value -= 1
        }
    }

    // Interactive Neon Quiz generation
    fun generateNewQuestion() {
        _selectedAnswerIndex.value = null
        _quizFeedback.value = null

        val availableWords = getQuizWordsCollection()
        if (availableWords.size < 2) {
            _quizQuestion.value = null
            return
        }

        // Pick 1 target word
        val target = availableWords.random()
        // Pick incorrect alternatives
        val alternatives = availableWords.filter { it.word != target.word }
            .shuffled()
            .take(3)

        val options = (alternatives.map { it.translation } + target.translation).shuffled()
        val correctIndex = options.indexOf(target.translation)

        _quizQuestion.value = QuizQuestion(
            englishWord = target.word,
            partOfSpeech = target.partOfSpeech,
            contextSentence = target.sentence,
            correctAnswerIndex = correctIndex,
            options = options,
            targetRef = target
        )
    }

    fun submitQuizAnswer(index: Int) {
        val question = _quizQuestion.value ?: return
        if (_selectedAnswerIndex.value != null) return // Already submitted

        _selectedAnswerIndex.value = index
        if (index == question.correctAnswerIndex) {
            _quizFeedback.value = "Зөв байна! Үнэхээр сайн байна. ✨"
            _quizScore.value += 10
        } else {
            val correctWord = question.options[question.correctAnswerIndex]
            _quizFeedback.value = "Буруу байна. Зөв хариулт: '$correctWord' 🛑"
        }
    }

    private fun getQuizWordsCollection(): List<QuizWordSource> {
        val list = mutableListOf<QuizWordSource>()
        // Add all user saved words from Room
        savedWords.value.forEach {
            list.add(QuizWordSource(it.word, it.translation, it.partOfSpeech, it.sentence))
        }
        // If saved list is too tiny, merge with pre-defined vocab presets from movies to make it dynamic and ready to play!
        if (list.size < 4) {
            MovieDataset.movies.forEach { movie ->
                movie.vocabList.forEach { preset ->
                    if (!list.any { it.word.lowercase() == preset.English.lowercase() }) {
                        list.add(QuizWordSource(
                            word = preset.English,
                            translation = preset.Mongolian,
                            partOfSpeech = preset.pos,
                            sentence = if (movie.subtitles.isNotEmpty()) movie.subtitles[0].english else ""
                        ))
                    }
                }
            }
        }
        return list
    }

    val allReviewsFlow: StateFlow<List<MovieReviewEntity>> = repository.allReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getReviewsForMovie(movieTitle: String): Flow<List<MovieReviewEntity>> {
        return repository.getReviewsForMovie(movieTitle)
    }

    fun submitMovieReview(movieTitle: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val authorName = _loggedInUser.value?.name ?: "Зочин"
            val authorEmail = _loggedInUser.value?.email ?: "anonymous@cinelingo.mn"
            val review = MovieReviewEntity(
                movieTitle = movieTitle,
                rating = rating,
                comment = comment,
                authorName = authorName,
                authorEmail = authorEmail
            )
            repository.insertReview(review)
        }
    }

    fun deleteMovieReview(reviewId: Int) {
        viewModelScope.launch {
            repository.deleteReviewById(reviewId)
        }
    }
}

data class QuizWordSource(
    val word: String,
    val translation: String,
    val partOfSpeech: String,
    val sentence: String
)

data class QuizQuestion(
    val englishWord: String,
    val partOfSpeech: String,
    val contextSentence: String,
    val correctAnswerIndex: Int,
    val options: List<String>,
    val targetRef: QuizWordSource
)

// Viewmodel Factory integration
class CineLingoViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CineLingoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CineLingoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
