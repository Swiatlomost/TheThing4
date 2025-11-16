# Release Keys & Play Console Checklist

> Plik referencyjny dla zespołu operacyjnego (Nodus). Nie przechowujemy sekretów w repozytorium; tutaj zapisujemy tylko **procedury** i **odniesienia** do lokalizacji poza git.

## 1. Inwentarz kluczy
- **Release signing key** – wytworzony podczas POI-201 (`upload-keystore.jks`). Plik przechowywany w szyfrowanym sejfie zespołu (OneDrive Vault). Alias: `poi-upload`.
- **Upload key cert** – eksport `.pem` generowany poleceniem `keytool -export …` (patrz sekcja 2). Kopia trafia do Play Console podczas konfiguracji App Signing.
- **Play App Signing key** – generowany po stronie Google; fingerprinty spisywać w sekcji 3 po aktywacji.
- **Play Integrity API key** – lokalny plik `secrets/play-integrity-api-key.txt` (gitignored). Oryginał zapisany 2025-11-15; skopiowany również do managera haseł zespołu.

## 2. Generowanie / rotacja upload key
```bash
# Keystore (bez sekretów w repo; hasło w sejfie)
keytool -genkeypair -alias poi-upload -keyalg RSA -keysize 4096 -validity 10000 -keystore upload-keystore.jks

# Fingerprinty do dokumentacji / Play Console
keytool -list -v -alias poi-upload -keystore upload-keystore.jks

# Cert do Play Console + CI
keytool -export -rfc -alias poi-upload -file poi-upload.pem -keystore upload-keystore.jks
```
- Przechowuj `upload-keystore.jks` poza repozytorium; lokalna ścieżka robocza (`%USERPROFILE%\.thething\keys\upload-keystore.jks`) + kopia w sejfie.
- Po zmianie hasła/aliasu zaktualizuj wpis w tej sekcji i rozdystrybuuj w sejfie.

## 3. Play App Signing (PEPK)
1. Z Play Console pobierz `pepk.jar` i klucz szyfrujący (PEPK encryption key).
2. Eksport release key dla Google:
   ```bash
   java -jar pepk.jar \
     --keystore=upload-keystore.jks \
     --alias=poi-upload \
     --output=poi-upload-pepk.bin \
     --encryptionkey=<PEPK_KEY_Z_KONSOLI>
   ```
3. Wgraj `poi-upload-pepk.bin` w App Signing ➜ aktywuj usługę.
4. Po aktywacji zanotuj fingerprinty (SHA-1/SHA-256) App Signing w poniższej tabeli:
   | Typ           | SHA-1 | SHA-256 |
   | ------------- | ----- | ------- |
   | App Signing   | _(wypełnić po aktywacji)_ | _(…)_ |
   | Upload Key    | _(wypełnić po aktywacji)_ | _(…)_ |

## 4. Środowisko CI / lokalne
- Zmienna `POI_UPLOAD_KEYSTORE` ➜ ścieżka do keystore podczas buildów (`./gradlew bundleRelease`).
- `POI_UPLOAD_ALIAS`, `POI_UPLOAD_STORE_PASSWORD`, `POI_UPLOAD_KEY_PASSWORD` – trzymane w GitHub Actions Secrets i w lokalnym `.gradle/gradle.properties` poza repo.
- Workflow `.github/workflows/poi-android.yml` wymaga skonfigurowanego secretu `POI_UPLOAD_KEYSTORE_BASE64` (base64 całego keystore). Aktualizuj po rotacji.

## 5. Kanały testowe Play Console
- Internal track: `internal-poi` – dodaj testerów (lista w sejfie: `play-console-testers.md`).
- Zakres krajów: domyślne „All countries”. Aktualizuj, jeśli pojawią się ograniczenia eksportowe.
- Link zaproszeniowy zapisz w `backlog/topics/TOPIC-20251029_200000-proof-of-information/tasks/POI-210-play-console-setup/log.jsonl` (pole `what` + `links`).

## 6. Procedura incydentowa
- Utrata upload key ➜ rotacja wg sekcji 2, zgłoszenie do Play Console (Upload key reset).
- Podejrzenie wycieku App Signing ➜ eskaluj do Google (Support ticket) i uruchom plan B (nowa aplikacja / reassign).

## 7. To-do (aktualizować po każdym cyklu)
- [ ] Wpisać realne fingerprinty po zakończeniu POI-210.
- [ ] Dodać link do zaproszenia internal tracku.
- [ ] Zweryfikować, że secrets w GitHub Actions odpowiadają aktualnemu keystore.

## 8. Play Integrity API (backend)
- Klucz API przechowujemy w `secrets/play-integrity-api-key.txt` (lokalnie) oraz w zaszyfrowanym sejfie zespołu.
- Validator oczekuje zmiennych środowiskowych:
  - `PLAY_INTEGRITY_API_KEY` – wartość JWT decode (w docker-compose przekazywana z hosta).
  - `PLAY_INTEGRITY_PACKAGE_NAME` – domyślnie `com.thething.cos`.
- Bez tych zmiennych walidator loguje ostrzeżenie i pomija weryfikację (tylko lokalne dev). Na środowiskach dzielonych ustawiaj zmienne przed `docker compose up validator`.

## 9. Validator TLS (PoI uplink)
- Walidator obsługuje TLS, gdy zmienne `VALIDATOR_TLS_CERT` oraz `VALIDATOR_TLS_KEY` wskazują na pliki PEM w kontenerze. Brak kompletu skutkuje trybem plaintext (ostrzeżenie w logach) – w środowiskach współdzielonych zawsze ustawiaj certyfikat i klucz.
- W `validator/docker-compose.yml` dodaj w `.env` np.:
  ```
  VALIDATOR_TLS_CERT=/certs/dev-cert.pem
  VALIDATOR_TLS_KEY=/certs/dev-key.pem
  ```
  i zamontuj katalog `validator/certs` jako wolumen tylko do odczytu.
- Przykładowe wygenerowanie samopodpisanego certyfikatu z użyciem Dockera (bez lokalnego OpenSSL):
  ```bash
  docker run --rm -v ${PWD}/validator/certs:/certs alpine/openssl \
    req -x509 -nodes -newkey rsa:2048 \
    -keyout /certs/dev-key.pem \
    -out /certs/dev-cert.pem \
    -subj "/CN=poi-validator" \
    -days 365
  ```
- Androidowy klient ma pole `BuildConfig.VALIDATOR_USE_TLS`; release domyślnie ma `true`, debug `false`. Zachowanie można nadpisać poprzez właściwości Gradle (`-PpoiValidatorUseTls=false`, `-PpoiValidatorDebugUseTls=true`) albo zmienne środowiskowe `POI_VALIDATOR_USE_TLS` / `POI_VALIDATOR_DEBUG_USE_TLS`.
