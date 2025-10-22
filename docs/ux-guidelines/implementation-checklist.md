# UX/UI Implementation Checklist

## Planowanie
- [ ] Sprawdz docs/ux-guidelines/research.md i design-system.md przed startem funkcji.
- [ ] Udokumentuj wymagania w ADR, jesli wprowadzasz nowy wzorzec UI/UX.

## Projektowanie
- [ ] Uzyj komponentow z core/designsystem; nie tworz duplikatow bez review.
- [ ] Zapewnij adaptacyjne layouty (Compact/Medium/Expanded) w Compose (WindowSizeClass).
- [ ] Dobierz kolory/typografie z MaterialTheme.colorScheme/typography.

## Accessibility
- [ ] Zapewnij kontrast >= 4.5:1 (Material Theme Builder/Accessibility Scanner).
- [ ] Dodaj contentDescription/semantics dla ikon, elementow interaktywnych.
- [ ] Test TalkBack / Switch Access.
- [ ] Zapewnij focus i keyboard navigation (Compose ocusable, Modifier.clearAndSetSemantics).

## Implementacja
- [ ] UI state jako immutable data class; obsługuj stany Loading/Error/Success.
- [ ] Animacje: nimate*AsState, duracje 150-300ms, respektuj prefersReducedMotion.
- [ ] Dostarcz placeholdery/skeletony dla wolnych zasobów.

## Testy
- [ ] Compose UI tests dla kluczowych ekranow (Espresso dla widoków legacy).
- [ ] Accessibility checks (AccessibilityChecks.enable()).
- [ ] Snapshot/screenshot tests jeśli UX krytyczny.

## QA / CI
- [ ] ./gradlew lint detekt ktlintCheck (napraw ostrzezenia UI).
- [ ] ./gradlew connectedDebugAndroidTest z testami UI.
- [ ] Uruchom Accessibility Scanner na emulatorze.

## Dokumentacja i pamięć
- [ ] Zaktualizuj README / docs jeśli dodajesz nowe komponenty/zasady.
- [ ] Dodaj wpis do docs/ux-guidelines/changelog.md (utworz jesli brak).
- [ ] Scribe notuje w logu/kronice; Nyx aktualizuje memory.json linkami.

## Po wdrożeniu
- [ ] Checklist cooldown (task.json -> log.md -> agents/status.md).
- [ ] Kai otrzymuje checklist QA (UI/Accessibility) do weryfikacji.
- [ ] Zweryfikuj, czy design system jest w sync (update modułu/previews).
