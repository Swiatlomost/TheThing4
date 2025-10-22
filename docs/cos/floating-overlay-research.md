# Floating Overlay Research (Echo, 2025-10-22)

## 1. Cel
- Określić wymagania platformowe i UX dla trybu A (pływające okno Cośia) na Androidzie 14.
- Przygotować wytyczne dla Vireal (ADR), Lumen (implementacja), Nodus (integracja) i Kai (testy).

## 2. Uprawnienia i ograniczenia
- **SYSTEM_ALERT_WINDOW** – wymagane do wyświetlania `TYPE_APPLICATION_OVERLAY`; od Android 10 instalator musi pokazać użytkownikowi ekran ustawień systemowych (`ACTION_MANAGE_OVERLAY_PERMISSION`).
- **Foreground service** – Android 14 wymaga jawnego zadeklarowania typu (`mediaProjection`, `specialUse`, etc.). Dla pływającego elementu UI rekomendowany typ `specialUse` + uzasadnienie w dokumentacji (Vireal/Nodus).
- **Battery optimizations** – długotrwałe animacje muszą respektować `doze`/`app standby`. Wyłączanie optymalizacji tylko w wyjątkowych sytuacjach (nie planujemy).

## 3. Rekomendowana architektura
- **Serwis nakładki** (`LifecycleOverlayService`):
  - Foreground service z własnym kanałem notyfikacji (`IMPORTANCE_LOW`).
  - Zarządza `WindowManager` + `TYPE_APPLICATION_OVERLAY`.
  - Komunikuje się z głównym repozytorium stanu (`CellRepository`) przez `StateFlow`.
- **Compose in overlay**:
  - Możliwość użycia `ComposeView` osadzonego w `FrameLayout`. Wymaga `setContent { ... }` analogicznie do Activity.
  - Animacje minimalne (puls) z ograniczeniem FPS (`LaunchedEffect` + `delay(32)`).
- **Wejście/wyjście do aplikacji**:
  - Podwójne stuknięcie w overlay wywołuje `startActivity(MainActivity)` z flagą `FLAG_ACTIVITY_NEW_TASK`.
  - Serwis utrzymuje `isOverlayVisible`; wchodząc do aplikacji – pauzuje/ukrywa overlay.

## 4. Gesty i UX
- **Przesuwanie**: `detectDragGestures` → aktualizacja `WindowManager.LayoutParams.x/y`. Dodać małe ograniczenia (stay in screen bounds).
- **Dotyk**: pojedynczy dotyk = brak akcji (tylko drobna animacja). Podwójne tapnięcie = wejście do aplikacji. Długie przytrzymanie (przyszłość) – menu.
- **Widoczność**: overlay półprzezroczysty w stanie Seed/Bud, w trybie Mature wypełnienie ~70% przezroczystości, by nie zasłaniał UI.
- **Accessibility**: zapewnić `contentDescription` + integrację z TalkBack (komunikat "Coś – dotknij dwukrotnie, aby edytować").

## 5. Integracja danych
- Overlay subskrybuje te same `CellSnapshot` co tryb B.
- Czas i saturacja kontynuują się dzięki `TimeProvider` w serwisie (dzielenie przez `SingletonComponent` – patrz ADR).
- Dodatkowe pole `overlayPosition` w repo (persist via DataStore) – umożliwia zapamiętanie ostatniego miejsca.

## 6. Testy i weryfikacja
- **Automatyczne**: UI test z `UiAutomator` (Kai) – sprawdzenie obecności overlay i reakcji na gest przesunięcia/double tap.
- **Manualne**: Pixel_5 – scenariusze:
  1. Włączyć tryb A z ustawień aplikacji.
  2. Zweryfikować, że overlay startuje z półprzezroczystą kropką (Seed).
  3. Przesuń w różne obszary ekranu (pozostaje w obrębie).
  4. Double tap → aplikacja otwiera się, overlay znika.
  5. Zamknąć aplikację → overlay wraca w poprzednim miejscu.

## 7. Referencje
- Android Developers: [Draw over other apps](https://developer.android.com/develop/ui/views/overlays) (SYSTEM_ALERT_WINDOW).
- Android 14 Changes: [Foreground service types](https://developer.android.com/about/versions/14/behavior-changes-all#fgs-types).
- Google Material: [Bubble / floating assistive UI guidelines](https://m3.material.io/components/bottom-sheets/overview) – inspiracje dot. interakcji.

