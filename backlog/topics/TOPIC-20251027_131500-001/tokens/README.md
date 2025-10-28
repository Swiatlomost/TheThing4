Global UI Tokens (draft)

Zakres
- Kolory bazowe, akcent glow, typografia, promie powiaty, blur, grubo obwodu, gradienty.
- Jedno rldo prawdy dla aplikacji i overlay.

Konwencja JSON
- Nazewnictwo kebab-case, wartoci w jednostkach logicznych (dp/sp/ms) lub normalizowanych [0..1].
- Kolory w hex ARGB (#AARRGGBB).
- Czas w ms; intensywno glow [0..1].

Integracja
- App: mapowanie na Compose Theme/Brush/Shadow.
- Overlay: import tych samych wartoci i konwersja na efekty.

Akceptacja Kai\n- anim-params: birth-threshold, ring-width-dp, halo-exponent, bg-radial-strength - uzywane w Morfogenezie (011/012).\n\nAkceptacja Kai
- 60 fps na Pixel 5 (lub uzasadnione noty), kontrast tekstu min. WCAG-AA.


Final (v0.3c) - Accepted Neon Source
- Produkcyjny odczyt: wartosci z `core/designsystem/src/main/res/raw/ui_tokens.json` (LocalUiTokens).
- Parametry (app/overlay):
  - `cell.ring-stroke-dp`
  - `glow.halo-width-mult` (opcjonalne, fallback 11.3)
  - `glow.halo-alpha` (opcjonalne, fallback 0.40)
  - `glow.blur-dp`
- Render:
  - Maska: halo klipowane do kola organizmu (brak kwadratu okna overlay).
  - Obwod: bialy rdzen + akcent (wspolczynniki w kodzie: core=0.4, crisp=0.6).

Energy Fill (Gaussian) - parametry
- `energy.whiten` [0..1] - domieszka do bieli rdzenia (szklistosc).
- `energy.core-alpha` [0..1] - sila rdzenia w centrum.
- `energy.glow-alpha` [0..1] - sila poswiaty turkusowej.
- `energy.core-stop` [0..1] - pozycja zaniku rdzenia (szybszy spadek).
- `energy.glow-stop` [0..1] - pozycja zaniku poswiaty (wolniejszy spadek).
- `energy.rim-alpha` [0..1] - subtelny rim-light przy granicy wypelnienia.

Implementacja:
- Canvas czyta parametry z tokenow i rysuje dwa radialne gradienty (SRC_OVER),
  opcjonalnie dodajac rim-light. Patrz: `feature/cos-lifecycle/.../CosLifecycleScreen.kt:342`.

Override w trybie deweloperskim
- Plik `files/ui_tokens_override.json` (wewnetrzny katalog aplikacji) moze zawierac pelne drzewo lub czesciowy fragment, np. tylko `{"energy":{...}}`.
- `UiTokenProvider` scala override z bazowym JSON (deep-merge). 
- Zapisywanie/Usuwanie: z ekranu Skin Demo przyciskami Zapisz do tokenow" i Usun override".
- Aby zastosowac globalnie (w calej aplikacji), uruchom ponownie aplikacje.
