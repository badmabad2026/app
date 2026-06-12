package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.MovieRepository
import com.example.viewmodel.CineLingoViewModel
import com.example.viewmodel.CineLingoViewModelFactory
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cinelingo_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val repository: MovieRepository by lazy {
        MovieRepository(database.savedWordDao(), database.memberDao(), database.movieReviewDao())
    }

    private val viewModel: CineLingoViewModel by lazy {
        ViewModelProvider(
            this,
            CineLingoViewModelFactory(repository)
        )[CineLingoViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle target tab routing if clicked from notification
        intent?.getStringExtra("target_tab")?.let { tab ->
            viewModel.selectTab(tab)
        }

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("target_tab")?.let { tab ->
            viewModel.selectTab(tab)
        }
    }
}

