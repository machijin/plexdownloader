package com.plexdownloader.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plexdownloader.domain.BrowseItem
import com.plexdownloader.domain.DownloadDecision
import com.plexdownloader.domain.DownloadTask
import com.plexdownloader.domain.MediaKind
import com.plexdownloader.domain.VideoItem
import com.plexdownloader.ui.theme.PlexDownloaderTheme
import java.util.Locale

@Composable
fun PlexDownloaderApp(viewModel: PlexDownloaderViewModel) {
    val destination by viewModel.destination.collectAsState()
    val downloadedVideos by viewModel.downloadedVideos.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()
    val context = LocalContext.current

    when (val screen = destination) {
        Destination.Home -> HomeScreen(
            videos = downloadedVideos,
            activeDownload = activeDownloads.firstOrNull(),
            onDownload = viewModel::openDownloadHub,
            onSettings = viewModel::openSettings,
            onWatch = { openWithSystemPlayer(it, context) },
            onDelete = viewModel::delete,
        )
        Destination.DownloadHub -> DownloadHubScreen(
            onBack = viewModel::back,
            onOpenMovies = { viewModel.openLibrary("movies", "Movies") },
            onOpenShows = { viewModel.openLibrary("shows", "TV Shows") },
        )
        is Destination.Library -> LibraryScreen(
            title = screen.title,
            items = libraryItems,
            onBack = viewModel::back,
            onOpenItem = viewModel::openDetail,
        )
        is Destination.Detail -> DetailScreen(
            item = screen.item,
            onBack = viewModel::back,
            onDownload = { viewModel.queueDownload(screen.item) },
            onMakeSmaller = { viewModel.makeSmaller(screen.item) },
        )
        Destination.Settings -> SettingsScreen(onBack = viewModel::back)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    videos: List<VideoItem>,
    activeDownload: DownloadTask?,
    onDownload: () -> Unit,
    onSettings: () -> Unit,
    onWatch: (VideoItem) -> Unit,
    onDelete: (VideoItem) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<VideoItem?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PlexDownloader", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onSettings) { Text("⚙", style = MaterialTheme.typography.titleLarge) } },
            )
        },
        bottomBar = { DownloadButton(onClick = onDownload) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        ) {
            Text("My Videos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (videos.isEmpty()) {
                EmptyLibrary(Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoCard(video = video, onClick = { onWatch(video) }, onDelete = { pendingDelete = video })
                    }
                }
            }
            activeDownload?.let { DownloadStatus(it) }
        }
        pendingDelete?.let { video ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete ${video.title}?") },
                text = { Text("This video will be removed from this device.") },
                confirmButton = {
                    TextButton(onClick = {
                        onDelete(video)
                        pendingDelete = null
                    }) { Text("Delete") }
                },
                dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Keep video") } },
            )
        }
    }
}

@Composable
private fun EmptyLibrary(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📺", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(18.dp))
        Text("Nothing downloaded", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Find something to watch", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DownloadButton(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(20.dp)) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp)) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("DOWNLOAD", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DownloadStatus(task: DownloadTask) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Downloading ${task.item.title}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { task.progress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text("${(task.progress * 100).toInt()}%  •  ${formatBytes(task.bytesDownloaded)} / ${formatBytes(task.selectedVersion.sizeBytes)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadHubScreen(onBack: () -> Unit, onOpenMovies: () -> Unit, onOpenShows: () -> Unit) {
    Scaffold(topBar = { BackTopBar("Download", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Find something new", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Choose a library to browse.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            LibraryChoice("🎬", "Movies", "Big-screen adventures", onOpenMovies)
            LibraryChoice("📺", "TV Shows", "Episodes and series", onOpenShows)
        }
    }
}

@Composable
private fun LibraryChoice(emoji: String, title: String, summary: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun LibraryScreen(title: String, items: List<BrowseItem>, onBack: () -> Unit, onOpenItem: (BrowseItem) -> Unit) {
    Scaffold(topBar = { BackTopBar(title, onBack) }) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(items, key = { it.video.id }) { item ->
                BrowseCard(item, onClick = { onOpenItem(item) })
            }
        }
    }
}

@Composable
private fun DetailScreen(item: BrowseItem, onBack: () -> Unit, onDownload: () -> Unit, onMakeSmaller: () -> Unit) {
    val selected = DownloadDecision.smallestAvailable(item.versions)
    Scaffold(topBar = { BackTopBar("Download", onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Poster(item.video, Modifier.fillMaxWidth(0.54f).aspectRatio(2f / 3f))
            Spacer(Modifier.height(20.dp))
            Text(item.video.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            item.video.subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.height(28.dp))
            selected?.let {
                Text("Download size", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatBytes(it.sizeBytes), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                SizeIndicator(it.sizeBytes)
                Spacer(Modifier.height(28.dp))
                Button(onClick = onDownload, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("DOWNLOAD", fontWeight = FontWeight.Bold)
                }
                if (DownloadDecision.canMakeSmaller(it)) {
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(onClick = onMakeSmaller, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text("Make Smaller — 720p")
                    }
                }
            }
        }
    }
}

@Composable
private fun SizeIndicator(sizeBytes: Long) {
    val proportion = (sizeBytes.toDouble() / 8_000_000_000.0).toFloat().coerceIn(0.08f, 1f)
    Column(Modifier.fillMaxWidth(0.72f)) {
        LinearProgressIndicator(progress = { proportion }, modifier = Modifier.fillMaxWidth().height(10.dp), trackColor = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text("Smaller download", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VideoCard(video: VideoItem, onClick: () -> Unit, onDelete: () -> Unit) {
    Column {
        Box {
            Poster(video, Modifier.fillMaxWidth().aspectRatio(2f / 3f).clickable(onClick = onClick))
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(Color(0xB3000000), RoundedCornerShape(20.dp)).size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete ${video.title}", tint = Color.White)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(video.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        video.subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun BrowseCard(item: BrowseItem, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick)) {
        Poster(item.video, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        Spacer(Modifier.height(6.dp))
        Text(item.video.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        item.video.subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun Poster(video: VideoItem, modifier: Modifier = Modifier) {
    val color = Color(video.artworkColor)
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(color), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(video.artworkLabel, style = MaterialTheme.typography.displayLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(if (video.kind == MediaKind.EPISODE) "TV" else "MOVIE", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(topBar = { BackTopBar("Settings", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("Plex connection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Connecting your Plex server will be added after the real-server POC.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))
            Text("Downloads", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Videos will be stored on this device and opened with your usual video player.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun openWithSystemPlayer(video: VideoItem, context: Context) {
    val uri = video.localUri?.let(Uri::parse) ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Watch ${video.title}"))
}

private fun formatBytes(bytes: Long): String {
    return if (bytes >= 1_000_000_000) String.format(Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
    else String.format(Locale.US, "%d MB", bytes / 1_000_000)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun EmptyLibraryPreview() {
    PlexDownloaderTheme { EmptyLibrary(Modifier.fillMaxHeight()) }
}
