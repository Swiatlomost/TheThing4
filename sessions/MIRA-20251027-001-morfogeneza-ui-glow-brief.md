# MIRA-20251027-001 — Morfogeneza: UI Glow (4MAT)

Why
- Spójna, bioluminescencyjna skóra UI (ciemne tło + neonowy glow) wzmacnia tożsamość produktu i ułatwia percepcję stanu komórki.
- Animacje cyklu (narodziny → dojrzewanie) stanowią język wizualny dla procesu: „kropka → obwód → wypełnienie”.

What
- Nowa paleta (bioluminescencja): tło „noir teal”, tekst high/medium, akcent „Cyan Glow”.
- Tokeny i gradienty: obwód z poświatą, wypełnienie radialne, pasek progresu w stylu glow.
- Komórka: 5 etapów wizualnych (1: kropka; 2: obwód; 3: animacja kropka→obwód; 4: obwód narodzonej; 5: dojrzewanie przez wypełnienie gradientem).
- Organizmy wielokomórkowe: siatka kół z glow wewnątrz okręgu (rys. 2).

How
- Design tokens: kolor, promień poświaty, grubość obwodu, prędkości animacji (ms), easing.
- Compose: `Brush` dla obwodu (radial + blur), `ShaderBrush`/`Blur` lub maska dla poświaty, preferuj 60 fps.
- Animacje:
  - Narodziny: promień rośnie od kropki do docelowego obwodu (t≈300–500 ms, easeOutCubic).
  - Dojrzewanie: gradient wypełnienia od środka na zewnątrz (t≈400–800 ms, easeInOutSine), bez „przecieku” poza obwód.
- Parzystość app/overlay: wspólne parametry i kanał zdarzeń; brak dryfu frame’ów.

What If
- Tryb „niska energia”: redukcja intensywności glow/blur i klatek animacji.
- Presety barwne (turkus, fiolet, bursztyn) w przyszłości.

Acceptance (min.)
- Paleta i tokeny w jednym źródle prawdy; 3 ekrany skórkowane (w tym „Co chcesz powiedzieć?”).
- Etap 3→4: płynny wzrost promienia bez aliasingu; etap dojrzewania: wypełnienie gradientem w czasie.
- Render organizmu wielokomórkowego: układ kół w dużym okręgu, spójny glow.
- 60 fps na docelowym urządzeniu (lub noty Nodus jeśli niżej) i brak rozjazdu z overlay.

Questions (dla Orina/Vireal/Kai)
- Target fps/urządzenie? Minimalny SoC/SDK dla shaderów/blur.
- Dopuszczalna intensywność poświaty (kontrast, WCAG dla tekstu na ciemnym tle)?
- Czy animacje mogą być skracane w trybie oszczędzania energii?

Links
- board: backlog/board.json
- topic: backlog/morfogeneza/topics/TOPIC-20251025_120000-001/topic.json
- reference: backlog/morfogeneza/topics/TOPIC-20251025_120000-001/UI-20251026_120000-001.md
