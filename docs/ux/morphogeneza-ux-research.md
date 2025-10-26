# Morfogeneza - Research UX & Guard Rails

**Autor:** Echo (Analyst)  
**Data:** 2025-10-25  
**Powiazane zadania:** `ECHO-20251025-002`, `ORIN-20251025-002`

## 1. Referencje edytorow creature/morph

| Produkt | Co inspiruje | Guard rails / Wnioski |
|---------|--------------|------------------------|
| **Spore Creature Creator** (Maxis, 2008) | Modularne dodawanie elementow na siatce bryl, natychmiastowy podglad animacji | Limit resource points na element; zielone/pomaranczowe highlighty wskazuja poprawne/niepoprawne umieszczenie; slider grubosci konczyn z automatycznym balansowaniem sylwetki |
| **The Sims 4 – Create A Sim** (EA, 2014) | Gesty pull & push na ciele z kontekstowymi hotspotami | Blokada przeciagniecia poza zakres (sprzezynujace odbicie); tooltipy opisujace wplyw na animacje; szybki reset segmentu do presetow |
| **No Man's Sky – Living Ship** (Hello Games, 2020) | Sekwencyjne etapy rozwoju organizmu z wymogiem zasobow | Kazda mutacja wymaga konkretnego surowca i wyswietla ostrzezenie przy brakach; mapa docelowych form pokazuje kierunek |
| **Morphii** (iOS/Android, 2021) | Edycja twarzy poprzez suwaki i gesty pinch/expand | Czytelne suwaki z etykietami granic min/max; blokada nakladania przez natychmiastowe odsunięcie elementu i czerwony outline |
| **Makers Empire 3D** (aplikacja edukacyjna) | Laczy siatke heksagonalna z wolnym rysowaniem | Snap-to-grid redukuje bledy i ulatwia walidacje kolizji |

## 2. Guard rails dla Morfogenezy Cosia

1. **Limity zasobow:** licznik dostepnych komorek maleje natychmiast po dodaniu elementu; undo nie oddaje zasobu (trzeba usunac komorke świadomie).
2. **Nakladanie komorek:**  
   - W tle utrzymujemy mape kolizji (heksy).  
   - Przy naruszeniu: czerwony outline + baner „Komorki nachodza – przesuń lub zmniejsz”.  
   - Opcja „autosort” przenosi kolidujace komorki na najblizsze wolne pozycje.  
3. **Granice organizmu:** outline oraz strefa ostrzegawcza 5%; slider promienia blokuje wartosci poza obrysem (sprzezynujace odbicie).  
4. **Kontrola rozmiaru:** slider zalezy od levelu Cosia (`minRadius(level)`, `maxRadius(level)`); UI pokazuje procent zajetej powierzchni.  
5. **Historia form:** przechowuj co najmniej 5 operacji (undo/redo) i pozwol zapisac szkic bez aktywacji.  
6. **Feedback aktywacji:** toast + log overlay „Forma X aktywowana”; podswietlenie aktywnej formy na liscie.  
7. **Bezpieczenstwo danych:** repozytorium utrzymuje flagi dirty; po powrocie proponuj przywrocenie szkicu.  
8. **Autosort i cofanie:** autosort pokazuje propozycje przesuniecia (podglad + komunikat) i dopiero po potwierdzeniu przenosi komorki na wolne heksy; przycisk undo natychmiast cofa wynik autosortu i innych operacji (dodanie, przesuniecie, zmiana promienia, usuniecie).
9. **Zapisane formy:** wybor formy w dropdownie/listach natychmiast laduje ja do edytora (bez aktywacji) i zaznacza biezacy szkic; aktywacja wysyla forme do overlay (SharedFlow + broadcast).
10. **Forma bazowa:** ekran cyklu Cos zawsze odzwierciedla forme 0; aktywacja zapisanych form zmienia jedynie overlay.

> Uwaga: elementy historii/autosortu pozostaja w backlogu do czasu ponownej decyzji produktowej.

## 3. Pytania follow-up

### Do Vireala
1. Czy potrzebujemy dodatkowej informacji o orientacji/rotacji w modelu `MorphoCell` na przyszle presetowe formy?  
2. Czy autosort powinien byc udokumentowany w ADR jako domyslna heurystyka, czy jako opcja?  
3. Czy overlay wymaga dodatkowych meta-danych przy historii (np. id operacji)?

### Do Lumena
1. Czy kontrolki undo/redo beda w naglowku czy przy panelu edytora?  
2. Jak prezentujemy podglad autosortu (ghost position, highlight)?  
3. Czy utrzymujemy limit historii (np. 10 krokow) i jak sygnalizujemy jego przekroczenie?

### Do Orina (koordynacja)
1. Jak priorytetyzujemy undo/redo vs. kolejne funkcje (np. presety form)?  
2. Czy autosort powinien wymagac potwierdzenia przed zapisem/aktywacja?

## 4. Rekomendacje dla Orina i Lumena

- **Etap 1 (UI scaffold):** przycisk Morfogeneza otwierajacy obrys + menu (level, licznik komorek) z ostrzezeniem 5%.  
- **Etap 2 (Manipulacja):** dodawanie/usuwanie, walidacja kolizji i natychmiastowy feedback.  
- **Etap 3 (Zapis/aktywacja):** panel form (forma 0 + szkice), toast aktywacji, event `ActivateMorphoForm`.  
- **Etap 4 (Historia i autosort - backlog):** kontrolki cofania/przywracania, widoczny stan historii, autosort z podgladem i potwierdzeniem (do wznowienia po decyzji).  
- **Testy uzytkowe:** po etapie 4 zaplanowac min. trzy sesje feedbacku (QA + wlasciciel wizji) korzystajac z checklisty guard rails.

## 5. Nastepne kroki

- [ ] Echo - odnotowac w ADR-2025-10-25, ze undo/redo/autosort sa w backlogu; przygotowac szkic aktualizacji na przyszle wznowienie.  
- [ ] Lumen/Nodus - utrzymac dokumentacje sanity w stanie etapu 006; checklisty undo/redo (`NODUS-20251026-004`) oznaczyc jako oczekujace na wznowienie.  
- [ ] Kai - pozostawic scenariusze undo/redo/autosort jako przyszle notatki w `docs/testing/morphogeneza-test-plan.md`.  
- [ ] Storywright - podkreslic w briefie, ze undo/redo/autosort sa w backlogu i zebrac pytania produktowe na przyszlosc.  
