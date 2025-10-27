# Morfogeneza — Architektura i Wskazówki Implementacyjne

- Cel: tworzenie/edycja form oraz natychmiastowa aktywacja (SharedFlow + fallback broadcast) z parytetem app/overlay.
- Canvas: siatka heksagonalna z micro-offsetem (snap-to-hex, +5% promienia).
- Kanał: `MorphoFormChannel` (Hilt singleton) — SharedFlow + broadcast do overlay.
- Synchronizacja promieni: engine ekranu cyklu + overlay używają tych samych wartości.
- Testy: Compose + sanity logcat/dumpsys (Kai + Nodus) — sprawdzanie zdarzenia `forma_aktywna` i braku regresji.

Komponenty (przykładowe):
- ViewModel: `MorphogenesisViewModel` — zapis, wybór, aktywacja form, emisje do kanału.
- Overlay: `LifecycleOverlayService` — subskrypcja kanału, malowanie formy.
- Engine: `CosLifecycleEngine` — promienie komórek i reguły styku.

Dobre praktyki:
- Minimalne przyrosty (oddzielne tasks), każdy z własnym PDCA i logiem.
- Zanim ustawisz `in_progress` — uzupełnij PDCA; po każdym kroku zapisz Why → Next w logu.
- Aktualizuj ADR tylko przy zmianach modelu/kontraktów publicznych.

Szybkie linki:
- ADR: `docs/architecture/ADR-2025-10-25-morfogeneza.md`
- UX: `docs/ux/morphogeneza-ux-research.md`
- Test plan: `docs/testing/morphogeneza-test-plan.md`
- Checklist: `docs/testing/morphogeneza-event-checklist.md`

