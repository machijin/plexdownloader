package com.plexdownloader.data

import com.plexdownloader.domain.DownloadTask
import com.plexdownloader.domain.MediaVersion
import com.plexdownloader.domain.VideoItem
import kotlinx.coroutines.flow.StateFlow

interface DownloadRepository {
    val downloadedVideos: StateFlow<List<VideoItem>>
    val activeDownloads: StateFlow<List<DownloadTask>>
    suspend fun enqueue(item: VideoItem, version: MediaVersion)
    suspend fun delete(itemId: String)
}

/**
 * The production implementation will coordinate resumable HTTP/Plex work with
 * Android storage and foreground/background execution. It deliberately stays
 * separate from the repository that the UI observes.
 */
interface DownloadManager {
    suspend fun download(item: VideoItem, version: MediaVersion)
    suspend fun optimizeTo720p(item: VideoItem): MediaVersion
}

