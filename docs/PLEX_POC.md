# Plex real-server POC

This POC comes before wiring the Compose demo to a user account. Its purpose is to prove the exact Plex behaviors required by the product, using only documented endpoints and a real Plex Media Server (PMS).

## What current Plex documentation establishes

- New clients should use Plex's PIN/JWK JWT flow. It creates a PIN at `clients.plex.tv`, opens the Plex authorization page, and exchanges the claimed PIN for an `authToken`. The server API expects the token in `X-Plex-Token` and a client identifier.
- The PMS API supports JSON when `Accept: application/json` is sent. A new client starts its media-library discovery at `/media/providers` rather than hard-coding library endpoints, then follows returned feature keys.
- API version `1.0.0` added documented `/downloadQueue` endpoints, though the app must inspect the exact request/response schema for the target PMS version during implementation.

Sources: [Plex PMS API reference](https://developer.plex.tv/pms/) and [Plex developer portal](https://developer.plex.tv/), checked 2026-09-20.

## Acceptance sequence

| Step | Proof to capture | App boundary |
| --- | --- | --- |
| 1. Sign in | PIN is claimed and a token is received; token is stored securely | `PlexAuthRepository` |
| 2. Find PMS | User's reachable PMS and connection URI are discovered | `PlexServerRepository` |
| 3. Find libraries | Movies and TV libraries appear using provider-discovered keys | `PlexLibraryRepository` |
| 4. Read media | A movie, show, season, and episode return metadata, art, media parts, resolution, and size | `PlexMediaRepository` |
| 5. Download original | One chosen media part is saved to app-owned storage, then opened by an Android content URI | `DownloadManager` |
| 6. Inspect versions | Existing original/optimized versions can be identified with their actual bytes and resolution | `PlexMediaRepository` |
| 7. Choose automatically | The app chooses the smallest existing appropriate candidate without a quality prompt | `DownloadDecision` |
| 8. Create 720p | PMS is asked to create an optimized version, only after a user chooses Make Smaller | `DownloadManager` |
| 9. Monitor preparation | The app reflects queued/running/failed/complete optimization accurately | `DownloadManager` |
| 10. Download result | The resulting optimized media is written locally and appears in My Videos | `DownloadRepository` |

## Important unresolved point

The current public PMS reference clearly documents authentication, provider discovery, media-library traversal, and the existence of a download queue. It does **not** expose a clear, documented contract in the reference for creating and monitoring optimized media versions. Community examples mention older/private-style optimization calls, but this app will not rely on them.

So steps 8–10 are intentionally not implemented. Before any production implementation, verify that the target PMS release exposes documented optimized-version creation and status endpoints. If it does not, the safe product fallback is: automatically download the smallest already-present media version, hide **Make Smaller — 720p**, and explain the capability limitation in the adult-facing settings area. The child-facing download action remains simple.

## Test setup needed

- A Plex account and a reachable, user-authorized PMS.
- One movie above 720p, one episode at 720p or below, and one title with an existing optimized version.
- Enough free Android storage for a real original-part transfer.
- A device/emulator where the app can expose a downloaded file with `FileProvider` and launch `ACTION_VIEW` with a read grant.

No Plex credentials, URLs, or media IDs should be committed to this repository.

