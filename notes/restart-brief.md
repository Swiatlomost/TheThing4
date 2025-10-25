# Morfogeneza – Notatka Restartowa (2025-10-25)

## Zakończone kroki
- Dodano moduł `feature:morphogenesis` z ekranem Morfogenezy, bazującym na danych `CosLifecycleEngine`.
- Nagłówek ekranów: przycisk nawigacyjny w cyklu (`CosLifecycleScreen`) + pasek statusu (Lv / Cells / Active form) z dropdownem zapisanych form.
- Testy: `./gradlew test` oraz `./gradlew connectedDebugAndroidTest` (Pixel_5) – PASS. Potwierdzone w logu Lumena i Orina.
- Kanał `forma_aktywna` (SharedFlow + fallback broadcast) wdrożony przez Nodusa, opisany w ADR.
- ADR-2025-10-25 (Morfogeneza) posiada status `Accepted`, guard rails scalone z notatki Echo.

## Aktualne zadania (2025-10-25)
- **LUMEN-20251025-004** – menu poziomu i licznik komórek (UX header). Zależność: realny stan `CosLifecycleEngine` + guard rails Echo.
- **LUMEN-20251025-005** – edytor komórek + zapisy form (manipulacje, walidacja kolizji, suwak rozmiaru).
- **ECHO-20251025-002** – research UX Kontynuacja: dostarczyć wkład do guard rails (już część jest w ADR).
- **MIRA-20251025-001** – aktualizowanie briefu Morfogenezy.
- **KAI-20251025-003** – rozszerzyć plan testów (guard rails + event SharedFlow/broadcast).
- **NODUS-20251025-002** – zakończony; checklisty adb/logcat do dopięcia (TODO).
- **Orin** – monitoruje realizację 003→005, Kai (`forma_aktywna`), Nodus (checklisty).

## Co zostało w LUMEN-20251025-003
- Zadanie oznaczone jako `done`. Raport testów wpisany w logu Lumena (zwróć uwagę na czyste logi – brak polskich znaków w logu, do poprawienia przy najbliższej edycji).

## Kolejne kroki po wznowieniu
1. Lumen: rozpocząć `LUMEN-20251025-004`
   - UI: wąski pasek statusu -> dodać dodatkowe elementy (alerty przy niskim limicie będą w 004).
   - Po etapie: `./gradlew test` + `./gradlew connectedDebugAndroidTest` (Pixel_5) i raport w logu + Orin.
2. Nodus: uzupełnić checklisty adb/logcat i potwierdzić z Lumenem oraz Kai, że event `forma_aktywna` dociera i loguje się (logcat, telemetry placeholder).
3. Kai: rozbudować `KAI-20251025-003` (test plan) o scenariusze guard rails + SharedFlow/broadcast.
4. Storywright: po aktualizacjach potwierdzić brief w `sessions/ORIN-20251025-002-morfogeneza-brief.md`.

## Przydatne pliki
- `docs/architecture/ADR-2025-10-25-morfogeneza.md` – decyzje, kanał `forma_aktywna`, guard rails.
- `docs/ux/morphogeneza-ux-research.md` – referencje UX + wskazówki (Echo).
- `feature/morphogenesis/src/main/java/...` – aktualny stan UI i ViewModel.
- `agents/lumen/log.md`, `agents/orin/log.md` – logi z ostatniej iteracji.

## Komendy startowe
```
./gradlew test
./gradlew connectedDebugAndroidTest    # Pixel_5
```

## Jak wznowić sesję
1. Otwórz plik `notes/restart-brief.md` i przejrzyj sekcję „Aktualne zadania”.
2. W VS Code (lub innym IDE) ustaw stały kontekst dla asystenta:
   ```
   Prosze przeczytac notes/restart-brief.md, PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, AI_GUIDE.md, MEMORY_SPEC.md. Zostań w tym kontekście dopóki nie powiem inaczej.
   ```
3. Uruchom komendy sanity (opcjonalnie, jeśli cache był czyszczony):
   ```
   ./gradlew test
   ./gradlew connectedDebugAndroidTest    # Pixel_5
   ```
4. Kontynuuj od listy w „Kolejne kroki po wznowieniu”. W razie wątpliwości poproś odpowiedniego agenta (`[AGENT::LUMEN]`, `[AGENT::NODUS]`, `[AGENT::KAI]`) zgodnie z opisami w `AGENTS.md`.

## TODO przed kolejną sesją
- [ ] Sprawdzić polskie znaki w logach (konwersja plików na UTF-8).
- [ ] Przygotować checklisty Nodus (adb/logcat).
- [ ] Stworzyć skeleton planu testów w `docs/testing/` dla Morfogenezy (Kai).
