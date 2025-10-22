# UX/UI Best Practices Research

## Kluczowe zrodla
- [Material Design 3](https://m3.material.io/): system designu Google (kolory dynamiczne, typografia, komponenty adaptacyjne).
- [Material Design Guidelines](https://material.io/design): wzorce nawigacji, layout, motion.
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility): WCAG, TalkBack, kontrast, focus.
- [Now in Android](https://developer.android.com/series/now-in-android): artykuly o Compose UI, themingu, Motion.
- [Compose Design Systems](https://developer.android.com/jetpack/compose/designsystems): tworzenie design systemu w Compose.
- Community: Google I/O sesje o UX, blogi Material You, Medium (Stream, Lyft), książka "Refactoring UI" (adaptacja do Androida).

## Najwazniejsze praktyki
- **Spójny Design System**: kolory (Material You palettes), typografia (Dynamic Type), spacing 8dp grid.
- **Accessibility First**: kontrast >= 4.5:1, etykiety contentDescription, focusable elements, testy TalkBack.
- **Responsywnosc**: dostosowanie do foldables/tabletów, breakpoints (Compact, Medium, Expanded).
- **Motion & Animations**: meaningful motion (duration 200-400ms), gestures, transitions Compose Navigation.
- **Input & Forms**: validation inline, descriptive helper text, error states.
- **Theming**: MaterialTheme with dark/light, dynamic color fallback.

## Przyklady referencyjne
- [Now in Android sample](https://github.com/android/nowinandroid): Compose, dynamic color, adaptive layout.
- [Jetnews](https://github.com/android/compose-samples/tree/main/JetNews): Compose patterns i accessibility.
- [Plaid](https://github.com/android/plaid): modularyzacja design systemu.

## Narzedzia
- Figma + Material Theme Builder (eksport do Compose).
- Accessibility Scanner (Android) + Espresso accessibility checks.
- Baseline Profiles + Macrobenchmark for UI performance.

## Deliverables
1. Design system guide (Vireal) — design-system.md.
2. Wdrożeniowe checklisty (Lumen) — implementacja, testy, CI.
3. Nyx — aktualizacja pamięci i linków.

## Ryzyka
- Rozbieznosc miedzy Compose a XML (decyzja: Compose first, fallback XML). 
- Dynamic color vs brand guidelines — potrzebne fallback kolory.
- Accessibility debt bez dedykowanych testów — dodać do checklist.
