---
name: dejaview-media
description: Implement DejaView image/video/audio import, recording, upload and resource cleanup. Use for file providers, media processing and search modes.
---

# Media

Read contracts.md MediaStore/SearchCompletion and the modality UC. Reuse existing media libraries/importer; use dependencies before adding codecs, trimming or transformations. Camera/crop/trim/share/background work are not automatically in sprint scope.

Content URI is not a filesystem path. Stream into private temp with a byte limit while reading; provider MIME/size/name are untrusted. Validate nonempty content, format and duration. Unknown duration is not zero. Limits are image 10 MiB; video 50 MiB/30000 ms; audio 20 MiB/10000 ms. Validate transformed output too.

Define draft ownership through preparation/upload/error/success/cancel. Retain a valid draft for explicit retry after a network error; release on replacement, cancellation and success. Close all streams/calls/players/recorders; startup cleans abandoned temporary files. History retains metadata, not originals. Durable background work requires separate ownership/TTL.

Upload progress measures bytes; processing has no fake percentage. Request IDs suppress late completion. Only shell writes history. Request microphone permission on recording, preserve picker fallback after denial, release recorder when leaving. An M4A suffix does not prove AAC/container correctness.

Automate limit boundaries, revoked URI, unknown metadata, corrupt codec, disk exhaustion, cancel during copy/upload, rotation and account changes. Test actual provider and recorder/playback paths on device/emulator where supported; label remaining hardware checks precisely. Never commit users' media as fixtures.
