# PDCA: MORPHO-007-undo-redo

## Plan
- Cel: Wprowadzenie historii operacji (undo/redo) w edytorze form bez regresji overlay.
- Zakres: Akcje dodaj/usuń/skaluj komórkę, aktywacja szkicu, limit pamięci historii, integracja z SharedFlow.
- Zależności: MorphoFormChannel (SharedFlow), hex-canvas, ADR-2025-10-25.

## Do
- Zaprojektuj bufor historii (lista operacji z kompaktowaniem).
- Zaimplementuj komendy undo/redo oraz integrację z UI.
- Zsynchronizuj eventy z overlay (SharedFlow + fallback broadcast).

## Check
- Compose testy operacji na kanwie (Kai).
- Sanity logcat/dumpsys dla eventów forma_aktywna po undo/redo (Nodus + Kai).
- Brak dryfu wizualnego promieni.

## Act
- Korekty wydajności i UX (np. limit głębokości historii).
- Aktualizacja ADR jeśli zmieni się model danych.

