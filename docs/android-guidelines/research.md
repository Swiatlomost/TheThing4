# Android Best Practices Research

## Key Sources
- [Android Developers Guides](https://developer.android.com/): official recommendations on architecture, testing, performance.
- [Now in Android](https://developer.android.com/series/now-in-android): showcases modern Jetpack patterns, MAD scorecards.
- [Guide to app architecture](https://developer.android.com/topic/architecture): MVVM + unidirectional data flow, separation of concerns.
- [Now in Android sample app](https://github.com/android/nowinandroid): reference implementation with modular Clean architecture.
- [Modern Android Development (MAD) Skills](https://developer.android.com/jetpack/compose/madskills): videos/articles covering Compose, WorkManager, Navigation, etc.
- Community references: Google I/O talks (2023-2024), articles on modularization (CashApp, Uber), testing strategies (Google Testing Blog).

## Architecture Highlights
- **Layered + MVVM + Clean principles**: UI layer (Compose/Views) -> ViewModel -> Use Cases -> Repository -> Data sources.
- **Unidirectional data flow** with immutable UI state, coroutines/Flow for async streams.
- **Dependency Injection**: Hilt (recommended) or Koin; centralised modules, interfaces for testability.
- **Modularization**: split by feature + core libraries; enforce API boundaries with Android Gradle Module conventions.

## Coding Standards
- Kotlin-first (coroutines, Flow, sealed classes).
- Use Jetpack components (ViewModel, LiveData/StateFlow, Navigation, WorkManager).
- Follow Kotlin style guide (naming, immutability, null-safety).
- Resource naming conventions (snake_case), theming via Material 3.

## Testing & Quality
- Unit tests with JUnit + kotlinx.coroutines-test.
- Instrumentation tests: Espresso / Compose Testing, use Hilt testing utilities.
- Snapshot + contract tests for modules, baseline profiles for performance.
- CI/CD: Gradle tasks (lint, detekt, ktlint, unit test, instrumentation test), GitHub Actions example workflows.

## Tooling & Automation
- Static analysis: Detekt/Ktlint, Android Lint baseline.
- Code formatting via Spotless or ktlint.
- Dependency updates tracked with Gradle Versions Plugin / Renovate.
- Performance monitoring: Baseline Profiles, Macrobenchmark, Crashlytics.

## Risks & Considerations
- Over-modularization increases build time -> follow feature + core module split.
- Compose adoption requires additional testing strategy (Compose UI tests).
- Ensure DI scope definitions to avoid memory leaks.
- Keep documentation living (Nyx updates memory + references).

## Deliverables for Team
1. Draft architecture spec (Vireal).
2. Implementation checklist (Lumen).
3. Memory update + links (Nyx).
4. Training notes in Scribe chronicle.
