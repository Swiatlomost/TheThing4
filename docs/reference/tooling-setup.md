# Android Tooling Setup

## Command line tools
1. Download Android Commandline Tools (https://developer.android.com/studio#command-tools).
2. Extract to e.g. `C:\Android\cmdline-tools\latest`.
3. Set environment variables:
   - `ANDROID_SDK_ROOT=C:\Android`
   - Add `C:\Android\cmdline-tools\latest\bin` and `C:\Android\platform-tools` to PATH.
4. Run:
   ```
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   sdkmanager --licenses
   ```
5. Verify with `sdkmanager --list`.

## Gradle wrapper
1. In project root execute `gradle wrapper --gradle-version 8.6` (or reuse existing wrapper).
2. Ensure repo contains `gradlew`, `gradlew.bat`, and `gradle/wrapper/*` with distribution URL `https://services.gradle.org/distributions/gradle-8.6-bin.zip`.
3. On Unix run `chmod +x gradlew`.
4. Basic check:
   ```
   ./gradlew --version
   ./gradlew assembleDebug
   ```
5. APK output lives in `app/build/outputs/apk/debug/app-debug.apk`.

## After setup
- Nodus logs CLI availability and overlay permission tests.
- Lumen confirms wrapper build and reports results.
- Scribe records installation notes; Nyx snapshots memory.
