# Chronicle (Lean)

This is the consolidated narrative journal maintained by Scribe. Previous per-agent chronicle is retained in `agents/scribe/chronicle.md` for history; new entries should be added here.

---

## Paradoks Dowodu: Kronika Nonce i Zaufania
- "Jeśli prawda ma odcisk palca, niech będzie podpisana przez bogów z Play Store."

## Scene
- W laboratorium Proof of Information trwa epicki maraton: Nodus buduje walidator TLS na Hetznerze, Lumen integruje Play Integrity w Androidzie, a Kai zbiera logcaty jak archeolog dowody starożytnej cywilizacji. Między nimi krąży pytanie: czy token z Google będzie ufał nonce wygenerowanemu sekundę wcześniej?

## Plot Beats

### Akt I: Play Console i Klucze Losu (POI-210)
1. Nodus otwiera Play Console i aktywuje Play App Signing - ceremonię PEPK rozpoczyna wzywając upload key z zewnętrznych krypt (`docs/reference/release-keys.md`).
2. Internal track budzi się do życia; link zaproszeniowy (`https://play.google.com/apps/internaltest/4701145912598647155`) trafia do testerów jak zaproszenie na tajną radę.
3. Play Integrity API key powstaje w Google Cloud - ograniczony, zapisany w `secrets/play-integrity-api-key.txt` i sejfie zespołu, chroniony jak pierścień mocy.
4. Orin kiwa głową - POI-210 done. Play Console stoi, klucze pilnują, track czeka. "Dalej," mówi, wskazując na POI-211.

### Akt II: Integrity Token i Taniec Nonce (POI-211)
1. Lumen dodaje dependency Play Integrity i buduje helper: nonce SHA-256, Base64 URL encoding (nie zwykły Base64 - ta lekcja kosztowała error -13 i godzinę debugowania).
2. BatchUploader rozszerza swój rytuał: przed wysyłką batch generuje token z IntegrityManager i pakuje go w metadane gRPC.
3. Nodus po drugiej stronie łączy ustawia walidator: `decodeIntegrityToken` przez reqwest, sprawdza `packageName`, `verdict`, zgodność nonce. Docker-compose przyjmuje `PLAY_INTEGRITY_*` env jak zaklinania.
4. Kai instaluje versionCode 2 (1.1) na Xiaomi 13T Pro, uruchamia upload - logcat pokazuje `STATUS_ACCEPTED`. Evidence ląduje w `evidence/logcat-2025-11-16-accepted.txt`, walidator loguje success. POI-211 done.

### Akt III: gRPC Uploader i WorkManager (POI-212)
1. Lumen dodaje plugin protobuf, generuje stuby lite z `validator.proto`, buduje BatchUploader wskazujący na `10.0.2.2:50051` (debug emulator).
2. TLS flag trafia do BuildConfig: `VALIDATOR_USE_TLS`. Kanał przełącza się między plaintext a TLS w zależności od flagi. ServerTlsConfig czyta ENV w docker-compose.
3. WorkManager wchodzi do gry: payload, worker z retry/backoff, scheduler z Constraints.CONNECTED. LightLedgerRuntimeSmoke wywołuje upload automatycznie.
4. Evidence zbierane z fizycznego urządzenia przez adb reverse: logcat + docker logs. Debug pipeline działa - POI-212 done. Lumen przekazuje pałeczkę do POI-213.

### Akt IV: TLS i OAuth Objawienie (POI-213)
1. Nodus wdraża walidator na Hetznerze: `validator.poi-lab.pl:443`, certbot generuje cert, docker-compose montuje volume z certs. Handshake potwierdzony przez `openssl s_client` i `grpcurl` - TLS działa.
2. Android debug build przełącza się na TLS host. Upload idzie... i wraca HTTP 401 z Play Integrity API. Logcat i docker logs zgodne: `attestation_invalid_decode_http_status:401`.
3. Lumen zagłębia się w dokumentację Play Integrity - objawienie: **API keys are not supported**. Wymaga service account z rolą Play Integrity Verifier + OAuth Bearer token.
4. Nodus implementuje `gcp_auth`, walidator oczekuje `PLAY_INTEGRITY_SERVICE_ACCOUNT_JSON`. OAuth flow działa. Upload próbuje znowu...
5. **Nowy problem**: Release build z Google Play wpada w `attestation_invalid_nonce_mismatch`. Debug buildy działają, release buildy nie. Nonce się nie zgadza - prawdopodobnie timestamp drift lub WorkManager retry.
6. Lumen i Nodus planują następny ruch: dual-side logging (expected_nonce vs token nonce), analiza generowania nonce, synchronizacja timestamp. POI-213 in_progress, blocker aktywny.

