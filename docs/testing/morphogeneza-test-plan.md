> Plan testow Kai (KAI-20251025-003) dla funkcji Morfogenezy.

# Morfogeneza - Plan testow

## Kontekst
- Agent odpowiedzialny: Kai (`KAI-20251025-003`).
- Zakres: ekran Morfogenezy (`feature:morphogenesis`), emisja eventu `forma_aktywna`, integracja z overlay.
- Referencje: `docs/architecture/ADR-2025-10-25-morfogeneza.md`, `docs/ux/morphogeneza-ux-research.md`, `docs/testing/morphogeneza-event-checklist.md`.

## Metryki sukcesu
- Brak naruszen guard rails (limit komorek, kolizje).
- Overlay odswieza aktywna forme w <1 s od emisji eventu.
- Automaty (`./gradlew test`, `./gradlew connectedDebugAndroidTest`) przechodza bez regresji.

## Scenariusze testowe
| ID | Obszar | Kroki | Oczekiwany wynik | Narzedzie |
|----|--------|-------|------------------|-----------|
| MORPH-001 | Limity komorek | Dodaj komorki do wypelnienia poziomu, sproboj dodac kolejna | UI blokuje dodanie, alert "Zapas komorek wyczerpany" | Manual + Compose preview |
| MORPH-002 | Alert niskiego limitu | Zmniejsz liczbe dostepnych komorek do progu 10% | Alert `Niski zapas komorek: X` | `./gradlew test` (ViewModel) |
| MORPH-003 | Kolizje | Sprobuj umiescic komorki w tej samej pozycji (po wdrozeniu walidacji) | Czerwony outline + komunikat o kolizji | Manual |
| MORPH-004 | Aktywacja formy | Zapisz nowa forme i aktywuj ja | Toast + log `MorfoEvent` + overlay aktualizuje forme | Checklista ADB |
| MORPH-005 | Fallback broadcast | Symuluj brak SharedFlow i aktywuj forme | Broadcast `com.example.cos.FORMA_AKTYWNA` widoczny w `dumpsys` | Checklista ADB |
| MORPH-006 | Regresja UI | `./gradlew connectedDebugAndroidTest` na Pixel_5 | Wszystkie testy przechodza | Instrumentacja |
| MORPH-007 | Undo/redo | Wykonaj sekwencje dodaj -> przesuń -> zmień promień, następnie cofnij i przywróć | Stan płótna i licznik komórek wracają do poprzednich wartości; event `forma_aktywna` nie emituje duplikatów | Unit + Instrumentacja |
| MORPH-008 | Autosort kolizji | Umieść dwie komórki w kolizji i aktywuj autosort | Komórki przesuwają się na najbliższe wolne heksy, brak komunikatu błędu | Manual + ViewModel test |

## Automaty
- `feature/morphogenesis/src/test/.../MorphogenesisViewModelTest.kt` - stan domyslny, zapis szkicu i aktywacja formy.
- `feature/morphogenesis/src/androidTest/.../MorphogenesisScreenTest.kt` - weryfikuje wyswietlanie alertu w naglowku (MORPH-UI-001).
- `feature/morphogenesis/src/androidTest/.../MorphogenesisViewModelInstrumentedTest.kt` - sanity eventu forma_aktywna (log + broadcast).

## Manualne sanity
- Uzyj `docs/testing/morphogeneza-event-checklist.md` (ADB + logcat).
- Dodatkowo: manualny test slidera rozmiaru, undo/redo (co najmniej 5 kroków historii) oraz autosortu.
- 2025-10-26: `adb logcat -d -s MorfoEvent:*` -> `I/MorfoEvent: forma_aktywna formId=FORM-1761479486922...` (Connected tests + checklist PASS; rerun potwierdzil brak dodatkowych wpisow).

## Rejestr defektow
- Zglaszaj w `agents/kai/log.md` z prefiksem `DEFECT:` oraz odniesieniem do commitow.

## Raportowanie
- Po iteracji dolacz podsumowanie (PASS/FAIL + defekty) do `agents/kai/log.md`.
- Aktualizuj `agents/status.json` przy gotowych scenariuszach lub blokadach.
