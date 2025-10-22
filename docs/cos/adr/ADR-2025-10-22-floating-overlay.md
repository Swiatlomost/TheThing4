# ADR-2025-10-22 — Floating Overlay Architecture

## Status
Proposed — 2025-10-22 (owner: Vireal)

## Context
- Tryb A wymaga pływającego okna działającego nad innymi aplikacjami (patrz research Echo – `docs/cos/floating-overlay-research.md`).
- Potrzebujemy integracji z istniejącym cyklem życia komórek oraz zapewnienia ciągłości animacji.
- Android 14 wprowadza ograniczenia dla overlay i foreground services – konieczna jest zgodność z politykami.

## Decision
1. **Komponenty**
   - `OverlayController` (Hilt Singleton): zarządza stanem trybu A (start/stop, visibility).
   - `LifecycleOverlayService` (Foreground service):
     - Zawiera `WindowManager` i `ComposeView`.
     - Tworzy kanał notyfikacji `OVERLAY_CHANNEL_ID` (IMPORTANCE_LOW).
     - Deklaruje typ FGS `specialUse`.
   - `OverlayRepository` (DataStore + in-memory) przechowuje pozycję, widoczność i preferencje użytkownika.

2. **Łańcuch danych**
   - Serwis subskrybuje `CellSnapshot` z `CellViewModel` poprzez `CellRepository` (ten sam model co w aplikacji).
   - Pozycja overlay (x,y) jest zapisywana do DataStore po każdym drag-u; przy starcie serwis wczytuje ostatnią wartość.
   - `OverlayState`:
     ```kotlin
     data class OverlayState(
         val isVisible: Boolean,
         val position: Offset,
         val cells: List<CellSnapshot>
     )
     ```

3. **Lifecycle**
   - Użytkownik uruchamia overlay z poziomu aplikacji (toggle).
   - `OverlayController.start()` → sprawdza uprawnienie `Settings.canDrawOverlays`; w razie braku otwiera Intent do ustawień.
   - Po pozytywnej odpowiedzi startuje serwis w trybie foreground (`ContextCompat.startForegroundService`).
   - W momencie otworzenia aplikacji (double tap) serwis zostaje zatrzymany (lub ukryty) w `onResume` `MainActivity`.

4. **UI Compose**
   - `ComposeView` hostuje `OverlayCosLifecycleScreen` (odmiana `CosLifecycleScreen` dostosowana do mniejszego rozmiaru).
   - Gesty:
     - Drag: `pointerInput { detectDragGestures { change, drag -> ... } }` – aktualizacja `WindowManager.LayoutParams`.
     - Double tap: `detectTapGestures(onDoubleTap = { controller.launchApp() })`.
   - Zmniejszona powierzchnia: domyślnie 120dp, skalowanie zależne od gęstości.

5. **Konfiguracja Android**
   - Manifest:
     ```xml
     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
     <service
         android:name=".overlay.LifecycleOverlayService"
         android:exported="false"
         android:foregroundServiceType="specialUse"/>
     ```
   - Dołączamy `OverlayPermissionActivity` prezentującą wyjaśnienie powodów.

6. **Testy**
   - Unit: `OverlayControllerTest` – sprawdzenie logiki start/stop, zapis pozycji.
   - Instrumented: `UiAutomator` – weryfikacja double tap + drag.
   - Manual: Pixel_5 – scenariusze z test planu.

## Consequences
- Wzrost złożoności (service + DataStore), ale zachowujemy spójność z architekturą domenową.
- Foreground service wymaga notyfikacji (widoczna ikona) – trzeba przygotować przyjazny tekst.
- W przyszłości można rozbudować o panel sterowania (długie przytrzymanie).

## Alternatives Considered
- **Bubble API** (Android 11) – odrzucone, bo ogranicza swobodę layoutu i nie spełnia wymagań wizualnych.
- **Accessibility Service** – odrzucone ze względu na restrykcje publikacyjne i nadmiarowe uprawnienia.

## Follow-up
- Lumen: zaimplementować `OverlayController`, serwis + Compose UI.
- Nodus: przygotować konfigurację manifestu, kanał notyfikacji i procedurę żądania uprawnienia.
- Kai: rozszerzyć plan testów (UI automaty + manual overlay).
- Nyx: dopisać heurystyki overlay do pamięci.
- Scribe: udokumentować proces w logu/kronice.
