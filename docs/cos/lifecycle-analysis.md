# Coś Life Cycle – Analyst Brief (v0.1)

## 1. Goal
- Zmapować wymagania funkcjonalne dla cyklu życia pojedynczej komórki (narodziny → dojrzewanie → dojrzałość) oraz określić konsekwencje czasowe i animacyjne.
- Dostarczyć wskazówki dla Vireal (ADR), Lumen (implementacja) i Kai (testy) przed rozpoczęciem kodowania.

## 2. Stan repozytorium (2025-10-22)
- Interfejs aplikacji to `MainActivity` z prostym widokiem XML (`activity_main.xml`) – brak obecnie Compose, logiki czasu czy animacji.
- Brak modelu danych dla komórek, brak warstwy prezentacji (ViewModel) i zależności dla animacji (Jetpack Compose Animations, MotionLayout itp.).
- Testy ograniczone do `SmokeTest.kt`, nie ma unit testów ani narzędzi do symulacji czasu.
- Wnioski: cykl życia trzeba projektować od podstaw; rekomendowane przejście na Compose zgodnie z wytycznymi architektury (docs/android-guidelines/architecture.md).

## 3. Model stanu komórki – propozycja
| Stan | Warunek wejścia | Zachowanie | Warunek wyjścia |
| --- | --- | --- | --- |
| `Seed` (kropka) | `creationTimestamp` ustawiony; `ageSeconds < warmup` | Render jako punkt bez konturu; timer startuje od `creationTimestamp`. | `ageSeconds >= warmup` (domyślnie 10 s). |
| `Bud` (okrąg nasycający się) | `warmup ≤ ageSeconds < warmup + saturationDuration` | Kontur stały, wypełnienie animowane od `alpha=0` do `alpha=1`. | `ageSeconds >= warmup + saturationDuration`. |
| `Mature` | `ageSeconds ≥ warmup + saturationDuration` | Pełne nasycenie; komórka gotowa do pączkowania. | Trygery pączkowania lub śmierci (przyszłe rozszerzenia). |

- Parametry domyślne: `warmup = 10s`, `saturationDuration = 12-15s` (do ustalenia przez design – puls powinien być zauważalny, ale spokojny).
- Wspólne dane: `creationTimestamp`, `currentSaturation (0f..1f)`, `lastUpdatedMonotonicMillis`.
- Dla wielu komórek należy utrzymywać listę `CellState` w repozytorium dzielonym przez tryb A/B.

## 4. Czas i synchronizacja
- Wymagane źródło monotoniczne: `SystemClock.elapsedRealtime()` lub `kotlinx.coroutines` `TimeSource.Monotonic`.
- Logika czasu powinna mieszkać w warstwie domenowej (np. use case `ComputeCellState(now: Duration)`).
- Aktualizacje stanu:
  - Tryb aplikacji (foreground): `Ticker`/`Animatable` w Compose lub `Choreographer` callback w XML.
  - Tryb pływający/bckg: `ForegroundService` z coroutine `whileActive { delay(frame); emitState() }`.
- Synchronizacja między trybami: wspólne repo (np. `DataStore` + in-memory cache) aktualizujące `creationTimestamp` i `totalLifetime`.
- Eventy UI nie powinny resetować zegara (nawet przy pauzie aplikacji).

## 5. Animacje i rendering
- Rekomendowana migracja do Jetpack Compose:
  - `Canvas` + `drawCircle` z `Brush.radialGradient` dla efektu nasycania.
  - `Animatable<Float>` kontroluje `saturationProgress`.
  - Puls: `InfiniteTransition` z lekką modulacją promienia (`baseRadius * (1f + 0.03f * sin(t))`).
- Alternatywa (jeśli pozostaniemy przy XML): `MotionLayout` lub niestandardowy `View` z `ValueAnimator`.
- Dla testów instrumentacyjnych Compose pozwoli na łatwiejsze „snapshoty” UI (`captureToImage`).

## 6. Testowalność
- Unit: symulować czas poprzez wstrzyknięty `TimeProvider`. Assert: po 10 s stan = `Bud`, po `warmup + saturation` = `Mature`.
- Instrumented: Compose test z `advanceTimeBy` (Compose animation clock) lub `TestScope.mainClock.advanceTimeBy`.
- Manual (Kai): scenariusz Pixel_5 – obserwacja transformacji po 10 s, walidacja koloru (RGB/Alpha) i promienia.
- Przygotować diagnostykę: log `CellStateDebug(val age: Duration, val saturation: Float)`.

## 7. Ryzyka i zależności
- **Precyzja czasu**: powolne urządzenia mogą dropować klatki → używać czasu rzeczywistego zamiast liczyć klatki.
- **Wielość trybów**: konieczność synchronizacji overlay ↔ aplikacja. Wymaga wczesnej decyzji Nodus/Vireal dot. serwisu.
- **Zasilanie/bateria**: pętla animacji w tle musi być zgodna z ograniczeniami Android 14 (Foreground Service Type `mediaProjection` lub `specialUse`? do analizy).
- **Migracja do Compose**: trzeba wprowadzić Compose BOM, aktualizować gradle, dostosować design system.

## 8. Rekomendacje dla zespołu
- Vireal: przygotować ADR dla `CellLifecycleStateMachine` (wejścia/wyjścia, zależności z repo).
- Lumen: rozpocząć od wprowadzenia Compose + modułu `core/time` z interfejsem `TimeProvider`.
- Kai: zaprojektować testy z wstrzykiwanym zegarem; przygotować checklistę manualną (timeout 15 s).
- Nyx: dodać heurystykę „czas życia liczony monotonicznie, nie resetować po przejściu w tle”.
- Scribe: narracja narodzin – highlight 10 s transformacji i wizualne pulsowanie.

## 9. Źródła / referencje
- Android Developers – [Time and duration best practices](https://developer.android.com/reference/android/os/SystemClock).
- Jetpack Compose – [Animations overview](https://developer.android.com/jetpack/compose/animation).
- Jetpack Compose – [Canvas drawing](https://developer.android.com/develop/ui/compose/graphics/draw/overview).
- Foreground services policy (Android 14) – [documentation](https://developer.android.com/develop/background-work/services/foreground-services).
- Kotlin Coroutines – [TimeSource API](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-time-source/).

