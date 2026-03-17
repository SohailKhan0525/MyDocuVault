# MyDocuVault

**MyDocuVault** is a private, offline document vault for Android. Keep your important files — images, PDFs, and Word documents — locked away on your device with PIN and biometric protection. No accounts, no cloud sync, no data ever leaves your phone.

---

## What Does It Do?

MyDocuVault gives you a secure, private space on your Android device to store and organise sensitive documents. Think of it as an encrypted filing cabinet that lives entirely on your phone:

- You set a **4-digit PIN** (and optionally enable **fingerprint / face unlock**) to protect access.
- Inside, you can create a **nested folder hierarchy** to organise files any way you like.
- You can **import images, PDFs, and DOCX files** from your device, then view, rename, move, or delete them at any time.
- Everything is stored in the app's **private internal storage** — other apps cannot access your files.
- A built-in **backup system** zips your documents and database into a single file saved to `Documents/MyDocuVaultBackup/` on your device — and can restore everything from that same file.
- **Auto-backup** runs silently every 8 hours in the background using WorkManager so your data is always protected.
- A built-in **update checker** notifies you when a new version is available and guides you through installing it.

---

## Features

### Security
- 🔒 **PIN lock** — 4-digit PIN required on every app launch
- 👆 **Biometric unlock** — optional fingerprint, face, or iris authentication
- 🗄️ **Private storage** — files saved to app-scoped internal storage, inaccessible to other apps
- 📵 **Fully offline** — no accounts, no cloud, no tracking

### File Management
- 📁 **Nested folders** — create, rename, and delete folders at any depth
- 📥 **Import documents** — bring in JPG/PNG images, PDF files, and DOCX files
- ✏️ **Rename & move** — reorganise documents without re-importing
- 🔄 **Replace** — swap a document's file while keeping its metadata
- 🗑️ **Delete** — remove individual documents or entire folders

### Document Viewing
- 🖼️ **Image viewer** — pinch-to-zoom with Coil-powered rendering
- 📄 **PDF viewer** — page-by-page navigation using Android's built-in `PdfRenderer`
- 📝 **DOCX preview** — plain-text extraction and display of Word documents

### Backup & Restore
- 💾 **Manual backup** — tap *Back up now* in Settings to create a timestamped ZIP containing all documents and the Room database
- 📂 **Backup location** — `Documents/MyDocuVaultBackup/MyDocuVault_backup_YYYY-MM-DD_HH-mm-ss.zip`
- ⏰ **Auto-backup** — WorkManager schedules a backup every 8 hours (runs only when battery is not low)
- 📥 **Restore** — pick any previous backup ZIP from the file picker; documents and database are fully restored
- 🔔 **Notifications** — silent notification confirms when auto-backup completes

### App Updates
- 🔔 **Update checker** — queries GitHub Releases API for new versions
- ⬇️ **In-app download & install** — downloads the APK with progress feedback and launches the installer

---

## Screenshots

> _Screenshots will be added in a future update._

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material Design 3 |
| Architecture | MVVM |
| Dependency Injection | Hilt 2.52 |
| Database | Room 2.6.1 |
| Preferences | DataStore 1.1.1 |
| Navigation | Navigation Compose 2.8.0 |
| Background Tasks | WorkManager 2.9.1 |
| Async | Kotlin Coroutines & Flow |
| Image Loading | Coil 2.6.0 |
| Networking | OkHttp 4.12.0 |
| Biometrics | AndroidX Biometric 1.1.0 |

---

## Minimum Requirements

| Property | Value |
|---|---|
| Minimum Android | 8.0 Oreo (API 26) |
| Target Android | 14 (API 34) |
| Java compatibility | Java 17 |

---

## Project Structure

```
app/src/main/java/com/mydocvault/
├── data/           # Room entities, DAOs, database, repository
├── di/             # Hilt modules and dependency bindings
├── ui/             # Jetpack Compose screens and reusable components
│   ├── screens/    # Splash, Pin, Home, Folder, Document, Settings
│   └── components/ # PinDots, ZoomableImage, PdfViewer, etc.
├── viewmodel/      # AuthViewModel, HomeViewModel, FolderViewModel,
│                   #   DocumentViewModel, SettingsViewModel
├── workers/        # BackupWorker (WorkManager)
└── utils/          # File helpers, BackupManager, DocxTextExtractor, FileType enum
```

### Navigation flow

```
Splash ──► PIN (create or unlock) ──► Home ──► Folder ──► Document
                                           └──► Settings (Security · Backup · Updates)
```

---

## Setup

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK with API 34 platform installed

### Clone and open
```bash
git clone https://github.com/SohailKhan0525/MyDocuVault.git
```
Open the project root in Android Studio. Gradle will sync automatically.

### Local properties (Codespaces / headless Linux)
If you are building outside of Android Studio in an environment that does not have the SDK on the default path, create a `local.properties` file in the project root:

```
sdk.dir=/path/to/your/android-sdk
```

For GitHub Codespaces the path is typically:
```
sdk.dir=/workspaces/MyDocuVault/android-sdk
```

---

## Build

```bash
# Assemble debug APK
./gradlew assembleDebug

# Full build (compile + lint + test)
./gradlew build
```

### APK output locations

| Variant | Path |
|---|---|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |

---

## Architecture Overview

MyDocuVault follows the **MVVM** pattern with a unidirectional data flow:

```
Compose UI  ◄──StateFlow──  ViewModel  ◄──Flow──  Repository
                                │                      │
                           (Hilt DI)             Room DB + FileManager

WorkManager ──► BackupWorker ──► BackupManager ──► ZIP (documents + DB)
```

- **UI layer** — stateless Compose screens that observe `StateFlow` from ViewModels.
- **ViewModel layer** — business logic, state management, Hilt-injected, coroutines on `Dispatchers.IO`.
- **Data layer** — `VaultRepositoryImpl` coordinates the Room database and the filesystem; DAOs expose `Flow` for reactive updates.
- **Workers layer** — `BackupWorker` runs as a `CoroutineWorker` managed by WorkManager; receives `BackupManager` via Hilt injection.

---

## Backup Details

| Item | Value |
|---|---|
| Backup destination | `Documents/MyDocuVaultBackup/` on external storage |
| File format | ZIP archive |
| File naming | `MyDocuVault_backup_YYYY-MM-DD_HH-mm-ss.zip` |
| Contents | `documents/` (all imported files) + `db/vault.db` (Room database + WAL) |
| Auto-backup interval | Every 8 hours (requires battery not low) |
| Restore method | Settings → Backup & Restore → *Restore backup* → pick ZIP file |
| Post-restore action | Restart the app to reload data |

---

## Contributing

1. Fork the repository and create a feature branch.
2. Make your changes and ensure the project builds (`./gradlew assembleDebug`).
3. Open a pull request with a clear description of what changed and why.

---

## License

This project is released under the **MIT License**.

```
MIT License

Copyright (c) 2024 Mohd Zaheer Uddin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<p align="center">Made with ❤️ by Mohd Zaheer Uddin</p>
