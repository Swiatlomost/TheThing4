# 🧬 ORIN-20251022-004: Mechanika paczkowania

## Cel zadania
Manualne i warunkowe paczkowanie dojrzalych komórek z walidacją styku struktury.

## Kryteria sukcesu
- Funkcjonalne: Paczkowanie tylko dojrzałych komórek, walidacja styku, poprawne tworzenie nowej komórki.
- Techniczne: Testy jednostkowe reguł styku PASS, scenariusz Espresso budujący nową komórkę na Pixel_5 PASS.

## Delegacje agentów
- Echo: analiza ryzyk, rekomendacje
- Vireal: wytyczne architektoniczne, ADR
- Lumen: implementacja, testy jednostkowe
- Kai: checklisty, testy akceptacyjne
- Scribe: logi, kronika
- Nyx: snapshot pamięci, konsolidacja

## Checkpointy
- [x] Echo: analiza i rekomendacje
- [x] Vireal: architektura i ADR
- [ ] Lumen: implementacja i testy
- [ ] Kai: testy akceptacyjne
- [ ] Scribe: logi i kronika
- [ ] Nyx: snapshot i konsolidacja

## Ryzyka i mitygacje
### Techniczne:
- Błędna walidacja styku → testy jednostkowe, review Kai
- Race condition przy paczkowaniu → synchronizacja w kodzie, testy
### Funkcjonalne:
- UX niejasny dla użytkownika → feedback od Scribe, checklisty

## Notatki sesji
### Decyzje kluczowe:
- [2025-10-23] ADR-2025-10-23-paczkowanie-komorek.md: Paczkowanie tylko dojrzałych komórek, walidacja styku, reguły energetyczne, logowanie operacji.
### Następne kroki:
- Implementacja i testy jednostkowe (Lumen)


---

## 📝 CHECKLISTA WORKFLOW

### 1. Pre-task validation
- [ ] Sprawdzenie spójności task.json i status.json
- [ ] Weryfikacja pamięci agentów (memory.json)
- [ ] Sprawdzenie logów (log.md)
- [ ] Testy jednostkowe i integracyjne (./gradlew test, connectedDebugAndroidTest)
- [ ] Brak konfliktów w repozytorium

### 2. Utworzenie pliku sesji
- [x] Plik `sessions/ORIN-20251022-004-mechanika-paczkowania.md` utworzony

### 3. Delegacje i planowanie
- [ ] Przypisanie zadań agentom (Echo, Vireal, Lumen, Kai, Scribe, Nyx)
- [ ] Opisanie wymagań i kryteriów sukcesu w pliku sesji
- [ ] Wpisanie checkpointów i ryzyk

### 4. Realizacja kroków
- [ ] Analiza wymagań i ryzyk (Echo)
- [ ] Decyzje architektoniczne i ADR (Vireal)
- [ ] Implementacja i testy jednostkowe (Lumen)
- [ ] Testy akceptacyjne i review (Kai)
- [ ] Logi i kronika (Scribe)
- [ ] Snapshot i konsolidacja pamięci (Nyx)

### 5. Synchronizacja statusów
- [ ] Aktualizacja agents/status.json i agents/status.md
- [ ] Aktualizacja statusów w task.json agentów

### 6. Cooldown i zamknięcie zadania
- [ ] Checklist cooldown (status updated, log entry, memory consolidated, status board updated, linked agents verified)
- [ ] Wpis do logów Orina i Scribe
- [ ] Przeniesienie zadania do completed_tasks

---

*Lista generowana automatycznie na podstawie WORKFLOW.md, AI_GUIDE.md, MEMORY_SPEC.md.*
