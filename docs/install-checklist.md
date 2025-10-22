## Instrukcja instalacji Android CLI i Gradle Wrapper

### 1. Android Commandline Tools
1. Pobierz narzedzia CLI ze strony Google (https://developer.android.com/studio#command-tools) – wersja dla Twojego systemu.
2. Rozpakuj do np. `C:\Android\cmdline-tools\latest`.
3. Ustaw zmienne srodowiskowe:
   - `ANDROID_SDK_ROOT=C:\Android`
   - dodaj `C:\Android\cmdline-tools\latest\bin` oraz `C:\Android\platform-tools` do PATH.
4. W terminalu uruchom:
   ```
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   sdkmanager --licenses
   ```
5. Potwierdz dzialanie: ``sdkmanager --list``.

### 2. Gradle Wrapper w repo
1. W katalogu projektu uruchom (wymagany lokalny Gradle lub wrapper z innego repo):
   ```
   gradle wrapper --gradle-version 8.6
   ```
2. Sprawdz, ze repo zawiera pliki wrappera (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`).
   - `gradle-wrapper.properties` powinien miec `distributionUrl=https://services.gradle.org/distributions/gradle-8.6-bin.zip`
3. (Linux/macOS) nadaj `chmod +x gradlew`.
4. Test:
   ```
   ./gradlew --version
   ./gradlew assembleDebug
   ```
5. Wynik APK: `app/build/outputs/apk/debug/app-debug.apk`.

### 3. Po instalacji
- Nodus aktualizuje log i status (`sdkmanager --list` OK).
- Lumen raportuje dzialanie wrappera oraz testu build.
- Scribe dopisuje wpis kroniki.
- Nyx wykonuje snapshot pamieci.
