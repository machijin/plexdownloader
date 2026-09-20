# PlexDownloader

An Android Compose starter for a deliberately simple offline video library. The home screen makes **My Videos** the primary destination and keeps **Download** visible at all times. Plex browsing is a separate flow.

## Current slice

- Compose navigation for My Videos, Download, Movies/TV Shows, item details, and Settings.
- Kid-facing language and layouts from the handoff, including an empty library state, visible download progress, and non-accidental delete control.
- Download decision logic that automatically chooses the smallest existing version and only offers **Make Smaller — 720p** above 720p.
- Explicit interfaces for `PlexAuthRepository`, `PlexServerRepository`, `PlexLibraryRepository`, `PlexMediaRepository`, `DownloadRepository`, and `DownloadManager`.
- In-memory demo catalog only. It exercises the UX but does **not** authenticate to Plex, download files, or request optimization yet.

## Deliberate next step: real-server POC

Before connecting the UI to a Plex account, validate the handoff's POC requirements against a real PMS: auth, server/library/media discovery, original part download, media-version size/resolution inspection, optimized 720p creation and progress, and downloading the optimized result. Do not guess API endpoints.

The current demo implementation is intentionally isolated in `AppContainer.kt`; it can be replaced without changing the UI flow.

## Open in Android Studio

Open this `PlexDownloader` folder in Android Studio, let Gradle sync, then run the `app` configuration on an Android 8.0+ device or emulator. The project uses Compose BOM `2026.09.00`, Kotlin `2.4.10`, and API 37 compilation per current Android documentation.

