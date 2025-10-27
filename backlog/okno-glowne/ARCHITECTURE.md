# Okno Główne — Architektura i Wskazówki

- Cel: stabilny cykl życia (kropka → okrąg pusty → okrąg wypełniony → potomstwo) z regułą styku.
- Silnik: `CosLifecycleEngine` — reguły, promienie, ID, timery.
- UI: ekrany Compose mapujące stan na wizualizację.

Najlepsze praktyki:
- Testy regresji timingu i spawnu potomka w granicach styku.
- Zgodność promieni z overlay i brak dryfu layoutu.

