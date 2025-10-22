# Test Plan — Tryb Pływający Cośia (Kai, 2025-10-22)

## Cel
Zweryfikować zachowanie pływającego okna (tryb A) obejmujące uruchomienie, interakcje (drag, double tap), zgodę na uprawnienie oraz restart po powrocie do aplikacji.

## Zakres
- `OverlayController`, `OverlayPermissionActivity`.
- `LifecycleOverlayService` (foreground).
- Widok Compose `OverlayCosLifecycleScreen`.

## Scenariusze

### 1. Unit / Integration (moduł JVM)
| ID | Opis | Kroki | Oczekiwany wynik |
| --- | --- | --- | --- |
| UT-OV-001 | Start bez uprawnienia | Wywołaj `OverlayController.start()` przy `Settings.canDrawOverlays=false` (mock) | Otwierane jest `OverlayPermissionActivity` |
| UT-OV-002 | Start z uprawnieniem | Zasymuluj `Settings.canDrawOverlays=true` | Startuje `LifecycleOverlayService` |

### 2. Instrumented
| ID | Opis | Kroki | Oczekiwany wynik |
| --- | --- | --- | --- |
| IT-OV-001 | Drag overlay | Po uruchomieniu trybu A użyj UiAutomator do przesunięcia komponentu | Pozycja overlay zmienia się zgodnie z ruchem, nie opuszcza ekranu |
| IT-OV-002 | Double tap | Double tap → sprawdź, czy `MainActivity` pojawia się w foreground | Overlay znika, aktywność otwiera się |
| IT-OV-003 | Restart overlay | Zamknij aplikację (back) | Overlay wraca w poprzednim miejscu |

### 3. Manual — Pixel_5
| ID | Opis | Kroki | Oczekiwany wynik |
| --- | --- | --- | --- |
| MAN-OV-001 | Uprawnienie | Włącz tryb pływający, przejdź przez ekran zgody | Użytkownik widzi komunikat, po akceptacji overlay startuje |
| MAN-OV-002 | Zachowanie overlay | Przesuń, obserwuj saturację, wykonaj double tap | Animacja identyczna jak w aplikacji, wejście do aplikacji działa |
| MAN-OV-003 | Powrót do trybu A | Zamknij aplikację | Overlay ponownie się wyświetla, pulsuje zgodnie z czasem życia |

## Automatyzacja
- Komenda: `./gradlew installDebug` (instalacja), `./gradlew connectedDebugAndroidTest` (po dodaniu testów UiAutomator).
- W testach UI wykorzystać `UiDevice.getInstance()` oraz `UiSelector().description("Coś – tryb pływający")`.

## Kryteria akceptacji
- Serwis działa jako foreground (widoczne powiadomienie).
- Drag i double tap działają bez błędów.
- Po zamknięciu aplikacji overlay wraca (bez crashy).

## Artefakty
- Log Kai: `agents/kai/log.md`.
- Wyniki manualnych obserwacji (Scribe chronicle + log).
- Dokumentacja: `docs/cos/floating-overlay-research.md`, `docs/cos/adr/ADR-2025-10-22-floating-overlay.md`.
