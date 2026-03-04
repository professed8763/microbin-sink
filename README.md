# MicroBin Sink

An Android app that integrates with the system Share menu to quickly upload text and files to a [MicroBin](https://github.com/szabodanika/microbin) server.

## Features

- **Share anything** — Share text or files from any app directly to your MicroBin instance
- **Multi-file support** — Send multiple files in a single share action
- **Configurable defaults** — Set your preferred privacy level, expiration, and burn-after-read settings
- **Password support** — Works with MicroBin instances that require an uploader password
- **Lightweight** — Minimal permissions (internet only), no background services

## Installation

### Download

Grab the latest APK from the [Releases](../../releases) page.

### Build from source

Requires JDK 17 and Android SDK (API 35).

```bash
git clone https://github.com/professed8763/microbin-sink.git
cd microbin-sink
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Configuration

Open the app and configure your settings:

| Setting | Description | Default |
|---------|-------------|---------|
| **MicroBin URL** | Your MicroBin server address | `https://pub.microbin.eu` |
| **Privacy** | Public, Read-only, Private, or Secret | Public |
| **Expiration** | 1 min, 10 min, 1 hour, 24 hours, 3 days, 1 week, or Never | 1 Hour |
| **Burn After** | Auto-delete after N reads (0 = no limit) | No Limit |
| **Uploader Password** | Required by some MicroBin instances | _(empty)_ |

## Usage

1. Open any app (browser, notes, file manager, etc.)
2. Tap **Share** and select **MicroBin Sink**
3. The content is uploaded and the paste URL is copied to your clipboard

## Requirements

- Android 9 (API 28) or higher
- A running MicroBin server

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, architecture, and how to submit changes.

## License

[MIT](LICENSE)
