package com.plexdownloader.domain

enum class MediaKind { MOVIE, EPISODE, SHOW }

data class VideoItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val kind: MediaKind,
    val artworkLabel: String,
    val artworkColor: Long,
    val localUri: String? = null,
)

data class MediaVersion(
    val id: String,
    val resolution: Int,
    val sizeBytes: Long,
    val isOptimized: Boolean,
)

data class BrowseItem(
    val video: VideoItem,
    val versions: List<MediaVersion>,
)

data class DownloadTask(
    val item: VideoItem,
    val selectedVersion: MediaVersion,
    val bytesDownloaded: Long,
    val state: DownloadState,
) {
    val progress: Float
        get() = if (selectedVersion.sizeBytes == 0L) 0f else bytesDownloaded.toFloat() / selectedVersion.sizeBytes
}

enum class DownloadState { PREPARING, DOWNLOADING, COMPLETE, FAILED }

