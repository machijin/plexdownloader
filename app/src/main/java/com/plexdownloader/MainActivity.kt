package com.plexdownloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plexdownloader.data.AppContainer
import com.plexdownloader.ui.PlexDownloaderApp
import com.plexdownloader.ui.PlexDownloaderViewModel
import com.plexdownloader.ui.PlexDownloaderViewModelFactory
import com.plexdownloader.ui.theme.PlexDownloaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = AppContainer.demo()

        setContent {
            PlexDownloaderTheme {
                val appViewModel: PlexDownloaderViewModel = viewModel(
                    factory = PlexDownloaderViewModelFactory(container)
                )
                PlexDownloaderApp(appViewModel)
            }
        }
    }
}

