# WORKFLOW.md - Proces pracy z agentami (v1.1)

## Rytual sesji
1. `[SESSION::START]` - **Pre-task validation** → Sprawdź spójność task.json i status.md
2. **Orin Session Initialization** → Dla każdego nowego zadania ORIN-YYYYMMDD-XXX:
   - Załaduj pamięć z poprzednich prac (memory.json wszystkich agentów)
   - Opisz szczegółowo wymagania oraz kryteria sukcesu  
   - **Utwórz plik sesji** `sessions/ORIN-YYYYMMDD-XXX-{nazwa}.md` z kompletną specyfikacją
   - Przypisz poszczególne zadania dla agentów z precyzyjnymi delegacjami
   - Aktualizuj task.json (status: pending → in_progress) i agents/status.md
3. Analiza -> `[TASK::ANALYZE]` do Echo / Vireal.
4. Budowa -> `[TASK::BUILD]` do Lumen / Nodus.
5. Przeglad -> `[TASK::REVIEW]` do Kai.
6. Dziennik -> `[TASK::LOG]` do Scribe.
7. Pamiec / snapshoty -> Nyx.

## Pre-task Validation Checklist
**Przed rozpoczęciem każdego nowego zadania Orin wykonuje:**

### ✅ **Synchronizacja Statusów**
- [ ] Sprawdź czy wszystkie zadania "done" są w `completed_tasks` (nie w `current_tasks`)
- [ ] Zweryfikuj zgodność `agents/status.md` z rzeczywistymi statusami w task.json
- [ ] Potwierdź że linked_agent_tasks mają spójne statusy

### ✅ **Pamięć i Logi**
- [ ] Sprawdź czy ostatnie zadania mają wpisy w logach agentów
- [ ] Zweryfikuj czy memory.json jest aktualne (ostatnia aktualizacja < 24h)
- [ ] Potwierdź że agents/dashboard.md odzwierciedla aktualny stan

### ✅ **Infrastruktura**
- [ ] `./gradlew test` - testy jednostkowe PASS
- [ ] `./gradlew connectedDebugAndroidTest` - testy integracyjne PASS (jeśli istnieją)
- [ ] Sprawdź czy nie ma konfliktów w git repo

### 🚨 **W przypadku błędów walidacji:**
1. **STOP** - nie rozpoczynaj nowego zadania
2. Napraw niezgodności używając procedury z "Cooldown po zadaniu"
3. Zapisz incydent w `agents/orin/log.md`
4. Po naprawie wykonaj ponownie pre-task validation

## Konwencje komunikacji
- Odpowiedzi konczymy sekcja **Next step**.
- Decyzje zapisujemy jako: *Decyzja -> Powod -> Nastepny krok (opcjonalnie alternatywy)*.
- Gdy kontekst jest niepelny, agent proponuje rozwiazanie wraz z ryzykami - nie blokuje.
- Kazdy agent utrzymuje w katalogu `agents/<name>/` pliki:
  - `log.md` - biezacy wpis + szablon archiwum,
  - `task.json` - lista zadan (status, identyfikatory, powiazania),
  - `memory.json` - sposob wspolpracy, kontekst i heurystyki.
- `agents/status.md` jest tablica kontrolna: agent, rola, task_id, status, timestamp.
- Scribe prowadzi faktograficzny log w `agents/scribe/log.md`, a prozowa kronika (humor/epika/Grzesiuk) trafia do `agents/scribe/chronicle.md`.

## Przyklady promptow
- `"[SESSION::START] Prosze zebrac cele na dzis i zainicjalizowac dziennik."`
- `"[AGENT::ECHO] [TASK::ANALYZE] Przejrzyj PROJECT_CONTEXT.md i wypisz ryzyka."`
- `"[AGENT::LUMEN] [TASK::BUILD] Przygotuj szkic widoku Device Info."`
- `"[AGENT::SCRIBE] [TASK::LOG] Zanotuj podsumowanie sesji i TODO."`

## Definicja "Done"
- Artefakt + test/review (Kai) + wpis w dzienniku (Scribe) + aktualizacja `task.json` i `memory.json`.

