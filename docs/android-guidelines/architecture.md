# Android Architecture Draft (v0.1)

## Architecture Goals
- Skalowalna, testowalna aplikacja zgodna z Modern Android Development (MAD).
- Jasny podział warstw i modułów: UI -> Domain -> Data.
- Łatwe rozszerzanie funkcji, szybkie testowanie oraz CI-friendly.

## Warstwy
1. **UI Layer**
   - Technologia: Jetpack Compose (preferowane) lub XML + ViewBinding.
   - Zasady: unidirectional data flow, immutable UI state, event intents.
   - Komponenty: @Composable, StateFlow/LiveData obserwowane przez Compose.
2. **Presentation Layer**
   - ViewModel (Jetpack) + Use Cases/Interactors.
   - ViewModel odpowiada za mapowanie danych domenowych na UI state oraz obsługę eventów.
   - Use Cases (opcjonalne) enkapsulują logikę biznesową, ułatwiają reuse/test.
3. **Domain Layer**
   - Modele domenowe (immutable data classes) + interfejsy repozytoriów.
   - Bez zależności od Android SDK.
4. **Data Layer**
   - Implementacje repozytoriów; źródła danych (REST API, lokalna baza Room, DataStore).
   - Mapowanie DTO <-> domain models.
   - Obsługa błędów i retry (Result/Either, sealed classes).

## Moduły Gradle (przykład)
- pp: aplikacja, DI entry point, nawigacja.
- core/designsystem: theming, reusable UI components.
- core/common: helpery, Result wrappers, coroutine dispatchers.
- eature/<name>: moduły funkcjonalne (ui + domain + data) izolowane.
- data/<source> (opcjonalnie) dla współdzielonych integracji.

## Dependency Injection
- Hilt jako domyślne DI.
- Definicje modułów: @Module + @InstallIn(SingletonComponent::class) dla repo, @ViewModelScoped dla use case.
- Dla testów: @HiltAndroidTest, alternatywne moduły w folderze ndroidTest.

## Kanony Kodowania
- Kotlin conventions (naming, visibility). Wymuszać poprzez ktlint/detekt.
- Asynchroniczność przez coroutines + Flow.
- Error handling: sealed Result (Success, Error, Loading) + UI state machine.
- Resource naming: eature_screen_state, ic_feature_action.

## Testing Strategy
- Unit tests: JUnit + MockK/kotlinx.coroutines-test dla ViewModel/UseCase/Repository.
- Instrumentation: Espresso lub Compose UI Test + Hilt testing.
- Integration tests dla modułów (Robolectric jeśli potrzebne).
- Baseline Profile generation (Macrobenchmark) dla performance-critical flows.

## CI/CD Pipeline (podgląd)
1. ./gradlew lint detekt ktlintCheck.
2. ./gradlew test (unit tests) z raportami JUnit.
3. ./gradlew connectedDebugAndroidTest na emulatorze.
4. Generowanie artefaktów (APK/AAB) + upload do Firebase App Distribution (opcjonalnie).

## Dokumentacja i ADR
- Tworzyć ADR-y w docs/android-guidelines/adr/ przy kluczowych decyzjach.
- Nyx aktualizuje memory.json z linkami do dokumentów.
- Scribe loguje najważniejsze decyzje w kronice.

## Kolejne Kroki
- Zatwierdzić plan DI i modularizacji (Orin/Team).
- Lumen przygotowuje checklisty implementacyjne.
- Nyx aktualizuje pamięć i status board.
