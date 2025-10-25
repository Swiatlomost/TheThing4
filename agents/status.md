# Status Board

Use this page as the human-readable mirror of agents/status.json.

## Updating Rules
- Keep entries sorted by task ID.
- Only tasks with status in_progress or pending belong in **Active Tasks**.
- Move finished items to **Completed Tasks** on the same day they close.
- Include references to supporting artefacts (session files, ADRs, test reports).

## Active Tasks
| Task ID | Agent | Title | Last Update | Notes |
|---------|-------|-------|-------------|-------|
| ECHO-20251025-001 | Echo | Discovery manualnego cyklu | 2025-10-25 | Wymagania + ryzyka dla parity |
| MIRA-20251025-001 | Mira | Brief manualnego cyklu | 2025-10-25 | Utrzymuje 4MAT i pytania |

## Completed Tasks
| Task ID | Agent | Title | Closed On | References |
|---------|-------|-------|-----------|------------|
| LUMEN-20251025-002 | Lumen | Parity overlay Cos v0.1 | 2025-10-25 | feature/cos-overlay/src/main/java/com/example/cos/overlay/LifecycleOverlayService.kt:92 |
| LUMEN-20251025-001 | Lumen | Implementacja manualnego cyklu (core) | 2025-10-25 | ./gradlew test |
| KAI-20251025-002 | Kai | Testy overlay Cos v0.1 | 2025-10-25 | ./gradlew connectedDebugAndroidTest |
| KAI-20251025-001 | Kai | Testy manualnego cyklu | 2025-10-25 | docs/testing/cos-v0.1-test-plan.md:19 |
| VIREAL-20251025-001 | Vireal | Guard rails manualnego cyklu | 2025-10-25 | docs/architecture/ADR-2025-10-24-cos-architecture.md |
| NODUS-20251025-001 | Nodus | Overlay tooling check | 2025-10-25 | adb sanity logcat |
| ORIN-20251024-001 | Orin | CoS v0.1 - manualny cykl i overlay | 2025-10-25 | agents/orin/log.md:9 |
| AURUM-20251025-001 | Aurum | Retro pipeline Cos | 2025-10-25 | agents/aurum/log.md:7 |
| NYX-20251025-001 | Nyx | Aktualizacja memory manualnego cyklu | 2025-10-25 | agents/nyx/log.md:11 |
| SCRIBE-20251025-001 | Scribe | Dokumentacja Cos v0.1 manualny cykl | 2025-10-25 | agents/scribe/log.md:8 |
