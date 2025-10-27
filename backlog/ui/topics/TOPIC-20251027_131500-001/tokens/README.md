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


Final (v0.3b) — Accepted Neon Defaults
- Produkcyjne (hard-coded) miejsce:
  - feature/cos-lifecycle/src/main/java/com/example/cos/lifecycle/CosLifecycleScreen.kt:101
- Wartości (app/overlay):
  - ringDp = 5.1
  - haloWidthMult = 11.3
  - haloAlpha = 0.40
  - blurDp = 46
- Maska: halo klipowane do koła organizmu (brak kwadratu okna overlay).
- Następny krok: migracja do tokens.json i przełączenie Canvas na odczyt z tokenów.
