Global UI Tokens (draft)

Zakres
- Kolory bazowe, akcent glow, typografia, promieĹ„ poĹ›wiaty, blur, gruboĹ›Ä‡ obwodu, gradienty.
- Jedno ĹşrĂłdĹ‚o prawdy dla aplikacji i overlay.

Konwencja JSON
- Nazewnictwo kebab-case, wartoĹ›ci w jednostkach logicznych (dp/sp/ms) lub normalizowanych [0..1].
- Kolory w hex ARGB (#AARRGGBB).
- Czas w ms; intensywnoĹ›Ä‡ glow [0..1].

Integracja
- App: mapowanie na Compose Theme/Brush/Shadow.
- Overlay: import tych samych wartoĹ›ci i konwersja na efekty.

Akceptacja Kai\n- anim-params: birth-threshold, ring-width-dp, halo-exponent, bg-radial-strength — używane w Morfogenezie (011/012).\n\nAkceptacja Kai
- 60 fps na Pixel 5 (lub uzasadnione noty), kontrast tekstu min. WCAG-AA.


Final (v0.3c) - Accepted Neon Source
- Produkcyjny odczyt: wartości z `core/designsystem/src/main/res/raw/ui_tokens.json` (LocalUiTokens).
- Parametry (app/overlay):
  - `cell.ring-stroke-dp`
  - `glow.halo-width-mult` (opcjonalne, fallback 11.3)
  - `glow.halo-alpha` (opcjonalne, fallback 0.40)
  - `glow.blur-dp`
- Render:
  - Maska: halo klipowane do koła organizmu (brak kwadratu okna overlay).
  - Obwód: biały rdzeń + akcent (współczynniki w kodzie: core=0.4, crisp=0.6).
