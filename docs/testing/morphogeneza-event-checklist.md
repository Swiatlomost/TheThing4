> Checklista sanity dla emisji forma_aktywna pomiedzy ekranem Morfogenezy a overlay.

# Morfogeneza - Event forma_aktywna (ADB / Logcat)

## Cel
Szybko potwierdzic, ze po aktywacji formy Morfogenezy system emituje zdarzenie forma_aktywna przez SharedFlow oraz fallback broadcast, a overlay rejestruje payload w logcat.

## Prerekwizyty
- Emulator lub urzadzenie Pixel_5 (API 34+) z wlaczonym trybem deweloperskim.
- Uruchomione srodowisko `./gradlew connectedDebugAndroidTest` (przynajmniej raz po kompilacji).
- Dostep do modulow `feature:morphogenesis` i `feature:cos-overlay` w biezacym branchu.
- Zainstalowana aplikacja debug (`app-debug.apk`) na emulatorze.

## Kroki ADB / Logcat
| # | Komenda / akcja | Oczekiwany rezultat |
|---|-----------------|----------------------|
| 1 | `adb devices` | Pixel_5 widoczny w trybie device lub emulator. |
| 2 | `adb shell am force-stop com.example.cos` | Aplikacja zatrzymana, overlay resetuje stan. |
| 3 | `adb shell am start -n com.example.cos/.MainActivity` | Uruchomienie aplikacji w stanie domyslnym. |
| 4 | W aplikacji: przejdz do ekranu Morfogenezy (CosLifecycleScreen -> przycisk Morfogeneza). | Ekran Morfogenezy z naglowkiem Lv / Komorki / Forma 0. |
| 5 | Uzyj "Dodaj komorke" (jesli potrzeba). Zaznacz komorke na liscie i wybierz Aktywuj. | UI pokazuje feedback aktywacji, slider reaguje na zmiany. |
| 6 | `adb logcat -d -s MorfoEvent:*` | Zdarzenie forma_aktywna z parametrami `formId`, `cellsHash`, `timestamp`. |
| 7 | `adb shell dumpsys activity broadcasts --history` (opcjonalnie z filtrem) | Fallback broadcast `com.example.cos.FORMA_AKTYWNA` zarejestrowany (status `result=0`). |

## Walidacja
- Kazde uruchomienie aktywacji formy powinno emitowac log w tagu `MorfoEvent`, np.\
  `I/MorfoEvent: forma_aktywna formId=FORM-0 cellsHash=... timestamp=...`
- Jesli log nie pojawia sie, uruchom `adb logcat -b events -s MorfoEvent` i powtorz kroki 4-6.
- W przypadku braku broadcastu: sprawdz `adb shell dumpsys activity broadcasts --history` dla intentu `com.example.cos.FORMA_AKTYWNA` (na API 34 historia moze byc czyszczona tuz po dostarczeniu).

## Roznice srodowiskowe
- Na fizycznym urzadzeniu upewnij sie, ze overlay ma przyznane uprawnienia `SYSTEM_ALERT_WINDOW` (komenda `adb shell appops get com.example.cos SYSTEM_ALERT_WINDOW`).
- W srodowisku CI ustaw `ANDROID_SERIAL` na identyfikator emulatora.

## Rezultaty do logow
- Lumen: w `agents/lumen/log.md` zanotuj date sanity oraz commit/branch.
- Nodus: w `agents/nodus/log.md` dodaj wpis "Checklisty adb/logcat" z linkiem do tego pliku.
- Kai: odnotuj wykorzystanie checklisty w `docs/testing/morphogeneza-test-plan.md`.

## Sesja 2025-10-26
- `adb devices` -> `emulator-5554 device`.
- `adb shell am force-stop com.example.cos` + `adb shell am start -n com.example.cos/.MainActivity` (po `./gradlew installDebug`).
- Emisja zdarzenia przez instrumentacje (`connectedDebugAndroidTest`) wygenerowala log:\
  `I/MorfoEvent: forma_aktywna formId=FORM-1761479486922 cellsHash=d9a7477e timestamp=1761479486924`.
- `adb shell dumpsys activity broadcasts --history` nie wykazal trwajacych broadcastow (zdarzenie dostarczone natychmiast).

## Undo/redo (backlog)
- [ ] Po wznowieniu prac nad undo/redo dodac kroki: aktywacja formy, cofniecie zmian (undo) i ponowne aktywacje; powtorzyc logcat/dumpsys.
- [ ] Zweryfikowac, czy `MorfoEvent` nie emituje duplikatow przy cofnieciu/przywroceniu oraz czy broadcast nie gromadzi starych payloadow.
