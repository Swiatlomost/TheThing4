# Projekt "Cos" - Manifest i orientacja (v1.1)

> Repozytorium stanowi **zywy kontekst** dla ChatGPT (GPT-5) pracujacego w VS Code.
> Zawiera manifest, architekture agentowa, standardy pracy oraz kompletna strukture pamieci i dziennikow.

## Quick Start (VS Code + GPT-5)
1. Otworz repozytorium w VS Code.
2. W rozszerzeniu ChatGPT wykonaj polecenie:
   ```
   Prosze przeczytac pliki: PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Ustaw ten zestaw jako staly kontekst sesji projektu "Cos".
   ```
3. Rozpocznij sesje:
   ```
   [SESSION::START] Prosze zainicjalizowac dziennik i zapytac o cel biezacej sesji.
   ```
4. Orin nadaje identyfikator zadaniu (`ORIN-YYYYMMDD-XXX`) i deleguje kolejne kroki.

## Dlaczego "Cos"?
Projekt traktuje relacje czlowiek <-> AI jako rdzen systemu. Agenci sa rolami z konkretna odpowiedzialnoscia, wlasnym logiem (`log.md`), stanem zadan (`task.json`) i pamiecia (`memory.json`).

## Najwazniejsze pliki
- `PROJECT_CONTEXT.md` - manifest + kontekst techniczny.
- `AGENTS.md` - specyfikacje agentow i struktura plikow.
- `WORKFLOW.md` - rytualy sesji, konwencje komunikacji, status board.
- `MEMORY_SPEC.md` - standardy pamieci i dziennikow.
- `AI_GUIDE.md` - instrukcja wstrzykiwania kontekstu do GPT-5 w VS Code.
- `SETUP.md` - skroty i zadania startowe.
- `agents/` - katalogi agentow z dokumentacja biezacych zadan.

## Aktualna struktura agentow
Kazdy agent posiada:
- `log.md` - sekcje **Active Task** + szablon archiwum,
- `task.json` - identyfikatory zadan i statusy,
- `memory.json` - sposob wspolpracy, heurystyki, ryzyka.

Tablica `agents/status.md` pokazuje w jednym miejscu postep calego zespolu.

## Checklisty build APK (Android)
1. **Srodowisko**
   - Zainstaluj [Android Commandline Tools](https://developer.android.com/studio#command-tools).
   - Ustaw `ANDROID_SDK_ROOT` i dodaj `platform-tools` do PATH.
   - Uruchom:
     ```
     sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
     sdkmanager --licenses
     ```
   - Upewnij sie, ze masz JDK 17 (`java -version`).
2. **Gradle**
   - Repo zawiera Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`).
   - `settings.gradle.kts` ma repozytoria: `google()`, `mavenCentral()`, `gradlePluginPortal()` w sekcji `pluginManagement`.
   - Opcjonalnie mozesz wskazac lokalny ZIP Gradle (cache) w `gradle-wrapper.properties`.
3. **Build**
   ```
   ./gradlew assembleDebug
   ```
   - Artefakt: `app/build/outputs/apk/debug/app-debug.apk`.
   - Testy instrumentacji: `./gradlew connectedDebugAndroidTest` (wymaga emulatora/urzadzenia).
4. **CI / cache**
   - Utrzymuj cache `~/.gradle` i katalogu SDK.
   - W GitHub Actions: `actions/setup-java@v3` (JDK 17) + instalacja CLI via `sdkmanager`.
   - Brak stabilnego internetu → rozważ lokalny mirror SDK/Gradle.
