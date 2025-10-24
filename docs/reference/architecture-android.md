# Android Architecture Reference

## Goals
- Follow Modern Android Development (MAD) with layered, testable modules.
- Keep separation of concerns: UI -> Presentation -> Domain -> Data.
- Support incremental feature modules and clear DI boundaries.

## Layer responsibilities
1. **UI layer**
   - Jetpack Compose preferred (Views acceptable when needed).
   - Unidirectional data flow, immutable UI state, events as intents.
   - Observe `StateFlow`/`LiveData` from ViewModel.
2. **Presentation layer**
   - ViewModel + optional Use Cases/Interactors.
   - ViewModel maps domain models to UI state and handles user events.
   - Use Case encapsulates business rules for reuse and testing.
3. **Domain layer**
   - Pure Kotlin data classes + repository interfaces.
   - No Android SDK dependencies.
4. **Data layer**
   - Repository implementations, data sources (REST, Room, DataStore).
   - DTO <-> domain mapping, error handling with `Result`/sealed classes.

## Module layout
- `app`: navigation shell, DI entry point.
- `core/designsystem`: theming, shared Compose components.
- `core/common`: utilities, coroutine dispatchers, Result helpers.
- `feature/<name>`: isolates UI + domain + data for a feature.
- Optional `data/<source>` modules for shared integrations.

## Dependency injection
- Use Hilt. Modules annotated with `@InstallIn(SingletonComponent::class)` for repositories, `@ViewModelScoped` for use cases.
- Separate test bindings with Hilt test modules.

## Coding standards
- Kotlin-first: coroutines, Flow, sealed hierarchies.
- Error handling via `Result`/sealed state machine.
- Enforce ktlint/detekt, keep resource naming consistent (`snake_case`).

## Testing & CI
- Unit: JUnit + kotlinx.coroutines-test (ViewModel/Use Case/Repository).
- Instrumented: Compose UI test or Espresso as needed; use Hilt testing utilities.
- Integration/Macro: baseline profiles for perf critical flows.
- CI pipeline: `lint`, `detekt`, `ktlintCheck`, `test`, `connectedDebugAndroidTest`, artifact build.

## Implementation checklist highlights
- Document deviations with ADRs before coding.
- Ensure ViewModel + UI state and unidirectional flow before UI work.
- Run static analysis/tests locally prior to PR.
- Update documentation + agent memories (Nyx/Scribe) after change.
