# POI-211 — Play Integrity Integration Guide

## Cel
Zapewnić kryptograficzne potwierdzenie autentyczności urządzenia po stronie Androida oraz walidatora, aby kolejne batch’e mogły przejść z trybu „dummy” na produkcyjny.

## Wymagania
- Play Console skonfigurowana (z zadania POI-210) i aplikacja dodana do Play Services.
- Dostęp do klucza API Play Integrity (`Google Cloud → APIs & Services → Credentials`).
- Emulator fizyczny lub urządzenie spełniające poziom Play Integrity (urządzenie z Google Play Services).

## Kroki — Klient Android
1. **Konfiguracja dependency**
   - W `app/build.gradle.kts` dodaj:  
     `implementation("com.google.android.play:integrity:1.5.0")` (sprawdź najnowszą wersję).
2. **Generowanie nonce**
   - Użyj wartości `merkleRoot || timestamp || deviceId` jako bazowego stringu.
   - Zastosuj `MessageDigest.sha256` i zakoduj Base64 → to będzie nonce na max 500 znaków.
3. **Pobranie tokenu**
   - Skorzystaj z `IntegrityManager` (Play Integrity API).  
     ```
     val request = IntegrityTokenRequest.builder()
         .setNonce(nonce)
         .build()
     ```
   - Obsłuż `StandardIntegrityToken`, wyciągnij `token`.
4. **Przekazanie do BatchUploader**
   - Zastąp placeholder `dummy` wartością tokenu.
   - Zapisz użyty nonce w metrykach, aby walidator mógł zweryfikować spójność.

## Kroki — Walidator (Rust)
1. **Biblioteka JWT**
   - Dodaj dependencję `jsonwebtoken = "9"` lub rozważ `governor` do cache TTL.
2. **Klucze JWK**
   - Endpoint: `https://www.googleapis.com/android/playintegrity/v1/jwks`.  
     Zbuduj prosty cache (TTL ~24h).
3. **Walidacja tokenu**
   - Parsuj JWT (algorytm RS256).  
   - Sprawdź `nonce` względem tego, który został przesłany w gRPC (muszą się zgadzać).
   - Zweryfikuj `deviceIntegrity` i `accountDetails` (według kryteriów ustalonych w PDCA).
4. **Rozszerzenie odpowiedzi**
   - Dodaj do `BatchProofResponse` `reason` np. `integrity_passed`, `integrity_missing`, `integrity_failed`.
   - Loguj typ werdyktu (INFO) oraz szczegóły w DEBUG (bez przechowywania całego tokenu).

## Testy
1. Uruchom apkę na fizycznym urządzeniu z dostępem do Google Play Services.
2. Upewnij się, że logcat zawiera status z `BatchUploader` oraz `PlayIntegrity` (dodać tag DEBUG).
3. Sprawdź logi walidatora (`docker compose logs -f validator`) — powinien pojawić się wpis `integrity=true`.
4. Dodaj wpis do `log.jsonl` z wynikiem i ewentualnymi ograniczeniami.

## Rezultat
- Gdy token nie przejdzie weryfikacji, batch jest odrzucany.
- W `log.jsonl` zanotowana metryczka „Next” → przełączenie TLS + WorkManager (POI-212).