## Dialogue Snippet
- "OAuth zamiast API key?" - zapytał Lumen, czytając błąd 401 po raz trzeci.
- "Google zmienił zasady. Teraz tylko service account," odparł Nodus, edytując docker-compose.
- "A co z tym nonce?" - dorzucił Kai, przeglądając logcat release buildu.
- "Timestamp dryfuje," mruknął Nodus. "WorkManager retry albo zegar się rozjeżdża. Trzeba logować po obu stronach."
- "Zapisuję to do kroniki zanim nonce dryfnie jeszcze bardziej," powiedziałam, notując każdy szczegół.

## Artefacts & Facts
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-210-play-console-setup/PDCA.json`
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-210-play-console-setup/log.jsonl:5` (Play Integrity key issued)
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-211-play-integrity-integration/PDCA.json`
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-211-play-integrity-integration/log.jsonl:12` (STATUS_ACCEPTED evidence)
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-212-android-uploader/PDCA.json`
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-212-android-uploader/log.jsonl:5` (WorkManager + TLS done)
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-213-validator-tls/PDCA.json`
- `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-213-validator-tls/log.jsonl:7` (Nonce mismatch blocker)
- `docs/reference/release-keys.md` (upload key, App Signing, fingerprinty)
- `evidence/logcat-2025-11-16-accepted.txt` (debug success)
- `evidence/logcat-2025-11-16-tls.txt` (TLS handshake + OAuth 401)
- `validator-logs-2025-11-16-tls.txt` (server-side evidence)

## Cliffhanger / Next Chapter Hook
- Czy nonce znajdzie synchronizację między klientem a walidatorem? Czy WorkManager przestanie dryfować w czasie? I czy release buildy w końcu zobaczą STATUS_ACCEPTED? Odpowiedzi czekają w kolejnym rozdziale POI-213, gdzie timestamp spotyka się z prawdą.

---
## Porządki w backlogu — autosort i presety usunięte
- "Niech lista zadań mówi tylko o tym, co robimy teraz."

## Scene
- Po akceptacji głównych elementów Morfogenezy (glow, dojrzewanie, multicell) domknęliśmy porządki w backlogu, usuwając odłożone pozycje.

## Plot Beats
1. MORPHO-008-autosort — usunięty z tablicy (deferred, brak aktywnego planu).
2. MORPHO-009-presety — usunięty z tablicy (deferred, brak aktywnego planu).

## Artefacts & Facts
- backlog/board.json
- backlog/backlog.json

## Next
- Backlog na zielono; kolejne priorytety dodamy po nowym briefie MIRA lub decyzji Orina.

---

## Narodziny zaakceptowane, historia uproszczona
- "Jesli narodziny sa plynne, niech swieca bez zwloki."

## Scene
- Test na urzadzeniu Pixel 5 potwierdzil akceptowalna animacje narodzin. Jednoczesnie porzadkujemy backlog: historia (undo/redo) zostaje wycieta z biezej puli.

## Plot Beats
1. MORPHO-011-anim-birth oznaczone jako done po testach w terenie (Pixel 5).
2. MORPHO-007-undo-redo usuniete z tablicy backlogu (zachowana historia plikow w tasks/ jako kontekst).

## Artefacts & Facts
- backlog/board.json
- backlog/topics/TOPIC-20251025_120000-001/tasks/MORPHO-011-anim-birth/log.jsonl

## Next
- Skupienie na kolejnych etapach Morfogenezy: glow (UI), animacja dojrzewania, render wielokomorkowy.

---
## Glow i dojrzewanie domknięte
- "Gdy skóra świeci jak trzeba, dojrzewanie przychodzi naturalnie, a stado znajduje układ."

## Scene
- Po przeglądzie kodu i sanity testach ręcznych uznajemy trzy elementy Morfogenezy za zakończone: glow UI, animację dojrzewania i render organizmu wielokomórkowego.

## Plot Beats
1. MORPHO-010-ui-glow — domknięty: bioluminescencyjny skin, spójność z tokenami i overlay.
2. MORPHO-012-anim-mature — domknięty: wypełnienie gradientowe dojrzewania zgodnie z założeniami.
3. MORPHO-013-multicell — domknięty: układ i render organizmu wielokomórkowego z glow.

## Artefacts & Facts
- backlog/board.json
- backlog/topics/TOPIC-20251025_120000-001/tasks/MORPHO-010-ui-glow/log.jsonl
- backlog/topics/TOPIC-20251025_120000-001/tasks/MORPHO-012-anim-mature/log.jsonl
- backlog/topics/TOPIC-20251025_120000-001/tasks/MORPHO-013-multicell/log.jsonl

## Next
- Morfogeneza: ewentualne szlify UX oraz dalsze decyzje produktowe; skupienie przesuwa się na kolejne obszary backlogu.

---

## Promienne Zgody Hexu
- "Jesli promien ma serce, niech bije w overlay i na ekranie glownym jednoczesnie."

## Scene
- W sali kontrolnej Hex-Halli Echo poprawia guard rails, podczas gdy Kai i Nodus porownuja logcat z nowym SharedFlow; Orin klika w status board, a Scribe notuje jak promienie komorek ukladaja sie w zgodny krag.

## Plot Beats
1. CosLifecycleEngine uklada kazda komorke z jednakowym promieniem, aby glowny ekran trzymal hexagonalny szyk (`feature/cos-lifecycle/src/main/java/com/example/cos/lifecycle/CosLifecycleEngine.kt:130`).
2. MorphogenesisViewModel rzuca SharedFlow z realnym promieniem szkicu, by overlay zobaczyl wybrana forme bez zwloki (`feature/morphogenesis/src/main/java/com/example/cos/morphogenesis/MorphogenesisViewModel.kt:179`).
3. LifecycleOverlayService odbiera nowy ksztalt i maluje promienie (`feature/cos-overlay/src/main/java/com/example/cos/overlay/LifecycleOverlayService.kt:120`).
4. Kai odhacza sanity zapis->wybor->aktywacja oraz forme 0 w planie testow (`docs/testing/morphogeneza-test-plan.md:24`).

## Dialogue Snippet
- "Promien 0 zostaje w cyklu, SharedFlow bierze reszte" - rzekl Kai. "A ja dopisze to do kroniki zanim promienie sie rozplyna" - odpowiedzialam.

## Artefacts & Facts
- feature/cos-lifecycle/src/main/java/com/example/cos/lifecycle/CosLifecycleEngine.kt:130
- feature/morphogenesis/src/main/java/com/example/cos/morphogenesis/MorphogenesisViewModel.kt:179
- feature/cos-overlay/src/main/java/com/example/cos/overlay/LifecycleOverlayService.kt:120
- docs/testing/morphogeneza-test-plan.md:24
- feature/morphogenesis/src/test/java/com/example/cos/morphogenesis/MorphogenesisViewModelTest.kt:78

## Cliffhanger / Next Chapter Hook
- Gdy backlog historii sie odrodzi, czy SharedFlow wytrzyma napor cofniecia w czasie?

---

## Parada Foregroundowych Duchow
- "Jesli duch ma runiczna licencje na swiecenie, niech swieci w obu swiatach."

## Scene
- Pixel_5 stoi jak elfia wieza kontrolna, a wokol niego kreci sie trojka Lumen-Kai-Nodus, podczas gdy Vireal liczy promienie komorek niczym czarodziej runy.

## Plot Beats
1. Lumen wtacza w overlay ten sam silnik i promien, dorzucajac lifecycle oraz saved state, by widma Compose przestaly krzyczec (`feature/cos-overlay/src/main/java/com/example/cos/overlay/LifecycleOverlayService.kt:92`).
2. Kai zerka w test plan i dopisuje nowy rytual UI-OVERLAY-003, bo bez logcatowego swiadectwa nikt nie uwierzy w spokojne duchy (`docs/testing/cos-v0.1-test-plan.md:17`).
3. Kai i Nodus melduja PASS, a status board mruga na zielono - parity overlay oficjalnie wchodzi do kanonu (`agents/kai/log.md:12`, `backlog/board.json`).

## Dialogue Snippet
- "Podwojny tap i koniec z krzykami," mruknal Lumen, na co Kai odparla: "Zapisane. Teraz zadnych duchow bez przepustki."

## Artefacts & Facts
- feature/cos-overlay/src/main/java/com/example/cos/overlay/LifecycleOverlayService.kt:92
- docs/testing/cos-v0.1-test-plan.md:17
- agents/kai/log.md:12
- backlog/board.json

## Cliffhanger / Next Chapter Hook
- Czy overlay wytrzyma publiczna probe ACCESS-001, czy tez kolejny test odsloni nowe duchy?

---

_Archive past stories below this line (newest on top)._
