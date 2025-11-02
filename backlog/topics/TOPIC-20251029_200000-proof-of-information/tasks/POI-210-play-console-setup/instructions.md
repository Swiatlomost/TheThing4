# POI-210 — Play Console Setup Checklist

## Cel
Włączyć Play App Signing dla TheThing, zarejestrować klucz upload oraz przygotować kanał testowy, aby kolejne buildy (POI-211/212) można było wypychać bez blokad operacyjnych.

## Wymagania wstępne
- Dostęp administracyjny do Google Play Console dla projektu TheThing.
- Lokalny plik `upload-keystore.jks` (lub możliwość wygenerowania go poleceniem `keytool` z `implementation-notes.md`).
- Zainstalowane narzędzie `pepk.jar` (Google Play Encryption Key), zwykle do pobrania z sekcji App Signing w konsoli.

## Kroki
1. **Play App Signing**
   - Zaloguj się do Play Console → Release → Setup → App Integrity → App Signing.
   - Jeśli App Signing nie jest aktywne, wybierz opcję „Export and upload a key from Java Keystore”.
   - Pobierz `pepk.jar` oraz kod konfiguracji **PEPK**.

2. **Upload key**
   - Jeżeli nie masz keystore: `keytool -genkeypair -alias poi-upload -keyalg RSA -keysize 4096 -validity 10000 -keystore upload-keystore.jks`.
   - Wyeksportuj klucz do formatu `.pem`:  
     `keytool -export -rfc -alias poi-upload -file poi-upload.pem -keystore upload-keystore.jks`.
   - Przekaż do Play Console fingerprint SHA-1 i SHA-256 (np. `keytool -list -v -keystore upload-keystore.jks`).

3. **Eksport release key do PEPK**
   - Uruchom:  
     `java -jar pepk.jar --keystore=upload-keystore.jks --alias=poi-upload --output=poi-upload-pepk.bin --encryptionkey=<PEPK_KEY_FROM_CONSOLE>`.
   - Wgraj plik `poi-upload-pepk.bin` w sekcji App Signing.

4. **Kanał testowy**
   - Release → Testing → Closed testing → utwórz nowy track „internal” (lub użyj istniejącego).
   - Dodaj co najmniej jedną grupę testerów (email / Google Group) i wybierz kraje (np. „All countries”).
   - Zanotuj link do zaproszenia; dopisz go w logu zadania po zakończeniu.

5. **Dokumentacja**
   - Uzupełnij `log.jsonl` wpisem „Why / What / Next” z fingerprintami i linkiem do tracka.
   - Zaktualizuj `docs/reference/release-keys.md` (jeśli dokument istnieje) albo stwórz wpis z fingerprintami oraz lokalizacją keystore (poza repo).

## Wynik końcowy
- Play App Signing aktywne, upload key działa.
- Track testowy gotowy, testerzy dodani.
- W dokumentacji zapisane fingerprinty kluczy oraz ścieżka do `upload-keystore.jks`.
