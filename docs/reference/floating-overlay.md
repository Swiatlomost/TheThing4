# Floating Overlay Solution Reference

## Platform requirements
- Overlay uses SYSTEM_ALERT_WINDOW permission with TYPE_APPLICATION_OVERLAY windows.
- Foreground service must declare type specialUse on Android 14.
- Request permission via ACTION_MANAGE_OVERLAY_PERMISSION and guide user through settings.

## Architecture summary
- OverlayController (Hilt singleton) handles start/stop, permission checks, DataStore for persisted position.
- LifecycleOverlayService (foreground) hosts WindowManager + ComposeView, subscribes to shared cell state, and exposes notification channel.
- OverlayRepository stores overlay visibility/position/preferences (DataStore).
- Overlay and in-app mode share the same CellSnapshot flow to keep life cycle sync.

## UI & gestures
- Compose overlay screen with:
  - pointerInput { detectDragGestures { ... } } to update WindowManager.LayoutParams.x/y within screen bounds.
  - detectTapGestures(onDoubleTap = { launch main activity }) to hop back into the app.
- Visual design: semi-transparent bud phases, mature/spawned komorki skaluja sie wzgledem kontenera.
- Accessibility: meaningful contentDescription, TalkBack announcement for double tap action.

## Lifecycle flow
1. User toggles overlay from app; controller checks permission and starts service via ContextCompat.startForegroundService.
2. Service attaches Compose view to WindowManager with layout params WRAP_CONTENT + flags FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS.
3. When app is opened (double tap), overlay hides or service stops to avoid duplication.
4. Drag updates persist to DataStore so overlay reappears in last position.
5. QA/dev mogą sterować etapami tylko z komponentów podpisanych tym samym kluczem (permission `com.example.cos.permission.CONTROL_LIFECYCLE`). W praktyce używamy UI w aplikacji lub dedykowanych skryptów/instrumentacji; czyste polecenia `adb shell am broadcast …` nie są już honorowane.

## Testing guidance
- Unit tests: OverlayControllerTest verifying lifecycle + DataStore persistence.
- Instrumented: UiAutomator scenario for overlay presence, drag, double tap.
- Manual: Pixel 5 check covering start, transparency, drag boundaries, resume/hide behaviour.

## Reference ADR / research
- docs/cos/floating-overlay-research.md (original research).
- docs/cos/adr/ADR-2025-10-22-floating-overlay.md (architecture decision record).
