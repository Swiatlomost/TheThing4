# ORIN-20251025-002 — Brief Morfogenezy

## 4MAT Discovery
- **WHY**: Morfogeneza daje użytkownikowi pełną sprawczość nad kształtem Cosia, co wzmacnia narrację rozwoju organizmu i nadaje znaczenie zgromadzonym komórkom.
- **WHAT**: Nowe okno dostępne z przycisku „Morfogeneza”. W centrum obrys organizmu, w górnym menu poziom Cosia (równy poziomowi okręgu najnowszego Cosia) oraz licznik dostępnych komórek. Użytkownik układa od 1 do posiadanej liczby komórek, każdą skalując suwakiem; komórki nie mogą się nakładać. Dowolna liczba zapisanych form, z formą 0 jako stanem obecnym.
- **HOW**: Tryb edycji pozwala dodawać/usuwać komórki, manipulować rozmiarem i pozycją, zapisywać szkice i aktywować wybraną formę. Aktywacja natychmiast synchronizuje wizualizację w aplikacji i overlay. Panel zapisanych form zawsze prezentuje formę 0 oraz kolejne wersje.
- **WHAT IF**: Otwarte kwestie to m.in. typ siatki/płótna do precyzyjnego pozycjonowania, sposób podglądu różnic między formami, reguły co do kolizji z organami wewnętrznymi oraz kontrakt API dla overlay.

## Conversation Highlights
- „Chcemy pełnej kreatywności, ale każdą komórką trzeba móc sterować w skali i położeniu.”
- „Formą 0 zostaje to, co widzimy dziś. Nowe formy mają natychmiast wpływać na overlay.”
- Decyzje: limit wykorzystania komórek = stan magazynu; brak dodatkowych kosztów ani cooldownów; przycisk Morfogenezy obok obecnych elementów UI.
- Do decyzji Orina: model fizycznego płótna (siatka? wolna przestrzeń?), szczegóły integracji z overlay oraz priorytety wdrożenia (UX vs. core logika).

## Draft Plan (PDCA Seeds)
- **Plan**: Dostarczyć interaktywny moduł Morfogenezy z zapisem, aktywacją i synchro overlay.
- **Do**: 
  - Echo zbiera referencje systemów „creature editor”.
  - Vireal przygotowuje ADR danych formy (lista komórek, rozmiary, pozycje, metadane).
  - Lumen prototypuje UI edytora + backend zarządzania formami.
  - Nodus planuje komunikat aktywacji dla overlay.
- **Check**: Kai przygotowuje scenariusze testów (limit komórek, overlap guard, szybka zmiana form). Orin zatwierdza, że aktywacja odświeża oba kanały.
- **Act**: Po walidacji zbieramy feedback użytkownika, wyniki trafiają do backlogu rozszerzeń (np. presetowe formy, współdzielone biblioteki).

## Recommendations for Orin
- Zadania do rozpisania:
  1. `[ECHO]` Research morfogenezy i kreatorów form — przykłady UX / guard rails.
  2. `[VIREAL]` ADR: Model danych formy Cosia + API aktywacji.
  3. `[LUMEN]` Prototyp UI nowego okna (edytor komórek, zapis/aktywacja, slider rozmiaru).
  4. `[NODUS]` Event synchro overlay («forma_aktywna»).
  5. `[KAI]` Plan testów E2E (limity komórek, brak nakładania, szybkie przełączanie).
- Ryzyka / zależności:
  - Brak ustalonej siatki może utrudnić precyzję i walidację (potrzebna decyzja).
  - Overlay wymaga jasnego kontraktu aktualizacji, inaczej powstanie lag w wizualizacji.
  - Skala komórek musi uwzględniać kolizje wewnątrz organizmu (potrzebne zasady).
- Pliki startowe:
  - `docs/reference/session-timeline.md` (fazy sesji).
  - `docs/templates/storywright-brief.md` (dla kolejnych iteracji).
  - Istniejące ADR: `docs/architecture/ADR-2025-10-24-cos-architecture.md` jako kontekst.
