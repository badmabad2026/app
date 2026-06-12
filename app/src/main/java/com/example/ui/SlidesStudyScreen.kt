package com.example.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieDataset
import com.example.data.SavedWord
import com.example.viewmodel.CineLingoViewModel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SlidesStudyScreen(
    viewModel: CineLingoViewModel
) {
    val context = LocalContext.current
    val savedWords by viewModel.savedWords.collectAsState()
    val slidesIndex by viewModel.slidesIndex.collectAsState()

    // Setup TextToSpeech
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        tts.language = Locale.US
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Determine slides deck: user saved words or falling back to premium presets if deck is empty
    val slideDeck = remember(savedWords) {
        if (savedWords.isNotEmpty()) {
            savedWords
        } else {
            // Compile default movie preset words so the deck is ready to show
            val list = mutableListOf<SavedWord>()
            MovieDataset.movies.forEach { movie ->
                movie.vocabList.forEachIndexed { i, preset ->
                    list.add(
                        SavedWord(
                            id = -(100 + i), // Negative ID to denote mock status
                            word = preset.English,
                            translation = preset.Mongolian,
                            partOfSpeech = preset.pos,
                            movieName = movie.title,
                            sentence = movie.subtitles.firstOrNull()?.english ?: "",
                            sentenceTranslation = movie.subtitles.firstOrNull()?.mongolian ?: "",
                            isLearned = false
                        )
                    )
                }
            }
            list
        }
    }

    // Clamp slides index safely
    LaunchedEffect(slideDeck) {
        if (slidesIndex >= slideDeck.size) {
            viewModel.setSlidesIndex((slideDeck.size - 1).coerceAtLeast(0))
        }
    }

    // Front/Back Card flip animations state
    var isFlipped by remember { mutableStateOf(false) }
    
    // Reset flip when shifting slide cards
    LaunchedEffect(slidesIndex) {
        isFlipped = false
    }

    // Setup animated flip degree
    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper stats & titles
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ШИНЭ ҮГИЙН САН",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "PPT Танилцуулга Карт",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (savedWords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonMagenta.copy(alpha = 0.1f))
                        .border(1.dp, NeonMagenta, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Жишээ горим",
                        color = NeonMagenta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val masteredCount = savedWords.count { it.isLearned }
                Text(
                    text = "Сурсан: $masteredCount / ${savedWords.size}",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Presentation slides viewer
        if (slideDeck.isNotEmpty()) {
            val currentWord = slideDeck.getOrNull(slidesIndex) ?: slideDeck.first()

            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slide ${slidesIndex + 1} / ${slideDeck.size}",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { (slidesIndex + 1).toFloat() / slideDeck.size },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonCyan,
                    trackColor = TextMuted.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The main interactive tactile Slide presentation box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = flipRotation
                        cameraDistance = 8 * density
                    }
                    .clickable { isFlipped = !isFlipped }
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(NeonCyan, NeonMagenta, NeonCyan)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .background(NeonCardBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // If rotated pass 90 deg, flip content visually so it isn't mirrored!
                val renderBack = flipRotation > 90f

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Correct mirror text projection
                            if (renderBack) rotationY = 180f
                        },
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!renderBack) {
                        // --- FRONT SIDE ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentWord.movieName.uppercase(),
                                color = NeonYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            IconButton(
                                onClick = {
                                    if (isTtsReady) {
                                        ttsInstance?.speak(currentWord.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                },
                                modifier = Modifier
                                    .testTag("pronounce_audio_button")
                                    .size(36.dp)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Дуудлага",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Giant core english word display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentWord.word,
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "part of speech: ${currentWord.partOfSpeech.lowercase()}",
                                color = TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Context sentence area
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonDarkBg.copy(alpha = 0.5f))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "КИНОНЫ ХЭЛЛЭГТ ОРСНООР:",
                                color = TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${currentWord.sentence}\"",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        // Flip prompt
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "",
                                tint = NeonMagenta.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Эргүүлж Монгол утгыг үзэх",
                                color = TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                    } else {
                        // --- BACK SIDE ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ЯМАР УТГАТАЙ ВЭ?",
                                color = NeonMagenta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentWord.word.uppercase(),
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Giant translation body
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentWord.translation,
                                color = NeonCyan,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 38.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Монгол тайлбар хөрвүүлэлт",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }

                        // Translation of context sentence
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonDarkBg.copy(alpha = 0.5f))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "МӨР ОРЧУУЛГА:",
                                color = TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${currentWord.sentenceTranslation}\"",
                                color = TextWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Mark status controls (Only available for real user database entries)
                        if (currentWord.id >= 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.toggleSavedWordLearned(
                                            currentWord.id,
                                            currentWord.isLearned
                                        )
                                    }
                                    .background(
                                        if (currentWord.isLearned) NeonGreen.copy(alpha = 0.15f)
                                        else NeonMagenta.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        1.dp,
                                        if (currentWord.isLearned) NeonGreen else NeonMagenta,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentWord.isLearned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "",
                                    tint = if (currentWord.isLearned) NeonGreen else NeonMagenta,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentWord.isLearned) "Сурчихсан (Мастер)" else "Сурах хэрэгтэй",
                                    color = if (currentWord.isLearned) NeonGreen else NeonMagenta,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "💡 Өөрийн карт үүсгэсэн үед Сурсан төлөв удирдах боломжтой.",
                                color = TextMuted,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slides direction controllers (Өмнөх / Дараах)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.prevSlide() },
                    enabled = slidesIndex > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCardBg,
                        contentColor = Color.White,
                        disabledContainerColor = NeonCardBg.copy(alpha = 0.4f),
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (slidesIndex > 0) NeonCyan.copy(alpha = 0.4f) else TextMuted.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Өмнөх карт", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { viewModel.nextSlide(slideDeck.size) },
                    enabled = slidesIndex < slideDeck.size - 1,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCardBg,
                        contentColor = Color.White,
                        disabledContainerColor = NeonCardBg.copy(alpha = 0.4f),
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (slidesIndex < slideDeck.size - 1) NeonMagenta.copy(alpha = 0.4f) else TextMuted.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(text = "Дараах карт", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "")
                }
            }
        } else {
            // Emptystate (Not reachable fallback since presets are loaded)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Карт ачаалахад алдаа гарлаа.", color = TextGray)
            }
        }
    }
}
