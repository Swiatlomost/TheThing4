# Morfogeneza - Research UX & Guard Rails

**Autor:** Echo (Analyst)  
**Data:** 2025-10-25  
**Powiązane zadania:** `ECHO-20251025-002`, `ORIN-20251025-002`

## 1. Referencje Edytorów „Creature/Morph”

| Produkt | Co inspiruje | Guard rails / Wnioski |
|---------|--------------|------------------------|
| **Spore Creature Creator** (Maxis, 2008) | Modularne dodawanie elementów na siatce brył, natychmiastowy podgląd animacji | Limit resource points przy każdej części; zielone/pomarańczowe highlighty pokazujące poprawne/niepoprawne umieszczenie; slider grubości kończyn z automatycznym balansowaniem sylwetki |
| **The Sims 4 – Create A Sim** (EA, 2014) | Bezsiatkowe „pull & push” na ciele z kontekstowymi hotspotami | Blokada przeciągnięć powyżej zakresu (sprężynujące odbicie); tooltipy opisujące wpływ na animację; szybkie resetowanie segmentu do presetów |
| **No Man’s Sky – Void Egg/Living Ship Growth** (Hello Games, 2020) | Sekwencyjne etapy rozwoju organizmu z wymogiem zasobów | Każda mutacja wymaga konkretnego surowca i wyświetla ostrzeżenie, jeśli brak zasobów; postęp pokazuje mapę docelowych form, by gracz wiedział dokąd zmierza |
| **Morphii** (iOS/Android creativity app, 2021) | Edycja twarzy poprzez suwaki i gesty pinch/expand | Czytelne suwaki z etykietami granic (min/max), blokada nakładania poprzez natychmiastowe odsnappowanie elementu i czerwony outline |
| **Makers Empire 3D** (Edukacyjny edytor 3D) | Uczy dzieci zasad przestrzeni – łączy siatkę z wolnym rysowaniem | Tryb siatki heksagonalnej pozwala łatwo liczyć jednostki; automatyczne „snap to grid” redukuje błędy i ułatwia walidację kolizji |

## 2. Guard Rails dla Morfogenezy Cosia

1. **Limity zasobów:** licznik dostępnych komórek maleje natychmiast po dodaniu elementu; pozwól na „rezerwację” komórki dopiero po zatwierdzeniu (undo nie oddaje zasobu).  
2. **Nakładanie komórek:**  
   - W tle utrzymuj mapę kolizji (prostokąty lub heksy).  
   - Przy naruszeniu: czerwony outline komórek + baner „Komórki nachodzą – przesuń lub zmniejsz”.  
   - Opcja „autosort” rozsuwa komórki do najbliższej wolnej pozycji.  
3. **Granice organizmu:** wizualizuj obrys (outline) oraz strefę ostrzegawczą 5% wewnątrz. Suwak promienia blokuje wartości wykraczające poza obrys (sprężynujące odbicie).  
4. **Kontrola rozmiaru:** slider z zakresem zależnym od levelu Cosia (`minRadius(level)`, `maxRadius(level)`); wyświetlaj liczby i procent zajmowanej powierzchni.  
5. **Historia form:** zachowuj ostatnie 5 operacji (undo/redo) i dawaj możliwość zapisania formy jako szkicu bez aktywacji.  
6. **Feedback aktywacji:** przy zapisie/aktywacji wysyłaj toast + log overlay „Forma X aktywowana” oraz podświetl kod kolorystyczny w panelu zapisanych form.  
7. **Bezpieczeństwo danych:** w repozytorium trzymaj „dirty flag” – jeśli aplikacja zostanie zamknięta przed zapisem, przy ponownym wejściu pokaż modal z pytaniem „Odzyskać szkic?”.  

## 3. Pytania Follow-up

### Do Vireala
1. Jakie płótno preferujemy: siatka heksagonalna (łatwiejsza walidacja) czy wolna przestrzeń z „snap-to-guide”?  
2. Czy `MorphoCell` powinien przechowywać orientację/rotację, jeśli w przyszłości pojawią się komórki kierunkowe?  
3. Czy integracja z overlay powinna odbywać się przez odrębny kanał (AIDL) czy wystarczy współdzielona baza (DataStore) i event LiveData?

### Do Lumena
1. Czy ekran Morfogenezy wykorzysta istniejący Compose canvas, czy potrzebujemy dedykowanej sceny (np. `AndroidView` z rysowaniem custom)?  
2. Jakie komponenty UI planujemy dla suwaków? (Material Slider vs. custom radial dial).  
3. Czy w panelu zapisanych form wyświetlamy miniatury? Jeśli tak, jakie koszty renderowania możemy zaakceptować?

### Do Orina (koordynacja)
1. Jakie priorytety w przyrostach – wejście + menu vs. edytor vs. zapis?  
2. Czy forma aktywna wymaga blokady innych operacji do czasu synchronizacji overlay (np. loading spinner)?  

## 4. Rekomendacje dla Orina i Lumen

- **Etap 1 (UI scaffold):** nowy przycisk Morfogeneza → okno z obrysem + menu (level, komórki). Dodać strefę ostrzegawczą w outline.  
- **Etap 2 (Manipulacja komórek):** implementacja dodawania/usuwania oraz walidacja kolizji z natychmiastowym feedbackiem.  
- **Etap 3 (Zapis i aktywacja):** panel form (forma 0 + szkice), toast aktywacji, event `ActivateMorphoForm`.  
- **UX notatka:** w panelu form trzymaj 1 slot „Szkic w toku” i dopiero po aktywacji przenoś do listy.  
- **Testy użytkowe:** po wdrożeniu Etapu 2 przewidzieć min. 3 sesje feedbacku (QA + właściciel wizji) z checklistą guard rails.

## 5. Następne Kroki

- [ ] Echo → potwierdzić z Virealem wybór płótna i uzupełnić ADR (sekcja Open Questions).  
- [ ] Echo → przekazać Lumenowi powyższą notatkę (komunikat `@Lumen`).  
- [ ] Lumen/Nodus → uzupełnić plan implementacji/testów (taski `LUMEN-20251025-003/004/005`, `NODUS-20251025-002`).  
- [ ] Kai → dodać scenariusze guard rails do planu testów Morfogenezy (`KAI-20251025-003`).  
- [ ] Storywright → po decyzjach płótna zaktualizować brief w `sessions/ORIN-20251025-002-...`.
