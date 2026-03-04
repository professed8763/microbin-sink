# Contributing to MicroBin Sink

## Development Setup

### Prerequisites

- **JDK 17** — Required by the Android Gradle plugin
- **Android SDK** — API level 35 with build-tools 35.0.0
- **Android Studio** (recommended) — or any editor with Kotlin support

### Getting started

```bash
git clone https://github.com/professed8763/microbin-sink.git
cd microbin-sink
```

If using Android Studio, open the project root and let Gradle sync. Otherwise, the Gradle wrapper handles everything from the command line.

## Building

```bash
# Debug build (unsigned)
./gradlew assembleDebug

# Release build (requires signing config — see Release Signing below)
./gradlew assembleRelease
```

Output APKs are written to `app/build/outputs/apk/`.

## Running Tests

```bash
./gradlew test
```

Tests use JUnit 4 and [Robolectric](http://robolectric.org/) for Android framework classes, plus OkHttp's `MockWebServer` for upload tests. No device or emulator is required.

### Test structure

| Test class | What it covers |
|------------|----------------|
| `MicrobinUploaderTest` | Upload requests, multipart form construction, redirect handling, error cases |
| `UrlValidationTest` | URL normalization, scheme validation, whitespace trimming |
| `PreferencesHelperTest` | Default config, stored preference reading, URL normalization at load time |
| `ShareReceiverActivityTest` | Intent handling for `ACTION_SEND` and `ACTION_SEND_MULTIPLE` |

## Project Architecture

```
app/src/main/java/com/microbinsink/app/
├── MicrobinSinkApplication.kt       # Application class
├── SettingsActivity.kt              # Hosts the settings UI
├── SettingsFragment.kt              # Preference screen with URL validation
├── ShareReceiverActivity.kt         # Receives share intents, triggers uploads
├── upload/
│   ├── MicrobinUploader.kt          # OkHttp-based upload to MicroBin /upload endpoint
│   ├── UploadConfig.kt              # Data class for upload parameters
│   └── UploadResult.kt              # Sealed class: Success | Error
└── util/
    └── PreferencesHelper.kt         # SharedPreferences wrapper
```

### How a share works

1. User shares content from another app
2. Android routes the intent to `ShareReceiverActivity` (registered for `ACTION_SEND` / `ACTION_SEND_MULTIPLE`)
3. `ShareReceiverActivity` reads the shared text or file URIs
4. `PreferencesHelper` loads the current settings (server URL, privacy, expiration, etc.)
5. `MicrobinUploader` POSTs a multipart form to `<server>/upload`
6. MicroBin responds with a 302/303 redirect to the new paste URL
7. The paste URL is shown to the user and copied to the clipboard

## CI/CD

Two GitHub Actions workflows live in `.github/workflows/`:

### `build.yml` — Pull request / branch builds

- Triggers on pushes to `claude/**` branches and manual dispatch
- Runs unit tests, Android Lint, and builds a debug APK
- Uploads lint report and debug APK as artifacts

### `release.yml` — Production releases

- Triggers on pushes to `main`/`master`, version tags (`v*`), and manual dispatch
- Builds a signed release APK
- Creates a GitHub Release with the APK attached

## Release Signing

Release builds require four environment variables (set as GitHub repository secrets for CI):

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded `.keystore` / `.jks` file |
| `KEYSTORE_PASSWORD` | Password for the keystore |
| `KEY_ALIAS` | Alias of the signing key |
| `KEY_PASSWORD` | Password for the signing key |

For local release builds, export these as environment variables before running `./gradlew assembleRelease`.

## Code Style

- **Language:** Kotlin
- **Min SDK:** 28 (Android 9)
- **Java compatibility:** 11
- **Architecture:** Single-activity settings + transparent share-receiver activity
- **Networking:** OkHttp (no Retrofit) with Kotlin coroutines for async work
- **UI:** AndroidX + Material Components, ViewBinding for layouts, Preference library for settings

## Submitting Changes

1. Fork the repository and create a feature branch
2. Make your changes
3. Ensure tests pass: `./gradlew test`
4. Run lint: `./gradlew lint`
5. Open a pull request with a clear description of the change
