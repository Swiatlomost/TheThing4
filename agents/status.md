# Status Board

Use this page as the human-readable mirror of `agents/status.json`.

## Updating Rules
- Keep entries sorted by task ID.
- Only tasks with status `in_progress` or `pending` belong in **Active Tasks**.
- Move finished items to **Completed Tasks** on the same day they close.
- Include references to supporting artefacts (session files, ADRs, test reports).

## Active Tasks
| Task ID | Agent | Title | Last Update | Notes |
|---------|-------|-------|-------------|-------|
| ORIN-20251025-002 | Orin | Morfogeneza v0.1 - orkiestracja sesji | 2025-10-25 | Koordynuje przyrosty 003-005, synchronizuje guard rails |
| ECHO-20251025-002 | Echo | Research kreatora Morfogenezy | 2025-10-25 | Guard rails UX - `docs/ux/morphogeneza-ux-research.md` |
| MIRA-20251025-001 | Mira | Brief Morfogenezy Cos | 2025-10-25 | PDCA: `notes/pdca/MIRA-20251025-001.md` |
| LUMEN-20251025-005 | Lumen | Edytor komorek i zapisy form | 2025-10-25 | PDCA: `notes/pdca/LUMEN-20251025-005.md` |
| KAI-20251025-003 | Kai | Plan testow Morfogenezy | 2025-10-25 | PDCA: `notes/pdca/KAI-20251025-003.md`, plan `docs/testing/morphogeneza-test-plan.md` |
| NODUS-20251025-003 | Nodus | Checklisty eventu forma_aktywna | 2025-10-25 | PDCA: `notes/pdca/NODUS-20251025-003.md`, checklist `docs/testing/morphogeneza-event-checklist.md` |

## Completed Tasks
| Task ID | Agent | Title | Closed On | References |
|---------|-------|-------|-----------|------------|
| ECHO-20251024-001 | Echo | Analiza ryzyk Cos overlay | 2025-10-24 | agents/echo/log.md |
| LUMEN-20251024-001 | Lumen | Implementacja cyklu Cos v0.1 | 2025-10-24 | feature/cos-lifecycle/src/main/java/com/example/cos/lifecycle/CosLifecycleEngine.kt |
| LUMEN-20251025-001 | Lumen | Implementacja manualnego cyklu (core) | 2025-10-25 | `./gradlew test` |
| LUMEN-20251025-002 | Lumen | Parity overlay Cos v0.1 | 2025-10-25 | feature/cos-overlay/src/main/java/com/example/cos/overlay/LifecycleOverlayService.kt |
| LUMEN-20251025-003 | Lumen | UI wejscia do Morfogenezy | 2025-10-25 | agents/lumen/log.md |
| LUMEN-20251025-004 | Lumen | Menu poziomu i zasobow komorek | 2025-10-25 | notes/pdca/LUMEN-20251025-004.md |
| NODUS-20251025-001 | Nodus | Overlay tooling check | 2025-10-25 | adb sanity logcat |
| NODUS-20251025-002 | Nodus | Event overlay forma_aktywna | 2025-10-25 | docs/architecture/ADR-2025-10-25-morfogeneza.md |
| ORIN-20251024-001 | Orin | CoS v0.1 - manualny cykl i overlay | 2025-10-25 | agents/orin/log.md |
| VIREAL-20251024-001 | Vireal | Architektura cyklu Cos v0.1 | 2025-10-24 | docs/architecture/ADR-2025-10-24-cos-architecture.md |
| VIREAL-20251025-002 | Vireal | ADR formy Cosia | 2025-10-25 | docs/architecture/ADR-2025-10-25-morfogeneza.md |
| ECHO-20251025-001 | Echo | Discovery manualnego cyklu | 2025-10-25 | docs/ux/morphogeneza-ux-research.md |
| KAI-20251024-001 | Kai | Plan testow Cos v0.1 | 2025-10-24 | docs/testing/cos-v0.1-test-plan.md |
| KAI-20251025-001 | Kai | Testy manualnego cyklu | 2025-10-25 | docs/testing/cos-v0.1-test-plan.md |
| KAI-20251025-002 | Kai | Testy overlay Cos v0.1 | 2025-10-25 | `./gradlew connectedDebugAndroidTest` |
| AURUM-20251025-001 | Aurum | Retro pipeline Cos | 2025-10-25 | agents/aurum/log.md |
| NYX-20251025-001 | Nyx | Aktualizacja memory manualnego cyklu | 2025-10-25 | agents/nyx/log.md |
| SCRIBE-20251025-001 | Scribe | Dokumentacja Cos v0.1 manualny cykl | 2025-10-25 | agents/scribe/log.md |



