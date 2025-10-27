# Overlay — Architektura i Wskazówki

- Cel: parytet stanu i gestów względem aplikacji (double tap, lifecycle/saved state).
- Serwis: `LifecycleOverlayService` — renderuje stan i nasłuchuje na kanałach zdarzeń.
- Testy: `connectedDebugAndroidTest` + sanity logcat (brak crashy, stabilny lifecycle).

Najlepsze praktyki:
- Utrzymuj minimalny interfejs wejścia (gesty/zdarzenia) i wspólną logikę stanu.
- Loguj zdarzenia z prefiksem (`MorfoEvent`) dla ułatwienia sanity.

