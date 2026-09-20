package com.plexdownloader.domain

/**
 * The normal child-facing flow never asks for a quality. It picks the smallest
 * already-available version. A 720p action is only offered when that choice is
 * higher than 720p; lower-resolution media is never upscaled.
 */
object DownloadDecision {
    fun smallestAvailable(versions: List<MediaVersion>): MediaVersion? =
        versions.minWithOrNull(compareBy<MediaVersion> { it.sizeBytes }.thenBy { it.resolution })

    fun canMakeSmaller(selected: MediaVersion): Boolean = selected.resolution > 720
}

