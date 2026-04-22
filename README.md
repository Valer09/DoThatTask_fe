# DoThatTask — Frontend

Client for the **DoThatTask** task-management app, built with **Kotlin Multiplatform** and **Compose Multiplatform** from a single shared codebase. Targets **Android**, **iOS**, **Desktop (JVM)** and **Web (WASM)**, talking to the Ktor backend through a shared HTTP client.

## Learning goal

Explore **Kotlin Multiplatform** and **Compose Multiplatform** — sharing UI, models and networking across mobile, desktop and web, and understanding platform-specific source sets (`commonMain`, `androidMain`, `iosMain`, `jvmMain`, `wasmJsMain`).

## Tech stack

- **Language:** Kotlin (Multiplatform)
- **UI:** Compose Multiplatform
- **Networking:** Ktor Client
- **Targets:** Android, iOS, Desktop (JVM), Web (Wasm/JS)
- **Build / Deploy:** Gradle (Kotlin DSL), Docker

## Project structure

```
composeApp/src/
├── commonMain/            # Shared UI, models, networking, view logic
│   └── kotlin/.../
│       ├── Model/         # Task, User, AppState, AuthState
│       ├── Network/       # HttpClientManager, TaskApi, AuthProvider
│       └── View/          # Screens, Components, navigation
├── androidMain/           # Android entry point
├── iosMain/               # iOS-specific code
├── jvmMain/               # Desktop entry point
└── wasmJsMain/            # Web entry point
iosApp/                    # Xcode project wrapping the shared UI
```

## Build & run

- **Desktop:** `./gradlew :composeApp:run`
- **Android:** `./gradlew :composeApp:assembleDebug`
- **Web (Wasm):** `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode and run on simulator/device.

## Companion repository

- Ktor backend: [DoThatTask_be](https://github.com/Valer09/DoThatTask_be)