## Cooldown po zadaniu
### 📋 Checklist zakończenia zadania (obowiązkowy dla wszystkich agentów):
1. **Agent wykonawczy**: Po zakończeniu pracy oznacza status zadania jako "done" w swoim `task.json` i dopisuje notatki
2. **Orin**: Przenosi ukończone zadania z `current_tasks` do `completed_tasks` w `agents/orin/task.json`
3. **Orin**: Aktualizuje `agents/status.md` - usuwa ukończone zadania z sekcji "Active Work"
4. **Scribe**: Dopisuje podsumowanie (log + kronika) oraz oznacza swoje zadanie jako DONE
5. **Nyx**: Gdy zaszła istotna zmiana stanu, aktualizuje `memory.json` i snapshot
6. **Wszystkich agenci**: Wykonują kroki 1-3 z sekcji "Relacje i zasady" z AGENTS.md

### ⚠️ Symptomy błędnej procedury cooldown:
- Zadania ze statusem "done" pozostają w sekcji `current_tasks`
- Niezgodność między `task.json` agentów a `agents/status.md`
- Brak wpisów zamknięcia w logach agentów

### 🔄 W przypadku wykrycia niezgodności:
1. Orin naprawia wszystkie pliki `task.json` przenosząc ukończone zadania
2. Aktualizuje `agents/status.md` 
3. Zapisuje incydent w swoim logu wraz z przyczynami błędu

## Template pliku sesji
**Każde zadanie ORIN-YYYYMMDD-XXX wymaga utworzenia pliku `sessions/ORIN-YYYYMMDD-XXX-{nazwa}.md` zawierającego:**

### 📋 **Struktura obowiązkowa:**
```markdown
# 🎯 ORIN-YYYYMMDD-XXX: [Tytuł zadania]

**Data rozpoczęcia**: YYYY-MM-DD  
**Koordynator**: Orin  
**Status**: IN_PROGRESS  
**Sprint**: [Nazwa sprintu]  

## 📋 Szczegółowe wymagania funkcjonalne
### Cel główny
- [Główne cele zadania]

### Kontekst techniczny z previous works
- [Stan obecny, co już mamy]
- [Co brakuje]

## 🎯 Kryteria sukcesu
### Funkcjonalne: [Lista kryteriów]
### Techniczne: [Testy, performance]  
### Jakościowe: [UX, accessibility]

## 🎯 Plan delegacji zadań
### ECHO-YYYYMMDD-XXX - [Tytuł]
**[AGENT::ECHO] [TASK::ANALYZE]**
- Zakres analizy: [Lista punktów]
- Źródła: [Pliki do przeanalizowania]
- Deliverables: [Oczekiwane rezultaty]

### VIREAL-YYYYMMDD-XXX - [Tytuł]  
**[AGENT::VIREAL] [TASK::BUILD]**
- Zakres projektowania: [Lista punktów]
- Wejścia: [Dane z Echo, dokumenty]
- Deliverables: [ADR, specyfikacje]

### LUMEN-YYYYMMDD-XXX - [Tytuł]
**[AGENT::LUMEN] [TASK::BUILD]**
- Zakres implementacji: [Lista punktów] 
- Zależności: [Wyniki Vireal, komponenty]
- Deliverables: [Kod, testy]

### KAI-YYYYMMDD-XXX - [Tytuł]
**[AGENT::KAI] [TASK::REVIEW]**
- Zakres testowania: [Lista punktów]
- Scenariusze: [Kluczowe test cases]
- Deliverables: [Testy, rezultaty]

### SCRIBE-YYYYMMDD-XXX - [Tytuł]
**[AGENT::SCRIBE] [TASK::LOG]**
- Zakres dokumentacji: [Lista punktów]
- Deliverables: [Logi, kronika]

### NYX-YYYYMMDD-XXX - [Tytuł]
**[AGENT::NYX] [TASK::LOG]**
- Zakres aktualizacji: [Lista punktów]
- Deliverables: [Memory updates, snapshots]

## 📊 Status realizacji
- [ ] Checkpoint 1: [Echo completion]
- [ ] Checkpoint 2: [Vireal architecture]
- [ ] Checkpoint 3: [Lumen implementation]
- [ ] Checkpoint 4: [Integration & testing]

## 🚨 Ryzyka i mitygacje
### Techniczne: [Lista ryzyk + mitygacje]
### Funkcjonalne: [Lista ryzyk + mitygacje]

## 📝 Notatki sesji
### Decyzje kluczowe: [Lista decyzji]
### Następne kroki: [Plan działania]
```

### 🎯 **Kiedy tworzyć plik sesji:**
- **ZAWSZE** dla nowych zadań ORIN-YYYYMMDD-XXX
- **Przed** rozpoczęciem delegacji do agentów  
- **Po** załadowaniu pamięci i analizie kontekstu
- **Jako** centralne źródło prawdy dla zespołu

