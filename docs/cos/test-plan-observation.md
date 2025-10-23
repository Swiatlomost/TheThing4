# Test Plan  Tryb obserwacji Cosia (Kai, 2025-10-23)

## Cel
Zabezpieczyc jakosc wydanego trybu obserwacji: grupowe przesuwanie organizmu, gating paczkowania, timeline saturacji oraz komunikaty dostepnosci.

## Zakres
- ViewModel i modele: `ObservationViewModel`, `ObservationUiState`, `ObservationGestures`, `ObservationRepository`.
- UI Compose: `ObservationModeScreen`, `ObservationCanvas`, `SaturationTimelineRail`, `ActionRow` z gatingiem.
- Integracja w `MainActivity` (nawigacja do Observation Mode).

## Srodowisko
- Pixel_5 API 34 (emulator, TalkBack aktywny przy testach dostepnosci).
- `./gradlew test` (JVM), `./gradlew connectedDebugAndroidTest` (Android)  brak dedykowanych testow Compose UI, plan do wdrozenia.
- Build variant: `debug`.

## Scenariusze

### 1. Unit / Integration (JVM)
| ID | Opis | Kroki | Oczekiwany wynik | Status |
| --- | --- | --- | --- | --- |
| UT-OBS-001 | Readiness liczy dojrzae komorki | Wstrzyknij `CellLifecycleStateMachine` z trzema komorkami (2 Mature) | `GatingReadiness.status == Ready`, `matureCount == 2` | PASS (`ObservationViewModelTest.readinessReflectsMatureCells`) |
| UT-OBS-002 | Cooldown po paczkowaniu | Wywoaj `onBuddingRequested()`, zasymuluj upyw 5 s | Status `Cooldown`, po 5 s powrot do `Ready` | PASS (`ObservationViewModelTest.buddingTriggersCooldown`) |
| UT-OBS-003 | Persist drag transform | Zasymuluj drag (`DragDelta(x=24f,y=-16f)`), sprawdz repo fake | `ObservationRepository.persistTransform` z nowym offsetem | PASS (`ObservationViewModelTest.dragPersistsTransform`) |

### 2. Instrumented (TODO)
| ID | Opis | Kroki | Oczekiwany wynik | Status |
| --- | --- | --- | --- | --- |
| IT-OBS-001 | Render UI i drag | Uruchom tryb obserwacji, wykonaj Compose test `performTouchInput { swipe(...) }` | Wszystkie komorki przesuwaja sie razem, `ObservationRepository` zapisuje transformacje | TODO (zaplanowane sprint +1) |
| IT-OBS-002 | Readiness Pill z TalkBack | Wacz TalkBack, odczytaj tresc `ReadinessPill` | Czytnik odczytuje status i komunikat (Ready/NotReady/Cooldown) | TODO |
| IT-OBS-003 | Timeline dostepnosc | Przewin timeline, sprawdz focusable chips | Chipsy dostepne sekwencyjnie, opisuja etap saturacji | TODO |

### 3. Manual (Pixel_5)
| ID | Opis | Kroki | Oczekiwany wynik | Status |
| --- | --- | --- | --- | --- |
| MAN-OBS-001 | Wejscie do obserwacji | Z `MainActivity` kliknij `Enter Observation`, wroc przyciskiem Back | Ekran obserwacji wacza sie, powrot przywraca tryb podstawowy | PASS (2025-10-23) |
| MAN-OBS-002 | Grupowy drag | Przytrzymaj organizm i przesun w lewo/gore | Wszystkie komorki przesuwaja sie spojnie, transformacja zapamietana po powrocie | PASS |
| MAN-OBS-003 | Gating zmiana stanu | Obserwuj dojrzewanie (min. 1 komorka Mature), aktywuj `Trigger budding`, odczekaj cooldown | Przycisk aktywny gdy Ready, po kliknieciu status `Cooling down`, po 5 s powrot do `Ready` | PASS |
| MAN-OBS-004 | Hints i timeline | Wywoaj drag bez wczesniejszego ruchu, podejrzyj timeline | Wyswietla sie podpowiedz drag, timeline aktualizuje saturacje | PASS |

## Automatyzacja
- `./gradlew test`  uruchamia `ObservationViewModelTest` (UT-OBS-001..003).
- `./gradlew connectedDebugAndroidTest`  obecnie tylko SmokeTest; dodanie `ObservationModeTest` planowane (Kai/Nodus).
- Docelowo Compose testy instrumentacyjne przy pomocy `createAndroidComposeRule`.

## Kryteria akceptacji
- Wszystkie scenariusze UT oraz MAN maja status PASS.
- IT scenariusze z flaga TODO posiadaja plan wdrozenia (ticket Kai-Nodus).
- Po release: brak regresji w `MainActivity`, przejscie do observation nie powoduje crashy ani utraty stanu.

## Wyniki / Artefakty
- Wynik unit: `app/src/test/java/com/example/thething4/observation/ObservationViewModelTest.kt`.
- Manual run sheet: zapis w `agents/scribe/log.md` (Entry #10) oraz kronika.
- Ten dokument + log Kai (sekcja KAI-20251022-003) suza jako dowod zakonczenia testow.

## Ryzyka i TODO
- Brak automatycznych testow Compose  zwiekszone ryzyko regresji UI.
- TalkBack / accessibility wymagaja walidacji instrumentacyjnej.
- Nalezy zsynchronizowac transformacje z przyszym trybem edycji (podatne na race conditions).

