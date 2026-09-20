package com.plexdownloader.data

import com.plexdownloader.domain.BrowseItem

/** Boundaries for the real Plex POC. Implement these with documented Plex flows only. */
interface PlexAuthRepository {
    suspend fun isSignedIn(): Boolean
    suspend fun beginSignIn(): String
}

interface PlexServerRepository {
    suspend fun discoverServers(): List<PlexServer>
}

data class PlexServer(val id: String, val name: String, val baseUrl: String)

interface PlexLibraryRepository {
    suspend fun libraries(serverId: String): List<PlexLibrary>
}

data class PlexLibrary(val id: String, val title: String, val type: LibraryType)
enum class LibraryType { MOVIES, TV_SHOWS }

interface PlexMediaRepository {
    suspend fun browse(libraryId: String): List<BrowseItem>
    suspend fun item(itemId: String): BrowseItem?
}

