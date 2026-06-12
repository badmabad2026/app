package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MovieRepository(
    private val savedWordDao: SavedWordDao,
    private val memberDao: MemberDao,
    private val movieReviewDao: MovieReviewDao
) {

    val savedWords: Flow<List<SavedWord>> = savedWordDao.getAllWords()
    val customMovies: Flow<List<CustomMovieEntity>> = memberDao.getAllCustomMovies()
    val allReviews: Flow<List<MovieReviewEntity>> = movieReviewDao.getAllReviews()
    val allUsersFlow: Flow<List<UserEntity>> = memberDao.getAllUsersFlow()

    fun getReviewsForMovie(movieTitle: String): Flow<List<MovieReviewEntity>> {
        return movieReviewDao.getReviewsForMovie(movieTitle)
    }

    suspend fun insertReview(review: MovieReviewEntity) = withContext(Dispatchers.IO) {
        movieReviewDao.insertReview(review)
    }

    suspend fun deleteReviewById(id: Int) = withContext(Dispatchers.IO) {
        movieReviewDao.deleteReviewById(id)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        memberDao.getUserByEmail(email)
    }

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        memberDao.insertUser(user)
    }

    suspend fun registerUser(user: UserEntity) = withContext(Dispatchers.IO) {
        memberDao.insertUser(user)
    }

    suspend fun insertCustomMovie(movie: CustomMovieEntity) = withContext(Dispatchers.IO) {
        memberDao.insertCustomMovie(movie)
    }

    suspend fun deleteCustomMovie(id: Int) = withContext(Dispatchers.IO) {
        memberDao.deleteCustomMovieById(id)
    }

    suspend fun saveWord(word: SavedWord) = withContext(Dispatchers.IO) {
        val existing = savedWordDao.getWordByValue(word.word)
        if (existing == null) {
            savedWordDao.insertWord(word)
        } else {
            savedWordDao.insertWord(
                word.copy(
                    id = existing.id,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateWord(word: SavedWord) = withContext(Dispatchers.IO) {
        savedWordDao.updateWord(word)
    }

    suspend fun deleteWord(id: Int) = withContext(Dispatchers.IO) {
        savedWordDao.deleteWordById(id)
    }

    suspend fun updateWordLearned(id: Int, isLearned: Boolean) = withContext(Dispatchers.IO) {
        savedWordDao.updateLearnedStatus(id, isLearned)
    }

    suspend fun clearWordBank() = withContext(Dispatchers.IO) {
        savedWordDao.clearAllWords()
    }

    suspend fun getWordByValue(word: String): SavedWord? = withContext(Dispatchers.IO) {
        savedWordDao.getWordByValue(word)
    }

    suspend fun translateSingleWordWithAI(word: String, sentence: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Pair("Офлайн (Тохиргоо шаардлагатай)", "Үг")
        }

        val prompt = """
            You are a translation microservice. Translate the following English word from the given context sentence into Mongolian.
            Word: "$word"
            Sentence: "$sentence"

            Respond with ONLY a clean JSON object containing the fields "translation" and "partOfSpeech". Do NOT use any Markdown block or styling around the JSON.
            Example envelope:
            {
              "translation": "хөрвүүлэг",
              "partOfSpeech": "Нэр үг"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (resultText != null) {
                val translation = extractJsonField(resultText, "translation") ?: "Орчуулга олдсонгүй"
                val partOfSpeech = extractJsonField(resultText, "partOfSpeech") ?: "Word"
                Pair(translation, partOfSpeech)
            } else {
                Pair("Орчуулах боломжгүй", "Word")
            }
        } catch (e: Exception) {
            Pair("Холболтын алдаа", "Алдаа")
        }
    }

    private fun extractJsonField(json: String, field: String): String? {
        try {
            val pattern = "\"$field\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val match = pattern.find(json)
            return match?.groupValues?.get(1)?.trim()
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun explainWordWithAI(word: String, sentence: String, movieTitle: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "**Хиймэл Оюун Ухааны Тайлбар:**\n\n⚠️ Одоогоор системд **GEMINI_API_KEY** түлхүүр тохируулаагүй байна. Та өөрийн API түлхүүрээ AI Studio-ийн 'Secrets' хэсэгт оруулан холбовол Gemini-ээр хоромхон зуурт дуудлага, дүрмийн тайлбарыг монголоор авах боломжтой болно!\n\n**Офлайн горим дахь орчуулга:**\n• Үг: *$word*\n• Монгол орчуулга: Хэллэгээс хамаарч хөрвүүлнэ.\n• Киноны хэрэглээ: '$sentence'"
        }

        val prompt = """
            Англи хэл сурч буй Монгол хүнд зориулж тайлбарлана уу.
            Үг: "$word"
            Хэрэглэгдсэн өгүүлбэр: "$sentence"
            Киноны нэр: "$movieTitle"

            Дараах бүтцээр монгол хэлээр маш тодорхой тайлбарлаж өгнө үү:
            1. 🌟 Орчуулга ба Дуудлага (Pronunciation/IPA)
            2. 📖 Энэ кинонд ямар утгаар, яагаад ийнхүү хэрэглэгдсэн бэ? (Context details)
            3. 💡 Ойролцоо утгатай үг болон хэрэглээний зөвлөмж
            4. 🚀 Тус үгийг ашигласан өөр 2 жишээ өгүүлбэр (монгол орчуулгын хамт)

            Хариултыг маш ойлгомжтой, цэгцтэй, уншихад урамтай, загварлаг (Markdown формат) хэлбэрээр бэлтгэж өгнө үү.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (resultText != null) {
                resultText
            } else {
                "Уучлаарай, хиймэл оюун ухаан хариу өгсөнгүй. Дахин оролдоно уу."
            }
        } catch (e: Exception) {
            "Алдаа гарлаа: ${e.localizedMessage}\n\nИнтернэт холболт болон API түлхүүрээ шалгана уу."
        }
    }
}
