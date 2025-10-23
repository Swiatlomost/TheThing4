# Checklist testów akceptacyjnych: mechanika paczkowania (ORIN-20251022-004)

## Testy jednostkowe
- [x] Paczkowanie możliwe tylko dla komórek dojrzałych (CellStage.Mature)
- [x] Walidacja styku blokuje sąsiadów w fazie Bud
- [x] Nowa komórka powstaje tylko przy spełnieniu wszystkich reguł
- [x] Paczkowanie nie działa dla niedojrzałych komórek

## Testy integracyjne
- [ ] Integracja z UI: wywołanie paczkowania z poziomu widoku komórek
- [ ] Aktualizacja stanu po paczkowaniu (nowa komórka pojawia się w modelu)

## Testy manualne (Pixel_5)
- [ ] Dodanie nowej komórki przez paczkowanie dojrzałej (przycisk/gest)
- [ ] Walidacja: nie można spakować niedojrzałej komórki
- [ ] Walidacja: nie można spakować jeśli sąsiad jest w fazie Bud
- [ ] Logowanie operacji paczkowania (Scribe)

## Review
- [ ] Pokrycie testami jednostkowymi i integracyjnymi
- [ ] Przegląd kodu pod kątem zgodności z ADR i checklistą

---
Kai, 2025-10-23
