# UX and UI Reference

## Design foundations
- Material 3 with dynamic color support and fallback brand palette.
- Typography: Material theme scales (display/headline/title/body/label) through `MaterialTheme.typography`.
- Spacing: 8dp grid (4dp micro); expose spacing tokens for components.
- Shapes: MaterialTheme.shapes (4dp / 12dp radii) adapted per platform.

## Components & navigation
- Maintain a `core/designsystem` module for shared Compose components (buttons, text fields, cards, chips, app bars, nav bars/rails).
- Provide preview catalog for designers/dev reviewing states.
- Navigation via Navigation Compose with single-activity pattern.
- Support responsive layouts: Compact <600dp, Medium 600-840dp, Expanded >840dp.

## Accessibility
- Minimum contrast 4.5:1; verify with Accessibility Scanner.
- Provide `contentDescription`/semantics for icons and interactive elements.
- Ensure focus order, keyboard navigation, TalkBack and Switch Access flows.
- Offer reduced motion option/respect system setting.

## Motion guidance
- Use Compose `animate*` APIs (150-300ms). FastOutSlowIn for enter, LinearOutSlowIn for exit.
- Define navigation transition specs, avoid excessive animation.

## Implementation checklist
- Always consult design-system and research docs before starting work.
- Reuse existing components; new patterns require ADR + review.
- UI state must be immutable data classes with Loading/Error/Success handling.
- Provide skeleton/placeholder for slow content.

## Testing & QA
- Compose UI tests (or Espresso for legacy views) on critical screens.
- Enable AccessibilityChecks in instrumentation.
- Use screenshot/snapshot tests when UI stability is critical.
- On CI: run lint/detekt/ktlint + connected UI tests and accessibility scanner passes.

## Documentation & memory
- Update README/docs when adding components or rules.
- Log decisions with Scribe; Nyx links new resources in memory.
- Keep a UX changelog for traceability.
