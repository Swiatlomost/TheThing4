# Test Plan — Cykl Życia Cośia (Kai, 2025-10-22)

## Cel
Zapewnić, że bazowy cykl życia pojedynczej komórki (Seed → Bud → Mature) działa identycznie w komponentach domenowych, UI Compose oraz podczas obserwacji manualnej na emulatorze Pixel_5.

## Zakres
- Pakiet domenowy `core/cell` i `core/time`.
- Warstwa prezentacji (`CellViewModel`, `CosLifecycleScreen`).
- Główny ekosystem Compose (MainActivity).
- Smoke testy jednostkowe + manualne.

## Scenariusze testowe

### 1. Unit — State Machine
| ID | Opis | Kroki | Oczekiwany wynik |
| --- | --- | --- | --- |
| UT-CL-001 | Przejście Seed → Bud | Ustaw `FakeTimeProvider` na 5 s; wywołaj `evaluate` | Zwraca `CellStage.Seed` z `progress ≈ 0.5` |
| UT-CL-002 | Wejście w Bud | Ustaw czas na `warmup + 3 s` | Zwraca `CellStage.Bud` z `progress ≈ 0.2` |
| UT-CL-003 | Dojrzałość | Ustaw czas na `warmup + saturation + 1 s` | Zwraca `CellStage.Mature` |

Implementacja: `app/src/test/java/.../CellLifecycleStateMachineTest.kt`

### 2. UI — Compose Preview / Instrumented (TODO po paczkowaniu)
- UI-CL-001 (Preview sanity): uruchom `CosLifecyclePreview`, wizualna walidacja konturu + wypełnienia ≈ 50%.
- UI-CL-002 (Instrumented TODO): Po dodaniu repozytorium, przygotować Compose UI test wykorzystujący `mainClock.advanceTimeBy` w celu odtworzenia 10 s i weryfikacji labeli.

### 3. Manual — Pixel_5
| ID | Opis | Kroki | Oczekiwany wynik |
| --- | --- | --- | --- |
| MAN-CL-001 | Narodziny | Uruchom apkę, obserwuj 0–10 s | Punkt bez konturu, puls subtelny, label „Narodziny (xx%)” |
| MAN-CL-002 | Dojrzewanie | 10–25 s | Okrąg z rosnącym wypełnieniem, label „Dojrzewanie (xx%)” |
| MAN-CL-003 | Dojrzałość | >25 s | Pełny kolor, label „Dojrzałość”, puls zachowany |

**Wynik (2025-10-22, Pixel_5 AVD):** MAN-CL-001..003 PASS — w fazie Seed pojawia się mała kropka, następnie pusty kontur z rosnącym wypełnieniem i finalne pełne nasycenie.

Notatki manualne: zapisać czasy i ewentualne opóźnienia (Kai log).

## Automatyzacja
- Komenda podstawowa: `./gradlew test` (pokrywa UT-CL-001..003).
- Po wprowadzeniu instrumented testów: `./gradlew connectedDebugAndroidTest`.

## Kryteria akceptacji
- Wszystkie testy jednostkowe przechodzą.
- Manualna obserwacja na Pixel_5 potwierdza transformację w wymaganych progach czasowych.
- Brak regresji w SmokeTeście (`SmokeTest.kt`).

## Artefakty
- Log Kai: `agents/kai/log.md` (sekcja 20251022-001).
- Wyniki `./gradlew test` — log konsoli.
- Manualne spostrzeżenia (dopisać po testach na emulatorze).
