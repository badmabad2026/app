package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.util.Locale
import com.example.data.MovieDataset
import com.example.data.MovieScene
import com.example.data.SavedWord
import com.example.R
import com.example.viewmodel.CineLingoViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CineLingoViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val savedWords by viewModel.savedWords.collectAsState()
    val selectedMovie by viewModel.selectedMovie.collectAsState()

    var showSettingsDrawer by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    val customApiKey by viewModel.customApiKey.collectAsState()

    LaunchedEffect(customApiKey) {
        apiKeyInput = customApiKey
    }

    // Outer layout
    Scaffold(
        topBar = {
            if (selectedMovie == null) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = "",
                                tint = NeonCyan,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CineLingo",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showSettingsDrawer = !showSettingsDrawer }
                        ) {
                            Icon(
                                imageVector = if (customApiKey.isNotEmpty()) Icons.Default.SettingsSuggest else Icons.Default.Settings,
                                contentDescription = "Тохиргоо",
                                tint = if (customApiKey.isNotEmpty()) NeonGreen else Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = NeonDarkBg
                    ),
                    modifier = Modifier.border(0.dp, Color.Transparent)
                )
            }
        },
        bottomBar = {
            if (selectedMovie == null) {
                NavigationBar(
                    containerColor = NeonCardBg,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                ) {
                    NavigationBarItem(
                        selected = selectedTab == "movies",
                        onClick = { viewModel.selectTab("movies") },
                        label = { Text("Нүүр хуудас", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Movie, contentDescription = "Муви") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedTextColor = TextGray,
                            unselectedIconColor = TextGray,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_movies")
                    )

                    NavigationBarItem(
                        selected = selectedTab == "saved",
                        onClick = { viewModel.selectTab("saved") },
                        label = { Text("Үгийн сан", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = { 
                            BadgedBox(
                                badge = {
                                    if (savedWords.isNotEmpty()) {
                                        Badge(
                                            containerColor = NeonMagenta,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = savedWords.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "Хавтас")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonMagenta,
                            selectedTextColor = NeonMagenta,
                            unselectedTextColor = TextGray,
                            unselectedIconColor = TextGray,
                            indicatorColor = NeonMagenta.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_saved")
                    )

                    NavigationBarItem(
                        selected = selectedTab == "slides",
                        onClick = { viewModel.selectTab("slides") },
                        label = { Text("PPT Карт", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Tv, contentDescription = "Слайд") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonYellow,
                            selectedTextColor = NeonYellow,
                            unselectedTextColor = TextGray,
                            unselectedIconColor = TextGray,
                            indicatorColor = NeonYellow.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_slides")
                    )

                    NavigationBarItem(
                        selected = selectedTab == "quickquiz",
                        onClick = { viewModel.selectTab("quickquiz") },
                        label = { Text("Асуулт", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Extension, contentDescription = "Даалгавар") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonGreen,
                            selectedTextColor = NeonGreen,
                            unselectedTextColor = TextGray,
                            unselectedIconColor = TextGray,
                            indicatorColor = NeonGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_quiz")
                    )

                    NavigationBarItem(
                        selected = selectedTab == "members",
                        onClick = { viewModel.selectTab("members") },
                        label = { Text("Гишүүд", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.People, contentDescription = "Гишүүд") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonMagenta,
                            selectedTextColor = NeonMagenta,
                            unselectedTextColor = TextGray,
                            unselectedIconColor = TextGray,
                            indicatorColor = NeonMagenta.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_members")
                    )
                }
            }
        },
        containerColor = NeonDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeonDarkBg)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonMagenta.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(0f, 0f),
                        radius = 800f
                    )
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(1000f, 1500f),
                        radius = 1200f
                    )
                )
                .padding(innerPadding)
        ) {
            // Render either the active movie study session OR standard tab screens
            val activeMovie = selectedMovie
            if (activeMovie != null) {
                MovieStudyScreen(
                    movie = activeMovie,
                    viewModel = viewModel,
                    onBack = { viewModel.selectMovie(null) }
                )
            } else {
                when (selectedTab) {
                    "movies" -> MoviesTabScreen(viewModel)
                    "saved" -> SavedWordsTabScreen(viewModel)
                    "slides" -> SlidesStudyScreen(viewModel)
                    "quickquiz" -> QuickQuizTabScreen(viewModel)
                    "members" -> MemberHubScreen(viewModel)
                }
            }

            // Custom sliding API config settings bottom bar sheet
            if (showSettingsDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f))
                        .clickable { showSettingsDrawer = false }
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                NeonCyan.copy(alpha = 0.5f),
                                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .clickable(enabled = false) {},
                        colors = CardDefaults.cardColors(containerColor = NeonCardBg),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .navigationBarsPadding()
                        ) {
                            Text(
                                text = "АПП-ЫН ТОХИРГОО",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gemini API Түлхүүр холбох",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Киноны үг хэллэгийг нэвтрүүлж, ахисан түвшний тайлбарыг AI багшаас монгол хэл дээр авахын тулд өөрийн GEMINI_API_KEY-ийг тохируулж болно эсвэл хоосон орхиж Офлайн тайлбарыг сонгоно уу.",
                                color = TextGray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("api_key_input"),
                                placeholder = { Text("Үүнд API түлхүүрээ оруулна уу...", color = TextMuted) },
                                singleLine = true,
                                trailingIcon = {
                                    if (apiKeyInput.isNotEmpty()) {
                                        IconButton(onClick = { apiKeyInput = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "",
                                                tint = TextGray
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = TextMuted,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = NeonDarkBg.copy(alpha = 0.6f),
                                    unfocusedContainerColor = NeonDarkBg.copy(alpha = 0.3f)
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row {
                                OutlinedButton(
                                    onClick = { 
                                        viewModel.setCustomApiKey("")
                                        apiKeyInput = ""
                                        showSettingsDrawer = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = NeonMagenta
                                    ),
                                    border = BorderStroke(1.dp, NeonMagenta),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Түлхүүр устгах")
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = {
                                        viewModel.setCustomApiKey(apiKeyInput)
                                        showSettingsDrawer = false
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(listOf(NeonCyan, NeonMagenta)),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Хадгалах", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoviesTabScreen(viewModel: CineLingoViewModel) {
    val savedWords by viewModel.savedWords.collectAsState()
    val movies by viewModel.allMoviesFlow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("Бүх") }

    val filteredMovies = remember(movies, searchQuery, selectedLevel) {
        movies.filter { movie ->
            val matchesQuery = movie.title.contains(searchQuery, ignoreCase = true) ||
                    movie.titleMn.contains(searchQuery, ignoreCase = true) ||
                    movie.genre.contains(searchQuery, ignoreCase = true) ||
                    movie.level.contains(searchQuery, ignoreCase = true)

            val matchesLevel = when (selectedLevel) {
                "Анхан" -> movie.level.contains("Beginner", ignoreCase = true)
                "Дунд" -> movie.level.contains("Intermediate", ignoreCase = true)
                "Ахисан" -> movie.level.contains("Advanced", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesLevel
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Cinematic Hero Banner Image from User
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(NeonCyan, NeonMagenta)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NeonDarkBg)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.img_movies_hero_1781233508368),
                    contentDescription = "Cinematic Movies Collage",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Dark Gradient overlay to make text highly readable and add depth
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Overlaid content text, e.g. Studio Title
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonMagenta)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ОНЦЛОХ КИНО СТУДИ",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CineLingo: Англи Хэл Сурах Ертөнц",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Кино уран бүтээлээр дамжуулан аялга, үг хэллэгийг төгс эзэмших",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Neo stats widget
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(NeonCyan.copy(alpha = 0.4f), NeonMagenta.copy(alpha = 0.4f))
                        )
                    ),
                    RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = NeonCardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "СУРАЛЦАХ ЯВЦ",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Англи Хэлний Түвшин",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Кино уншиж суралцахад зорилтот үгс Слайд карт болж хадгалагдана.",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth(0.72f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonDarkBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = savedWords.size.toString(),
                        color = NeonMagenta,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Карт",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title movie section
        Text(
            text = "СУРАЛЦАХ КИНОНУУД",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Сонгон тоглож үгийн баялгийг нэмээрэй",
            color = TextGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search Input filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Киноны нэр, жанр хайх...", color = TextGray, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Хайх",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Цэвэрлэх",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = NeonDarkBg,
                unfocusedContainerColor = NeonDarkBg,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("movie_search_input"),
            shape = RoundedCornerShape(14.dp)
        )

        // Difficulty filter chips
        val levelFilters = listOf("Бүх", "Анхан", "Дунд", "Ахисан")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            levelFilters.forEach { levelOption ->
                val isSelected = selectedLevel == levelOption
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(listOf(NeonCyan, NeonMagenta))
                            } else {
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f)))
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedLevel = levelOption }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("level_filter_${levelOption}")
                ) {
                    Text(
                        text = levelOption,
                        color = if (isSelected) NeonDarkBg else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                    )
                }
            }
        }

        if (filteredMovies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NeonCardBg)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Түүвэр харагдаагүй",
                        tint = TextGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Хайлтад тохирох кино олдсонгүй",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Өөр түлхүүр үг оруулах эсвэл шүүлтүүрийг цэвэрлэнэ үү.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Movie lists
            filteredMovies.forEach { movie ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color(movie.cardColorHex).copy(alpha = 0.6f), Color.Transparent)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = NeonCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(movie.cardColorHex))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = movie.level.uppercase(),
                                    color = Color(movie.cardColorHex),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                        text = "${movie.title} (${movie.titleMn})",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                )
                                if (movie.id.startsWith("custom_")) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonMagenta.copy(alpha = 0.2f))
                                            .border(1.dp, NeonMagenta, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "ГИШҮҮНИЙ КИНО",
                                            color = NeonMagenta,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = movie.accent,
                                color = TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Төрөл жанр: ${movie.genre} | Гаргасан он: ${movie.year} он",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Судлах хэсэг: ${movie.subtitles.size} өгүүлбэр",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = { viewModel.selectMovie(movie) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(movie.cardColorHex).copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(movie.cardColorHex)),
                            modifier = Modifier.testTag("start_movie_${movie.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "",
                                    tint = Color(movie.cardColorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Үзэх, сурах",
                                    color = Color(movie.cardColorHex),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun SavedWordsTabScreen(viewModel: CineLingoViewModel) {
    val savedWords by viewModel.savedWords.collectAsState()
    var showFlashcardQuiz by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadReminderSettings(context)
    }

    if (showFlashcardQuiz) {
        SavedWordsFlashcardDialog(
            savedWords = savedWords,
            viewModel = viewModel,
            onDismissRequest = { showFlashcardQuiz = false }
        )
    }

    if (showExportDialog) {
        ExportVocabularyDialog(
            savedWords = savedWords,
            onDismissRequest = { showExportDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ХАДГАЛСАН КАРТ",
                    color = NeonMagenta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Суралцах үгс (${savedWords.size})",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (savedWords.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearWordBank() }
                ) {
                    Text("Цэвэрлэх", color = NeonMagenta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Daily study reminder widget
        DailyReminderWidget(viewModel = viewModel)

        Spacer(modifier = Modifier.height(12.dp))

        if (savedWords.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CollectionsBookmark,
                    contentDescription = "",
                    tint = TextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Таны карт хоосон байна",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Кино уншиж байхдаа хэрэгтэй үгэн дээрээ дарж Слайд карт болгон үгийн сандаа нэмнэ үү.",
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showFlashcardQuiz = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .testTag("flashcard_practice_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(NeonCyan, NeonMagenta)),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipToBack,
                                contentDescription = null,
                                tint = NeonDarkBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "КАРТААР ДАВТАХ",
                                color = NeonDarkBg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .testTag("vocabulary_export_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, NeonCyan), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ЭКСПОРТЛОХ",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(savedWords) { word ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (word.isLearned) NeonGreen.copy(alpha = 0.4f) else TextMuted.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = NeonCardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "[${word.partOfSpeech.lowercase()}]",
                                    color = NeonYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { viewModel.deleteWord(word.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Устгах",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = word.word,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = word.translation,
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (word.isLearned) NeonGreen.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f))
                                    .border(
                                        0.5.dp,
                                        if (word.isLearned) NeonGreen.copy(alpha = 0.3f) else TextMuted.copy(alpha = 0.2f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        viewModel.toggleSavedWordLearned(word.id, word.isLearned)
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (word.isLearned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "",
                                    tint = if (word.isLearned) NeonGreen else TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (word.isLearned) "Сурсан (Мастер)" else "Сурах",
                                    color = if (word.isLearned) NeonGreen else TextMuted,
                                    fontSize = 9.sp,
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

@Composable
fun DailyReminderWidget(viewModel: CineLingoViewModel) {
    val context = LocalContext.current
    val savedWords by viewModel.savedWords.collectAsState()
    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.updateReminderSettings(context, true, reminderHour, reminderMinute)
            android.widget.Toast.makeText(context, "Өдрийн сануулагч идэвхжлээ!", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Мэдэгдэл илгээх зөвшөөрөл шаардлагатай.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isReminderEnabled) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = NeonCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Reminder Status",
                        tint = if (isReminderEnabled) NeonCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Өдөр тутмын давтлага сануулагч",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isReminderEnabled) {
                                "Идэвхтэй: Өдөр бүр ${String.format("%02d:%02d", reminderHour, reminderMinute)} цагт"
                            } else {
                                "Одоогоор идэвхгүй байна."
                            },
                            color = if (isReminderEnabled) NeonGreen else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand/Collapse",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = Color.White.copy(alpha = 0.08f))

                    Text(
                        text = "Сургалтын зуршил үүсгэгч сануулагч идэвхжүүлэх",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Мэдэгдэл илгээх сануулга",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.updateReminderSettings(context, true, reminderHour, reminderMinute)
                                        android.widget.Toast.makeText(context, "Өдрийн сануулагч идэвхжлээ!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    viewModel.updateReminderSettings(context, false, reminderHour, reminderMinute)
                                    android.widget.Toast.makeText(context, "Өдрийн сануулагч унтарлаа.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                            )
                        )
                    }

                    if (isReminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "САНУУЛАХ ЦАГИЙН ТОХИРГОО",
                            color = NeonMagenta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Hour Picker
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        var nextHour = reminderHour - 1
                                        if (nextHour < 0) nextHour = 23
                                        viewModel.updateReminderSettings(context, true, nextHour, reminderMinute)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, "-", tint = NeonMagenta, modifier = Modifier.size(16.dp))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Цаг", color = TextGray, fontSize = 9.sp)
                                    Text(
                                        text = String.format("%02d", reminderHour),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        var nextHour = reminderHour + 1
                                        if (nextHour > 23) nextHour = 0
                                        viewModel.updateReminderSettings(context, true, nextHour, reminderMinute)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, "+", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Minute Picker
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        var nextMinute = reminderMinute - 5
                                        if (nextMinute < 0) nextMinute = 55
                                        viewModel.updateReminderSettings(context, true, reminderHour, nextMinute)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, "-", tint = NeonMagenta, modifier = Modifier.size(16.dp))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Минут", color = TextGray, fontSize = 9.sp)
                                    Text(
                                        text = String.format("%02d", reminderMinute),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        var nextMinute = reminderMinute + 5
                                        if (nextMinute > 55) nextMinute = 0
                                        viewModel.updateReminderSettings(context, true, reminderHour, nextMinute)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, "+", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                val channelSpecId = "daily_saved_words_channel"

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channel = NotificationChannel(
                                        channelSpecId,
                                        "Өдрийн давтлага сануулагч",
                                        NotificationManager.IMPORTANCE_DEFAULT
                                    )
                                    notificationManager.createNotificationChannel(channel)
                                }

                                val launchIntent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    putExtra("target_tab", "saved")
                                }

                                val pendingIntent = PendingIntent.getActivity(
                                    context,
                                    101,
                                    launchIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                val reminderTitle = "🎬 CineLingo - Өдрийн давтлага (Туршилт)"
                                val textContent = if (savedWords.isNotEmpty()) {
                                    "Танд давтах шаардлагатай ${savedWords.size} үг байна. Өнөөдөр хамтдаа бататгая!"
                                } else {
                                    "Өнөөдөр кино үзэж, шинэ сонирхолтой үгсээ хадгалаарай!"
                                }

                                val testNotification = NotificationCompat.Builder(context, channelSpecId)
                                    .setSmallIcon(android.R.drawable.star_on)
                                    .setContentTitle(reminderTitle)
                                    .setContentText(textContent)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .build()

                                notificationManager.notify(9988, testNotification)
                                android.widget.Toast.makeText(context, "Туршилтын өдрийн сануулга (Notification) илгээгдлээ!", android.widget.Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlashOn, "Test", tint = NeonYellow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("МЭДЭГДЭЛ ШУУД ТУРШИЖ ҮЗЭХ", color = NeonYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickQuizTabScreen(viewModel: CineLingoViewModel) {
    val quizQuestion by viewModel.quizQuestion.collectAsState()
    val selectedAnswer by viewModel.selectedAnswerIndex.collectAsState()
    val feedback by viewModel.quizFeedback.collectAsState()
    val score by viewModel.quizScore.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ИДЭВХТЭЙ ДААЛГАВАР",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Нийт Оноо: $score хэлбэр",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ШУУД СОРИЛ",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (quizQuestion != null) {
            val question = quizQuestion!!

            // Question Box
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NeonCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ДАРААХ ҮГНИЙ ЗӨВ ОРЧУУЛГЫГ СОНГОНО УУ:",
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = question.englishWord,
                            color = NeonCyan,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "part of speech: ${question.partOfSpeech.lowercase()}",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    // Context sentence clue helper
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonDarkBg)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "ТУСЛАМЖ (КИНОНЫ ЭХ ХЭЛЛЭГ):",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "... ${question.contextSentence.replace(question.englishWord, "_____", ignoreCase = true)} ...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Feedback details if answered
                    if (feedback != null) {
                        Text(
                            text = feedback!!,
                            color = if (selectedAnswer == question.correctAnswerIndex) NeonGreen else NeonMagenta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multiple choice grid options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.options.forEachIndexed { i, option ->
                    val isSelected = selectedAnswer == i
                    val isCorrect = i == question.correctAnswerIndex

                    val borderColor = when {
                        selectedAnswer != null && isCorrect -> NeonGreen
                        isSelected && !isCorrect -> NeonMagenta
                        else -> TextMuted.copy(alpha = 0.3f)
                    }

                    val containerColor = when {
                        selectedAnswer != null && isCorrect -> NeonGreen.copy(alpha = 0.1f)
                        isSelected && !isCorrect -> NeonMagenta.copy(alpha = 0.1f)
                        else -> NeonCardBg
                    }

                    Button(
                        onClick = { viewModel.submitQuizAnswer(i) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(
                            text = option,
                            color = if (selectedAnswer != null && isCorrect) NeonGreen else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next question trigger button
            if (selectedAnswer != null) {
                Button(
                    onClick = { viewModel.generateNewQuestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(NeonCyan, NeonMagenta)),
                            RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Дараагийн асуулт", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "")
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

        } else {
            // Unlikely since we fallback to pre-programmed film words
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Хариу ачаалахад алдаа гарлаа.", color = TextGray)
            }
        }
    }
}

@Composable
fun SavedWordsFlashcardDialog(
    savedWords: List<SavedWord>,
    viewModel: CineLingoViewModel,
    onDismissRequest: () -> Unit
) {
    if (savedWords.isEmpty()) {
        onDismissRequest()
        return
    }

    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(0) }
    val currentCard = savedWords.getOrNull(currentIndex) ?: savedWords.first()

    // Flip state re-initializes to false when moving to another card
    var isFlipped by remember(currentIndex) { mutableStateOf(false) }

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

    // Auto speak when word changes or TTS is ready
    LaunchedEffect(currentIndex, isTtsReady) {
        if (isTtsReady) {
            ttsInstance?.speak(currentCard.word, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_flip_rotation"
    )

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipToBack,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Картаар давтах",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Хаах",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Дасгалын ахиц:",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentIndex + 1} / ${savedWords.size}",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }

                LinearProgressIndicator(
                    progress = (currentIndex + 1).toFloat() / savedWords.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonCyan,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Flippable card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 8 * density
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isFlipped = !isFlipped
                            // Speak word!
                            if (isTtsReady) {
                                ttsInstance?.speak(currentCard.word, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeonDarkBg)
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                if (isFlipped) listOf(NeonMagenta, NeonCyan) else listOf(NeonCyan, NeonMagenta)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val renderBack = rotation > 90f

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (renderBack) rotationY = 180f
                            },
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!renderBack) {
                            // FRONT SIDE: ENGLISH WORD (MONGOLIAN DEFINITION HIDDEN)
                            Text(
                                text = "АНГЛИ ҮГ",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = NeonYellow.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, NeonYellow.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = currentCard.partOfSpeech.uppercase(),
                                        color = NeonYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentCard.word,
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (isTtsReady) {
                                                ttsInstance?.speak(currentCard.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp).testTag("speak_front_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Англи дуудлага сонсох",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Товшиж орчуулгыг харах",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            // BACK SIDE: REVEALED MONGOLIAN DEFINITION
                            Text(
                                text = "МОНГОЛ ОРЧУУЛГА",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentCard.word,
                                        color = TextGray,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = if (currentCard.isLearned) {
                                            androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                        } else {
                                            androidx.compose.ui.text.TextStyle()
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (isTtsReady) {
                                                ttsInstance?.speak(currentCard.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp).testTag("speak_back_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Англи дуудлага сонсох",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = currentCard.translation,
                                    color = NeonGreen,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )

                                if (currentCard.sentence.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "\"${currentCard.sentence}\"",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    if (currentCard.sentenceTranslation.isNotEmpty()) {
                                        Text(
                                            text = currentCard.sentenceTranslation,
                                            color = NeonCyan.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp).padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Товшиж буцах",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Learned on practice screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (currentCard.isLearned) NeonGreen.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                        .border(
                            1.dp,
                            if (currentCard.isLearned) NeonGreen.copy(alpha = 0.4f) else TextMuted.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.toggleSavedWordLearned(currentCard.id, currentCard.isLearned)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentCard.isLearned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (currentCard.isLearned) NeonGreen else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentCard.isLearned) "Сурсан гэж тэмдэглэсэн байна" else "Энэ үгийг сурсан гэж тэмдэглэх",
                        color = if (currentCard.isLearned) NeonGreen else Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev button
                OutlinedButton(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("quiz_prev_btn"),
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (currentIndex > 0) NeonCyan else TextMuted)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Өмнөх",
                        tint = if (currentIndex > 0) NeonCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Өмнөх",
                        color = if (currentIndex > 0) NeonCyan else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Next button
                Button(
                    onClick = {
                        if (currentIndex < savedWords.size - 1) {
                            currentIndex++
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("quiz_next_btn"),
                    enabled = currentIndex < savedWords.size - 1,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentIndex < savedWords.size - 1) NeonCyan else TextMuted,
                        disabledContainerColor = TextMuted.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "Дараах",
                        color = if (currentIndex < savedWords.size - 1) NeonDarkBg else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Дараах",
                        tint = if (currentIndex < savedWords.size - 1) NeonDarkBg else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        dismissButton = {}
    )
}

