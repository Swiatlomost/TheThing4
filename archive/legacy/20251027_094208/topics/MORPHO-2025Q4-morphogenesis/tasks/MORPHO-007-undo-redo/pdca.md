# PDCA: MORPHO-007-undo-redo

## Plan
- Cel: Wprowadzenie historii operacji (undo/redo) w edytorze form bez regresji overlay.
- Zakres: Akcje dodaj/usun/skaluj komorke, aktywacja szkicu, limit pamieci historii, integracja z SharedFlow.
- Zaleznosci: MorphoFormChannel (SharedFlow), hex-canvas, ADR-2025-10-25.

## Do
- Zaprojektuj bufor historii (lista operacji z kompaktowaniem).
- Zaimplementuj komendy undo/redo oraz integracje z UI.
- Zsynchronizuj eventy z overlay (SharedFlow + fallback broadcast).

## Check
- Compose testy operacji na kanwie (Kai).
- Sanity logcat/dumpsys dla eventow forma_aktywna po undo/redo (Nodus + Kai).
- Brak dryfu wizualnego promieni.

## Act
- Korekty wydajnosci i UX (np. limit glebokosci historii).
- Aktualizacja ADR jesli zmieni sie model danych.

