# Architecture (Global)

Canonical ADRs: docs/architecture/
- ADR (Cos v0.1): docs/architecture/ADR-2025-10-24-cos-architecture.md
- ADR (Morfogeneza): docs/architecture/ADR-2025-10-25-morfogeneza.md

Overlay reference: docs/reference/floating-overlay.md
Android app structure: docs/reference/architecture-android.md

---
## Morfogeneza - Architektura i wskazowki
- Cel: edycja/zapis/aktywacja form (SharedFlow + fallback broadcast) z parytetem app/overlay.
- Canvas: siatka heksagonalna z micro-offsetem (snap-to-hex, +5% promienia).
- Kanal: MorphoFormChannel (Hilt singleton)  SharedFlow + broadcast do overlay.
- Synchronizacja promieni: engine ekranu cyklu i overlay uzywaja tych samych wartosci.
- Testy: Compose + sanity logcat/dumpsys - zdarzenie `forma_aktywna` i brak regresji.
- Komponenty: MorphogenesisViewModel (zapis/wybor/aktywacja form), LifecycleOverlayService (subskrypcja, render), CosLifecycleEngine (promienie/reguly).
- Linki: ADR `docs/architecture/ADR-2025-10-25-morfogeneza.md`, UX `docs/ux/morphogeneza-ux-research.md`, testy `docs/testing/morphogeneza-test-plan.md`.

## Overlay - Architektura i wskazowki
- Cel: parytet stanu/gestow wzgledem aplikacji (double tap, lifecycle/saved state).
- Serwis: LifecycleOverlayService - render stanu, subskrypcja kanalow.
- Testy: connected tests + sanity logcat (stabilny lifecycle, brak crashy).
- Dobre praktyki: minimalny interfejs wejscia (gesty/zdarzenia), wspolny model stanu; logi z prefiksem (MorfoEvent).
- Link: docs/reference/floating-overlay.md.

## Okno Glowne - Architektura i wskazowki
- Cel: stabilny cykl zycia (Seed  Bud  Mature  Spawned) z regulami styku.
- Silnik: CosLifecycleEngine - reguly, promienie, layout heksagonalny.
- UI: ekrany Compose mapujace stan na wizualizacje.
- Dobre praktyki: regresje timingu/spawnu, zgodnosc promieni z overlay i brak dryfu layoutu.
