# PDCA: NODUS-20251025-002 – Overlay sanity rerun (double tap, instrumentation)

## Plan
- Potwierdzić stabilność overlay (double tap, lifecycle, saved state) i integrację testów instrumentalnych.

## Do
- Uruchomić connectedDebugAndroidTest i zebrać logcat.
- Zweryfikować brak crashy i poprawność zamykania overlay.

## Check
- Raport PASS + logcat dostępny dla Kai.

## Act
- Dodać sanity do pipeline CI (matrix urządzeń).
