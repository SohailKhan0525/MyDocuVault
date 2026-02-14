# MyDocVault

MyDocVault is a personal, offline document vault for Android. It stores files in internal app storage and uses a local Room database for metadata. No cloud sync, no login, single-user only.

## Features
- PIN lock with optional biometric unlock
- Nested folders with create/rename/delete
- Import images, PDF, and DOCX
- Document rename, delete, replace, and move
- Image zoom viewer, PDF viewer (PdfRenderer), DOCX text preview
- Update check via GitHub Releases API with APK install intent

## Tech Stack
- Kotlin, Jetpack Compose, Material 3
- MVVM + Hilt
- Room, DataStore
- Navigation Compose, Coroutines/Flow

## Project Structure
- app/src/main/java/com/mydocvault/data
- app/src/main/java/com/mydocvault/di
- app/src/main/java/com/mydocvault/ui
- app/src/main/java/com/mydocvault/viewmodel
- app/src/main/java/com/mydocvault/utils

## Setup
This project expects a local Android SDK. If you are in a Codespaces-like Linux environment, set the SDK path in local.properties:

```
sdk.dir=/workspaces/MyDocuVault/android-sdk
```

## Build
```
./gradlew build
./gradlew assembleDebug
./gradlew assembleRelease
```

APK outputs:
- app/build/outputs/apk/debug/app-debug.apk
- app/build/outputs/apk/release/app-release.apk