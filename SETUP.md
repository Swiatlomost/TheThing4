# SETUP.md - Task startowy (opcjonalnie)

> Mozesz dodac zadanie VS Code (tasks.json), ktore pomaga w szybkim otwarciu kontekstu.

Przykladowy `/.vscode/tasks.json`:
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Initialize Cos Context",
      "type": "shell",
      "command": "echo 'Wczytaj PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md w ChatGPT i ustaw kontekst.'"
    }
  ]
}
```

## Przygotowanie srodowiska build APK
1. **Commandline Tools & SDK**
   - Pobierz pakiet Android Commandline Tools i ustaw `ANDROID_SDK_ROOT`.
   - Dodaj `platform-tools` do PATH.
   - Uruchom:
     ```
     sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
     sdkmanager --licenses
     ```
2. **JDK / Gradle**
   - Zainstaluj JDK 17 i ustaw `JAVA_HOME`.
   - Upewnij sie, ze repo zawiera Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`).
3. **Build testowy**
   ```
   ./gradlew assembleDebug
   ```
   - Wynik APK: `app/build/outputs/apk/debug/app-debug.apk`.
4. **Opcjonalne kroki**
   - Cache `~/.gradle` i katalogu SDK (szczegolnie w CI).
   - W CI (np. GitHub Actions) uzyj `actions/setup-java@v3` (JDK 17) oraz instalacji CLI przez `sdkmanager`.
