# PDCA: NODUS-20251025-002 - Overlay sanity rerun (double tap, instrumentation)

## Plan
- Potwierdzic stabilnosc overlay (double tap, lifecycle, saved state) i integracje testow instrumentalnych.

## Do
- Uruchomic connectedDebugAndroidTest i zebrac logcat.
- Zweryfikowac brak crashy i poprawnosc zamykania overlay.

## Check
- Raport PASS + logcat dostepny dla Kai.

## Act
- Dodac sanity do pipeline CI (matrix urzadzen).
