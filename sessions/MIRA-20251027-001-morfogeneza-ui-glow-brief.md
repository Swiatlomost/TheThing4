# MIRA-20251027-001 " Morfogeneza: UI Glow (4MAT)

Why
- Spljna, bioluminescencyjna sklra UI (ciemne to + neonowy glow) wzmacnia tosamo produktu i uatwia percepcj stanu komlrki.
- Animacje cyklu (narodziny ' dojrzewanie) stanowi... jzyk wizualny dla procesu: kropka ' obwld ' wypenienie.

What
- Nowa paleta (bioluminescencja): to noir teal, tekst high/medium, akcent Cyan Glow.
- Tokeny i gradienty: obwld z powiat..., wypenienie radialne, pasek progresu w stylu glow.
- Komlrka: 5 etaplw wizualnych (1: kropka; 2: obwld; 3: animacja kropka'obwld; 4: obwld narodzonej; 5: dojrzewanie przez wypenienie gradientem).
- Organizmy wielokomlrkowe: siatka kl z glow wewn...trz okrgu (rys. 2).

How
- Design tokens: kolor, promie powiaty, grubo obwodu, prdkoci animacji (ms), easing.
- Compose: `Brush` dla obwodu (radial + blur), `ShaderBrush`/`Blur` lub maska dla powiaty, preferuj 60 fps.
- Animacje:
  - Narodziny: promie ronie od kropki do docelowego obwodu (t300"500 ms, easeOutCubic).
  - Dojrzewanie: gradient wypenienia od rodka na zewn...trz (t400"800 ms, easeInOutSine), bez przecieku poza obwld.
- Parzysto app/overlay: wspllne parametry i kana zdarze; brak dryfu framelw.

What If
- Tryb niska energia: redukcja intensywnoci glow/blur i klatek animacji.
- Presety barwne (turkus, fiolet, bursztyn) w przyszoci.

Acceptance (min.)
- Paleta i tokeny w jednym rldle prawdy; 3 ekrany sklrkowane (w tym Co chcesz powiedzie?).
- Etap 3'4: pynny wzrost promienia bez aliasingu; etap dojrzewania: wypenienie gradientem w czasie.
- Render organizmu wielokomlrkowego: ukad kl w duym okrgu, spljny glow.
- 60 fps na docelowym urz...dzeniu (lub noty Nodus jeli niej) i brak rozjazdu z overlay.

Questions (dla Orina/Vireal/Kai)
- Target fps/urz...dzenie? Minimalny SoC/SDK dla shaderlw/blur.
- Dopuszczalna intensywno powiaty (kontrast, WCAG dla tekstu na ciemnym tle)?
- Czy animacje mog... by skracane w trybie oszczdzania energii?

Links
- board: backlog/board.json
- topic: backlog/topics/TOPIC-20251025_120000-001/topic.json
- reference: backlog/topics/TOPIC-20251025_120000-001/UI-20251026_120000-001.md
