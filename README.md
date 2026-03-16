# MyDocuVault

**MyDocuVault** is a private, offline document vault for Android. Keep your important files — images, PDFs, and Word documents — locked away on your device with PIN and biometric protection. No accounts, no cloud sync, no data ever leaves your phone.

---

## What Does It Do?

MyDocuVault gives you a secure, private space on your Android device to store and organise sensitive documents. Think of it as an encrypted filing cabinet that lives entirely on your phone:

- You set a **4-digit PIN** (and optionally enable **fingerprint / face unlock**) to protect access.
- Inside, you can create a **nested folder hierarchy** to organise files any way you like.
- You can **import images, PDFs, and DOCX files** from your device, then view, rename, move, or delete them at any time.
- Everything is stored in the app's **private internal storage** — other apps cannot access your files.
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
└── utils/          # File helpers, DocxTextExtractor, FileType enum
```

### Navigation flow

```
Splash ──► PIN (create or unlock) ──► Home ──► Folder ──► Document
                                           └──► Settings
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

# Assemble release APK
./gradlew assembleRelease

# Full build (compile + lint + test)
./gradlew build
```

### APK output locations

| Variant | Path |
|---|---|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

---

## Architecture Overview

MyDocuVault follows the **MVVM** pattern with a unidirectional data flow:

```
Compose UI  ◄──StateFlow──  ViewModel  ◄──Flow──  Repository
                                │                      │
                           (Hilt DI)             Room DB + FileManager
```

- **UI layer** — stateless Compose screens that observe `StateFlow` from ViewModels.
- **ViewModel layer** — business logic, state management, Hilt-injected, coroutines on `Dispatchers.IO`.
- **Data layer** — `VaultRepositoryImpl` coordinates the Room database and the filesystem; DAOs expose `Flow` for reactive updates.

---

## Contributing

1. Fork the repository and create a feature branch.
2. Make your changes and ensure the project builds (`./gradlew build`).
3. Open a pull request with a clear description of what changed and why.

---

## License

This project is provided as-is. See the repository for license details.