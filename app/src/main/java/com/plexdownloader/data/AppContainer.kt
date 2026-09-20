package com.plexdownloader.data

import com.plexdownloader.domain.BrowseItem
import com.plexdownloader.domain.DownloadState
import com.plexdownloader.domain.DownloadTask
import com.plexdownloader.domain.MediaKind
import com.plexdownloader.domain.MediaVersion
import com.plexdownloader.domain.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppContainer(
    val plexMediaRepository: PlexMediaRepository,
    val downloadRepository: DownloadRepository,
) {
    companion object {
        fun demo(): AppContainer {
            val catalog = DemoCatalog.items
            return AppContainer(
                plexMediaRepository = DemoPlexMediaRepository(catalog),
                downloadRepository = DemoDownloadRepository(catalog.take(2).map { it.video }),
            )
        }
    }
}

private object DemoCatalog {
    val items = listOf(
        BrowseItem(
            VideoItem("paddington", "Paddington", kind = MediaKind.MOVIE, artworkLabel = "P", artworkColor = 0xFFEEB44C),
            listOf(MediaVersion("pad-1080", 1080, 1_400_000_000, false))
        ),
        BrowseItem(
            VideoItem("bluey-204", "Bluey", "S02 E04", MediaKind.EPISODE, "B", 0xFF58A8E8),
            listOf(MediaVersion("bluey-720", 720, 680_000_000, true))
        ),
        BrowseItem(
            VideoItem("kiki", "Kiki's Delivery Service", kind = MediaKind.MOVIE, artworkLabel = "K", artworkColor = 0xFFDB6A68),
            listOf(MediaVersion("kiki-1080", 1080, 2_600_000_000, false), MediaVersion("kiki-720", 720, 1_200_000_000, true))
        ),
        BrowseItem(
            VideoItem("totoro", "My Neighbor Totoro", kind = MediaKind.MOVIE, artworkLabel = "T", artworkColor = 0xFF6AB895),
            listOf(MediaVersion("totoro-1080", 1080, 1_900_000_000, false))
        ),
    )
}

private class DemoPlexMediaRepository(private val items: List<BrowseItem>) : PlexMediaRepository {
    override suspend fun browse(libraryId: String): List<BrowseItem> =
        if (libraryId == "movies") items.filter { it.video.kind == MediaKind.MOVIE } else items.filter { it.video.kind != MediaKind.MOVIE }

    override suspend fun item(itemId: String): BrowseItem? = items.firstOrNull { it.video.id == itemId }
}

private class DemoDownloadRepository(initial: List<VideoItem>) : DownloadRepository {
    private val _downloadedVideos = MutableStateFlow(initial)
    override val downloadedVideos: StateFlow<List<VideoItem>> = _downloadedVideos.asStateFlow()
    private val _activeDownloads = MutableStateFlow<List<DownloadTask>>(emptyList())
    override val activeDownloads: StateFlow<List<DownloadTask>> = _activeDownloads.asStateFlow()

    override suspend fun enqueue(item: VideoItem, version: MediaVersion) {
        _activeDownloads.value = listOf(DownloadTask(item, version, version.sizeBytes * 64 / 100, DownloadState.DOWNLOADING))
    }

    override suspend fun delete(itemId: String) {
        _downloadedVideos.value = _downloadedVideos.value.filterNot { it.id == itemId }
    }
}

