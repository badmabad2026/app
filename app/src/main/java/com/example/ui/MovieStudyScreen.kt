package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.example.data.MovieScene
import com.example.data.SavedWord
import com.example.data.MovieReviewEntity
import com.example.viewmodel.CineLingoViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovieStudyScreen(
    movie: MovieScene,
    viewModel: CineLingoViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activeIndex by viewModel.currentSubtitleIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val speedMultiplier by viewModel.speedMultiplier.collectAsState()
    
    val activeWord by viewModel.activeDialogWord.collectAsState()
    val activeWordRefInfo by viewModel.activeDialogWordRefInfo.collectAsState()
    val aiExplanationText by viewModel.aiExplanationText.collectAsState()
    val isLoadingAiExplanation by viewModel.isLoadingAiExplanation.collectAsState()

    val savedWords by viewModel.savedWords.collectAsState()
    val isWordSaved = remember(savedWords, activeWord) {
        savedWords.any { it.word.lowercase() == activeWord?.lowercase() }
    }

    var showWordSampler by remember { mutableStateOf(false) }
    var showMovieReviews by remember { mutableStateOf(false) }

    val subtitleListState = rememberLazyListState()

    // Scroll active list item into view automatically as subtitle indices change
    LaunchedEffect(activeIndex) {
        if (movie.subtitles.isNotEmpty()) {
            val targetScrollIndex = (activeIndex - 1).coerceAtLeast(0)
            subtitleListState.animateScrollToItem(targetScrollIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("movie_back_button")
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .background(NeonCardBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Буцах",
                        tint = NeonCyan
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Text(
                        text = movie.level,
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleTranslation() },
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (showTranslation) NeonMagenta.copy(alpha = 0.6f) else TextMuted,
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (showTranslation) NeonMagenta.copy(alpha = 0.1f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (showTranslation) Icons.Default.Translate else Icons.Outlined.Translate,
                        contentDescription = "Орчуулга удирдах",
                        tint = if (showTranslation) NeonMagenta else TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simulated Neon Cinema Video Player Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16/9f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(NeonCyan, NeonMagenta)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .background(Color.Black)
            ) {
                // Background artistic neon visualization matching specific movies
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Subtly animate orbits or neon grids
                            val time = System.currentTimeMillis()
                            val pulse = (Math.sin(time.toDouble() / 1000.0) + 1.0) / 2.0
                            val col = when (movie.id) {
                                "titanic" -> NeonCyan
                                "inception" -> NeonMagenta
                                "wednesday" -> NeonYellow
                                else -> NeonGreen
                            }

                            // Glowing background sphere
                            drawCircle(
                                color = col.copy(alpha = 0.08f + (pulse * 0.04f).toFloat()),
                                radius = size.width * 0.35f,
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        }
                )

                // Render dynamic visual state representing active dialogue
                if (movie.subtitles.isNotEmpty() && activeIndex in movie.subtitles.indices) {
                    val activeSubText = movie.subtitles[activeIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title or speaker indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPlaying) NeonGreen else NeonMagenta)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeSubText.speaker.uppercase(),
                                    color = NeonYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                if (isPlaying) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    WaveformVisualizer(activeColor = NeonGreen)
                                }
                            }
                            Text(
                                text = "CLIP: ${activeSubText.timeCode}",
                                color = TextGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Decorative Movie Vector Illustration Helper
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = when (movie.id) {
                                    "titanic" -> Icons.Default.DirectionsBoat
                                    "inception" -> Icons.Default.Sync
                                    "wednesday" -> Icons.Default.MusicNote
                                    else -> Icons.Default.Pets
                                },
                                contentDescription = "",
                                tint = when(movie.id) {
                                    "titanic" -> NeonCyan
                                    "inception" -> NeonMagenta
                                    "wednesday" -> NeonYellow
                                    else -> NeonGreen
                                }.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .drawBehind {
                                        if (isPlaying) {
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.1f),
                                                radius = size.width * 1.5f,
                                                style = Stroke(
                                                    width = 1.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(
                                                        floatArrayOf(10f, 10f),
                                                        0f
                                                    )
                                                )
                                            )
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeSubText.sceneDesc,
                                color = TextGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontWeight = FontWeight.Light
                            )
                        }

                        // Big prominent glowing overlay of ENGLISH text on video screen
                        Text(
                            text = activeSubText.english,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        )
                    }
                }

                // Top shadow overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .align(Alignment.TopCenter)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Draggable Timeline Seek Bar (Synchronized with Subtitles)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (movie.subtitles.isNotEmpty() && activeIndex in movie.subtitles.indices) movie.subtitles[activeIndex].timeCode else "00:00",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Slider(
                    value = activeIndex.toFloat(),
                    onValueChange = { valIndex ->
                        viewModel.skipToSubtitle(valIndex.toInt())
                    },
                    valueRange = 0f..(movie.subtitles.size - 1).coerceAtLeast(1).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("timeline_slider")
                )

                Text(
                    text = if (movie.subtitles.isNotEmpty()) movie.subtitles.last().timeCode else "00:00",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Simulated media player controls bar (Neon themed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play speed control selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCardBg)
                        .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0.5f, 0.75f, 1.0f).forEach { speed ->
                        Text(
                            text = "${speed}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (speedMultiplier == speed) NeonCyan else TextGray,
                            modifier = Modifier
                                .clickable { viewModel.setSpeed(speed) }
                                .background(if (speedMultiplier == speed) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                // Active transport player controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.prevSubtitle() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "өмнөх",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonCyan, NeonMagenta)
                                )
                            )
                            .clickable { viewModel.togglePlayPause() }
                            .padding(1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(22.dp))
                                .background(NeonDarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Зогсоох" else "Эхлүүлэх",
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.nextSubtitle() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "дараах",
                            tint = Color.White
                        )
                    }
                }

                // Timeline completion rate
                Text(
                    text = "${activeIndex + 1} / ${movie.subtitles.size}",
                    color = NeonMagenta,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(
                color = Color.White.copy(alpha = 0.05f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ХАДМАЛ ОРЧУУЛГЫН ХЭСЭГ",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showWordSampler = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(NeonCyan, NeonMagenta))),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp).testTag("extract_words_launcher_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Үг түүвэрлэх",
                            tint = NeonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Түүвэрлэх",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { showMovieReviews = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(NeonMagenta, NeonYellow))),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp).testTag("movie_reviews_launcher_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Үнэлэх",
                            tint = NeonYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Үнэлгээ",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Subtitles Study Script Feed Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = subtitleListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(movie.subtitles) { index, subtitle ->
                        val isCurrent = index == activeIndex
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.skipToSubtitle(index)
                                }
                                .border(
                                    1.dp,
                                    if (isCurrent) NeonCyan.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) NeonCardBg.copy(alpha = 0.9f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Speaker card & tag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = subtitle.speaker,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) NeonCyan else NeonYellow.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = subtitle.timeCode,
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))

                                // Interactive words selector (Breaks english text into tappable visual chips)
                                val words = subtitle.english.split(" ")
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    words.forEach { rawWord ->
                                        val cleaned = rawWord.trim().replace(Regex("[^a-zA-Z']"), "")
                                        val isTapped = cleaned.lowercase() == activeWord?.lowercase()

                                        Text(
                                            text = rawWord,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 16.sp,
                                            fontWeight = if (isTapped) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isCurrent) {
                                                if (isTapped) NeonMagenta else Color.White
                                            } else {
                                                if (isTapped) NeonMagenta else TextGray
                                            },
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isTapped) NeonMagenta.copy(alpha = 0.15f) else Color.Transparent
                                                )
                                                .clickable {
                                                    viewModel.tapWord(
                                                        cleaned,
                                                        subtitle.english,
                                                        subtitle.mongolian
                                                    )
                                                }
                                                .padding(horizontal = 2.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                // Translated sub content
                                if (showTranslation) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = subtitle.mongolian,
                                        color = if (isCurrent) TextWhite.copy(alpha = 0.9f) else TextMuted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Gradient blur on bottom feed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, NeonDarkBg)
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        }

        // Tapped Word sliding drawer layout (Bottom Drawer sheet simulation for high control)
        AnimatedVisibility(
            visible = activeWord != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (activeWord != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(
                            BorderStroke(
                                2.dp,
                                Brush.verticalGradient(
                                    listOf(NeonMagenta, Color.Transparent)
                                )
                            ),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = NeonCardBg),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .navigationBarsPadding() // Keep safe from Android system navbar overlap
                    ) {
                        // Pinch drag styling handle
                        Box(
                            modifier = Modifier
                                .size(40.dp, 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TextMuted)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Word title header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = activeWordRefInfo?.word ?: activeWord!!,
                                        color = NeonCyan,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[${activeWordRefInfo?.partOfSpeech ?: "Word"}]",
                                        color = NeonYellow,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Хөрвүүлэлт: ${activeWordRefInfo?.translation ?: "Уншиж байна..."}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Dynamic Save/Discard Toggle Button
                            Button(
                                onClick = {
                                    val wordObj = activeWordRefInfo
                                    if (wordObj != null) {
                                        if (isWordSaved) {
                                            viewModel.deleteWord(wordObj.id)
                                        } else {
                                            viewModel.saveWord(wordObj)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWordSaved) NeonGreen.copy(alpha = 0.2f) else NeonMagenta
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isWordSaved) NeonGreen else NeonMagenta
                                ),
                                modifier = Modifier.testTag("vocabulary_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isWordSaved) Icons.Filled.Check else Icons.Default.Add,
                                    contentDescription = "",
                                    tint = if (isWordSaved) NeonGreen else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isWordSaved) "Карт боллоо" else "Карт нэмэх",
                                    color = if (isWordSaved) NeonGreen else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Context sentence section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonDarkBg)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "КИНОНЫ ХЭЛЛЭГ:",
                                    fontSize = 11.sp,
                                    color = TextGray,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeWordRefInfo?.sentence ?: "",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                                if (showTranslation) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeWordRefInfo?.sentenceTranslation ?: "",
                                        color = TextGray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Interactive scrolling area for AI explanations
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (aiExplanationText == null && !isLoadingAiExplanation) {
                                // Prompt button to activate AI context search
                                Button(
                                    onClick = { viewModel.loadAiExplanation() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                Brush.linearGradient(
                                                    listOf(NeonCyan, NeonMagenta)
                                                )
                                            ),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini хиймэл оюунаар тайлбарлуулах",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Gemini AI багшийн зөвлөгөө",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NeonCyan,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    if (isLoadingAiExplanation) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = NeonCyan,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    } else {
                                        // Stylized response content block
                                        Text(
                                            text = aiExplanationText ?: "",
                                            color = TextWhite,
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NeonDarkBg.copy(alpha = 0.6f))
                                                .padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Close button for drawer
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.closeWordDialog() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextGray
                            ),
                            border = BorderStroke(1.dp, TextMuted)
                        ) {
                            Text(
                                text = "Хаах",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (showWordSampler) {
            SubtitlesWordSamplerDialog(
                movieTitle = movie.title,
                subtitles = movie.subtitles,
                presets = movie.vocabList,
                onDismissRequest = { showWordSampler = false }
            )
        }

        if (showMovieReviews) {
            MovieReviewsDialog(
                movieTitle = movie.title,
                viewModel = viewModel,
                onDismissRequest = { showMovieReviews = false }
            )
        }
    }
}

@Composable
fun WaveformVisualizer(activeColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    val height1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val height2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val height3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val height4 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(470, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        Box(modifier = Modifier.size(2.dp, height1.dp).background(activeColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.size(2.dp, height2.dp).background(activeColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.size(2.dp, height3.dp).background(activeColor, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.size(2.dp, height4.dp).background(activeColor, RoundedCornerShape(1.dp)))
    }
}

@Composable
fun SubtitlesWordSamplerDialog(
    movieTitle: String,
    subtitles: List<com.example.data.SubtitleLine>,
    presets: List<com.example.data.VocabPreset>,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Extracted clean unique words list
    val uniqueWords = remember(subtitles) {
        val wordRegex = Regex("[a-zA-Z']+")
        subtitles.flatMap { sub ->
            wordRegex.findAll(sub.english)
                .map { it.value.lowercase() }
                .filter { it == "i" || it == "a" || it.length > 1 }
        }
        .distinct()
        .sorted()
    }
    
    // Create text files for exporting/sharing
    val textToExport = remember(uniqueWords, presets) {
        StringBuilder().apply {
            append("=== CINE LINGO ENGLISH WORDS SAMPLER ===\n")
            append("ИДЭВХТЭЙ КИНО: $movieTitle\n")
            append("НИЙТ ДАВТАГДАГҮЙ ҮГСҮҮД: ${uniqueWords.size}\n\n")
            append("--- ҮГСИЙН ЖАГСААЛТ ---\n")
            uniqueWords.forEachIndexed { idx, word ->
                val preset = presets.find { it.English.lowercase() == word }
                if (preset != null) {
                    append("${idx + 1}. $word - [${preset.pos}] ${preset.Mongolian} (Монгол орчуулгатай)\n")
                } else {
                    append("${idx + 1}. $word\n")
                }
            }
            append("\n=== CineLingo App ===")
        }.toString()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, Brush.linearGradient(listOf(NeonCyan, NeonMagenta)), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        containerColor = NeonCardBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DownloadForOffline,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Хадмал орчуулгын түүвэр",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$movieTitle киноноос англи үгсийг нэгтгэв",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(NeonDarkBg.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "НИЙТ ДАВТАГДАГҮЙ ҮГС:",
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${uniqueWords.size} үг олдлоо",
                            color = NeonYellow,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "БҮРЭН ТҮҮВЭР",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = "Доорх үгсийн бүрэн түүврийг текст (.txt) хэлбэрээр татах эсвэл шууд хуулж авах боломжтой:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Scrollable container of word list preview for user
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonDarkBg)
                        .border(1.dp, TextMuted.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(uniqueWords) { idx, word ->
                            val preset = presets.find { it.English.lowercase() == word }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${idx + 1}. ",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = word,
                                        color = NeonCyan,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (preset != null) {
                                    Text(
                                        text = preset.Mongolian,
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share/Export Button
                Button(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, textToExport)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Англи үгсийн түүврийг Текст хэлбэрээр хадгалах")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("export_text_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, NeonMagenta)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = NeonMagenta,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Текст татах",
                        color = NeonMagenta,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Copy Clip Button
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(textToExport))
                        android.widget.Toast.makeText(context, "Идэвхтэй киноны бүх үгсийг санах ойд хууллаа!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("copy_words_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = NeonDarkBg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Хуулах",
                        color = NeonDarkBg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TextMuted)
            ) {
                Text(
                    text = "Буцах",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
        }
    )
}

@Composable
fun MovieReviewsDialog(
    movieTitle: String,
    viewModel: CineLingoViewModel,
    onDismissRequest: () -> Unit
) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val allReviews by viewModel.allReviewsFlow.collectAsState()
    
    // Filter reviews specifically for this movie
    val movieReviews = remember(allReviews, movieTitle) {
        allReviews.filter { it.movieTitle == movieTitle }
    }
    
    // Calculate average rating
    val avgRating = remember(movieReviews) {
        if (movieReviews.isEmpty()) 0.0 else {
            movieReviews.map { it.rating }.average()
        }
    }

    var ratingInput by remember { mutableStateOf(5) }
    var commentInput by remember { mutableStateOf("") }
    var reviewerNameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, Brush.linearGradient(listOf(NeonMagenta, NeonCyan)), RoundedCornerShape(24.dp))
            .background(NeonDarkBg, RoundedCornerShape(24.dp)),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Киноны үнэлгээ",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = movieTitle,
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Хаах",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonCardBg)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (movieReviews.isEmpty()) {
                            Text(
                                text = "Үнэлгээ байхгүй байна",
                                color = TextWhite.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Анхны сэтгэгдлийг үлдээгээрэй! 🚀",
                                color = NeonYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f", avgRating),
                                    color = NeonYellow,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = NeonYellow,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Нийт ${movieReviews.size} гишүүний үнэлгээ",
                                color = TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Write review section
                Text(
                    text = "Миний сэтгэгдэл, үнэлгээ",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Stars row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val starNumber = index + 1
                        val isActive = starNumber <= ratingInput
                        IconButton(
                            onClick = { ratingInput = starNumber },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("star_${starNumber}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$starNumber stars",
                                tint = if (isActive) NeonYellow else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        repeat(ratingInput) {
                            Text("⭐", fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Review fields
                if (loggedInUser == null) {
                    // Anonymous review requires name
                    OutlinedTextField(
                        value = reviewerNameInput,
                        onValueChange = { reviewerNameInput = it },
                        label = { Text("Таны нэр (Зочин)", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = TextGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reviewer_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonCyan.copy(alpha = 0.05f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User info",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Гишүүн: ${loggedInUser?.name} (${loggedInUser?.email})",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    label = { Text("Сэтгэгдэл, үнэлгээний тайлбар...", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMagenta,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = NeonMagenta,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("review_comment_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val finalAuthorName = if (loggedInUser != null) {
                            loggedInUser!!.name
                        } else if (reviewerNameInput.isNotBlank()) {
                            reviewerNameInput
                        } else {
                            "Аноним"
                        }
                        
                        viewModel.submitMovieReview(
                            movieTitle = movieTitle,
                            rating = ratingInput,
                            comment = commentInput.ifBlank { "Үнэлгээ өглөө!" }
                        )
                        // Reset
                        commentInput = ""
                        reviewerNameInput = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("submit_review_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Сэтгэгдэл, үнэлгээ үлдээх",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comments feed list
                Text(
                    text = "Гишүүдийн сэтгэгдэлүүд (${movieReviews.size})",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(4.dp)
                ) {
                    if (movieReviews.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Хархан сэтгэгдэл үлдээгээгүй байна.",
                                color = TextGray.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            itemsIndexed(movieReviews) { _, review ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = NeonCardBg.copy(alpha = 0.8f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = review.authorName,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                repeat(review.rating) {
                                                    Text("⭐", fontSize = 10.sp)
                                                }
                                                // Optional Delete button
                                                val canDelete = loggedInUser == null || 
                                                                loggedInUser?.email == review.authorEmail || 
                                                                review.authorEmail == "anonymous@cinelingo.mn"
                                                if (canDelete) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    IconButton(
                                                        onClick = { viewModel.deleteMovieReview(review.id) },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Устгах",
                                                            tint = Color(0xFFFF5252),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = review.comment,
                                            color = TextGray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
