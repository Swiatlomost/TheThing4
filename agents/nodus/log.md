# Nodus Log (Integrator)

## Active Focus
- NODUS-20251025-002 - Event overlay forma_aktywna

## Decision Journal
| date | topic | outcome | next |
|------|-------|---------|------|
| 2025-10-25 | Overlay tooling | Sprawdzilem adb i emulator Pixel_5, przygotowalem checkliste uprawnien | Po parity overlay powtorzyc sanity |
| 2025-10-25 | Overlay sanity rerun | Zweryfikowalem foregound service + double tap workflow, overlay utrzymuje sie i zamyka poprawnie | Udostepnic logcat Kai oraz uaktualnic checklisty adb |
| 2025-10-25 | Event forma_aktywna | Wybralem kanal SharedFlow + fallback broadcast; zdarzenie zawiera formId, cellsHash, timestamp | Przygotowac checklisty adb/logcat i zsynchronizowac z Lumen/Kai |

## PDCA Snapshot
- **Plan**: Dostarczyc i zweryfikowac kanal `forma_aktywna` dla Morfogenezy (SharedFlow + fallback broadcast).
- **Do**: Zdefiniowalem specyfikacje w ADR, przygotowuje checklisty adb/logcat dla sanity.
- **Check**: Wspolnie z Lumenem i Kai potwierdzic, ze event dochodzi do overlay oraz loguje w telemetry.
- **Act**: Po wdrozeniu monitorowac logcat i raportowac ewentualne opoznienia; w razie problemow eskalowac do Orina w <30 min.

## TODO
- [x] Opracowac kanal `forma_aktywna` i zaktualizowac ADR
- [ ] Przygotowac checklisty adb/logcat dla sanity eventu
- [ ] Potwierdzic emisje z Lumenem (test manualny)
- [ ] Przekazac Kai scenariusz testowy eventu

## Archive
- (pending)
