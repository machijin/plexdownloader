package com.plexdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.plexdownloader.data.AppContainer
import com.plexdownloader.domain.BrowseItem
import com.plexdownloader.domain.DownloadDecision
import com.plexdownloader.domain.MediaVersion
import com.plexdownloader.domain.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface Destination {
    data object Home : Destination
    data object DownloadHub : Destination
    data class Library(val id: String, val title: String) : Destination
    data class Detail(val item: BrowseItem) : Destination
    data object Settings : Destination
}

class PlexDownloaderViewModel(private val container: AppContainer) : ViewModel() {
    val downloadedVideos = container.downloadRepository.downloadedVideos
    val activeDownloads = container.downloadRepository.activeDownloads

    private val _destination = MutableStateFlow<Destination>(Destination.Home)
    val destination: StateFlow<Destination> = _destination.asStateFlow()

    private val _libraryItems = MutableStateFlow<List<BrowseItem>>(emptyList())
    val libraryItems: StateFlow<List<BrowseItem>> = _libraryItems.asStateFlow()

    fun openDownloadHub() {
        _destination.value = Destination.DownloadHub
    }

    fun openLibrary(id: String, title: String) {
        _destination.value = Destination.Library(id, title)
        viewModelScope.launch { _libraryItems.value = container.plexMediaRepository.browse(id) }
    }

    fun openDetail(item: BrowseItem) {
        _destination.value = Destination.Detail(item)
    }

    fun openSettings() {
        _destination.value = Destination.Settings
    }

    fun back() {
        _destination.value = when (_destination.value) {
            is Destination.Library, Destination.DownloadHub, Destination.Settings -> Destination.Home
            is Destination.Detail -> Destination.DownloadHub
            Destination.Home -> Destination.Home
        }
    }

    fun queueDownload(item: BrowseItem) {
        val selected = DownloadDecision.smallestAvailable(item.versions) ?: return
        viewModelScope.launch {
            container.downloadRepository.enqueue(item.video, selected)
            _destination.value = Destination.Home
        }
    }

    fun makeSmaller(item: BrowseItem) {
        // The real manager will ask Plex to create an optimized version and monitor it.
        // This starter keeps the request visible in the manager instead of inventing an API call.
        val source = DownloadDecision.smallestAvailable(item.versions) ?: return
        val optimized = MediaVersion("${source.id}-720-request", 720, source.sizeBytes * 55 / 100, true)
        viewModelScope.launch {
            container.downloadRepository.enqueue(item.video, optimized)
            _destination.value = Destination.Home
        }
    }

    fun delete(video: VideoItem) {
        viewModelScope.launch { container.downloadRepository.delete(video.id) }
    }
}

class PlexDownloaderViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PlexDownloaderViewModel(container) as T
}

