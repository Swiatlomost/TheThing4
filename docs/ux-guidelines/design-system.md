# Design System Guidelines (Draft v0.1)

## Foundations
- **Kolory**: wykorzystaj dynamic color (Material You) + fallback brand palette (primary, secondary, 	ertiary).
- **Typografia**: display, headline, 	itle, ody, label (Material 3); uzywaj MaterialTheme.typography i TextStyle extension w Compose.
- **Spacing**: siatka 8dp (4dp dla micro spacing); komponenty padding -> Spacing.xs/s/m/l/xl.
- **Kształty**: MaterialTheme.shapes (corner radius 4dp/12dp), adaptacja do platform guidelines.

## Komponenty UI
- Przygotuj moduł core/designsystem z composables:
  - Buttons (Primary, Secondary, Tonal, Icon).
  - TextField (outlined, filled) z walidacją i helper/error text.
  - Card/Surface, Chip (filter, assist), FAB.
  - TopAppBar, NavigationBar / NavigationRail.
- Każdy komponent: dokumentacja parameterów + examples w designsystem-preview package.

## Nawigacja & Layout
- Wzorce: Navigation Compose + NavHost. Używaj Single Activity pattern.
- Adaptive layouts: breakpoints Compact (<600dp), Medium (600-840dp), Expanded (>840dp).
- Back navigation zgodne z systemem, bottom nav ≤5 destinations, rail dla large.

## Accessibility
- Min kontrast 4.5:1; test z Accessibility Scanner.
- contentDescription dla ikon, semantics w Compose.
- Focus order logiczny, ememberScrollState + Modifier.semantics.
- Animacje z opcją redukcji (prefers-reduced-motion).

## Motion
- Używaj nimate* API Compose, durations 150-300ms.
- Easing: FastOutSlowInEasing dla wejść, LinearOutSlowIn dla wyjść.
- Provide transition specs w Navigation (enter/exit). Unikaj nadmiaru animacji.

## Dokumentacja
- Diagram stylów w Figma + eksport do Compose przez Material Theme Builder.
- ADR dla zmian w brand palette/komponentach.
- Checklista review: kolor/typografia/ikonografia/accessibility/testy.

## Integracja
- Moduł core/designsystem eksponuje public API (MaterialTheme, components). Feature modules używają go poprzez dependency.
- Style w XML (jeśli potrzebne) synchronizowane z Compose ColorScheme.
- Testy screenshot/Compose UI dla komponentów krytycznych.

## Kolejne kroki
1. Utworzyc moduł core/designsystem z podstawowym themingiem.
2. Dostarczyć preview catalog (dev-only screen) do przeglądania komponentów.
3. Zdefiniować baseline accessibility checklist (współpraca z Lumen/Kai).
