# Morfogeneza - Notatka Restartowa (2025-10-25)

## Zakonczone kroki
- Dodano modul `feature:morphogenesis` z ekranem Morfogenezy, bazujacym na danych `CosLifecycleEngine`.
- Naglowek ekranow: przycisk nawigacyjny w cyklu (`CosLifecycleScreen`) + pasek statusu (Lv / Cells / Active form) z dropdownem zapisanych form.
- Testy: `./gradlew test` oraz `./gradlew connectedDebugAndroidTest` (Pixel_5) - PASS. Potwierdzone w logu Lumena i Orina.
- Kanal `forma_aktywna` (SharedFlow + fallback broadcast) wdrozony przez Nodusa, opisany w ADR.
- ADR-2025-10-25 (Morfogeneza) posiada status `Accepted`, guard rails scalone z notatki Echo.

## Aktualne zadania (2025-10-25)
- **LUMEN-20251025-004** - menu poziomu i licznik komorek (UX header). Zaleznosc: realny stan `CosLifecycleEngine` + guard rails Echo.
- **LUMEN-20251025-005** - edytor komorek + zapisy form (manipulacje, walidacja kolizji, suwak rozmiaru).
- **ECHO-20251025-002** - research UX Kontynuacja: dostarczyc wklad do guard rails (juz czesc jest w ADR).
- **MIRA-20251025-001** - aktualizowanie briefu Morfogenezy.
- **KAI-20251025-003** - rozszerzyc plan testow (guard rails + event SharedFlow/broadcast).
- **NODUS-20251025-002** - zakonczony; checklisty adb/logcat do dopiecia (TODO).
- **Orin** - monitoruje realizacje 003005, Kai (`forma_aktywna`), Nodus (checklisty).

## Co zostalo w LUMEN-20251025-003
- Zadanie oznaczone jako `done`. Raport testow wpisany w logu Lumena (zwroc uwage na czyste logi - brak polskich znakow w logu, do poprawienia przy najblizszej edycji).

## Kolejne kroki po wznowieniu
1. Lumen: rozpoczac `LUMEN-20251025-004`
   - UI: waski pasek statusu -> dodac dodatkowe elementy (alerty przy niskim limicie beda w 004).
   - Po etapie: `./gradlew test` + `./gradlew connectedDebugAndroidTest` (Pixel_5) i raport w logu + Orin.
2. Nodus: uzupelnic checklisty adb/logcat i potwierdzic z Lumenem oraz Kai, ze event `forma_aktywna` dociera i loguje sie (logcat, telemetry placeholder).
3. Kai: rozbudowac `KAI-20251025-003` (test plan) o scenariusze guard rails + SharedFlow/broadcast.
4. Storywright: po aktualizacjach potwierdzic brief w `sessions/ORIN-20251025-002-morfogeneza-brief.md`.

## Przydatne pliki
- `docs/architecture/ADR-2025-10-25-morfogeneza.md` - decyzje, kanal `forma_aktywna`, guard rails.
- `docs/ux/morphogeneza-ux-research.md` - referencje UX + wskazowki (Echo).
- `feature/morphogenesis/src/main/java/...` - aktualny stan UI i ViewModel.
- `agents/lumen/log.md`, `agents/orin/log.md` - logi z ostatniej iteracji.

## Komendy startowe
```
./gradlew test
./gradlew connectedDebugAndroidTest    # Pixel_5
```

## Jak wznowić sesję
1. Otworz plik `notes/restart-brief.md` i przejrzyj sekcje Aktualne zadania".
2. W VS Code (lub innym IDE) ustaw stały kontekst dla asystenta:
   ```
   Proszę przeczytać notes/restart-brief.md, PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, AI_GUIDE.md, MEMORY_SPEC.md. Zostań w tym kontekście dopóki nie powiem inaczej.
   ```
3. Uruchom komendy sanity (opcjonalnie, jesli cache byl czyszczony):
   ```
   ./gradlew test
   ./gradlew connectedDebugAndroidTest    # Pixel_5
   ```
4. Kontynuuj od listy w Kolejne kroki po wznowieniu". W razie watpliwosci popros odpowiedniego agenta (`[AGENT::LUMEN]`, `[AGENT::NODUS]`, `[AGENT::KAI]`) zgodnie z opisami w `AGENTS.md`.

## TODO przed kolejna sesja
- [ ] Sprawdzic polskie znaki w logach (konwersja plikow na UTF-8).
- [ ] Przygotowac checklisty Nodus (adb/logcat).
- [ ] Stworzyc skeleton planu testow w `docs/testing/` dla Morfogenezy (Kai).
