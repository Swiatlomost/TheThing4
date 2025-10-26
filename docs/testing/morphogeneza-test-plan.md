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
| MORPH-007 | Wybór zapisanej formy | Zapisz nową formę, wybierz ją z dropdownu/listy zapisów | Podgląd edytora ładuje wybraną formę, editor.formId/ormName odzwierciedlają wybór | Unit + Compose test |
| MORPH-008 | Aktywacja formy (overlay) | Zapisz i aktywuj nową formę | SharedFlow MorphoFormChannel emituje formę, overlay aktualizuje layout, logcat/dumpsys zawiera nowe orma_aktywna | Instrumentacja + Checklista ADB |
| MORPH-009 | Forma bazowa w ekranie cyklu | Po aktywacji zapisanej formy przejdź do ekranu cyklu Cos | Widok cyklu Cos nadal pokazuje bazową formę 0 (stan silnika), brak efektu ubocznego | Manual |

## Automaty
- `feature/morphogenesis/src/test/.../MorphogenesisViewModelTest.kt` - stan domyslny, zapis szkicu i aktywacja formy.
- `feature/morphogenesis/src/androidTest/.../MorphogenesisScreenTest.kt` - weryfikuje wyswietlanie alertu w naglowku (MORPH-UI-001).
- `feature/morphogenesis/src/androidTest/.../MorphogenesisViewModelInstrumentedTest.kt` - sanity eventu forma_aktywna (log + broadcast).

## Manualne sanity
- Uzyj `docs/testing/morphogeneza-event-checklist.md` (ADB + logcat).
- Dodatkowo: manualny scenariusz zapis -> wybór -> aktywacja (sprawdzenie podglądu i overlay), weryfikacja że ekran cyklu nadal pokazuje formę 0.
- 2025-10-26: `adb logcat -d -s MorfoEvent:*` -> `I/MorfoEvent: forma_aktywna formId=FORM-1761479486922...` (Connected tests + checklist PASS; rerun potwierdził brak dodatkowych wpisów (SharedFlow + logcat).).

## Rejestr defektow
- Zglaszaj w `agents/kai/log.md` z prefiksem `DEFECT:` oraz odniesieniem do commitow.

## Raportowanie
- Po iteracji dolacz podsumowanie (PASS/FAIL + defekty) do `agents/kai/log.md`.
- Aktualizuj `agents/status.json` przy gotowych scenariuszach lub blokadach.
