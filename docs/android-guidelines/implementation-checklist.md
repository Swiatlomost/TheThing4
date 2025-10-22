# Android Implementation Checklist

## Przed startem funkcji
- [ ] Przejrzyj docs/android-guidelines/architecture.md i potwierdź moduł docelowy.
- [ ] Dodaj/aktualizuj ADR jeśli decyzja architektoniczna różni się od standardu.
- [ ] Upewnij się, że istnieją odpowiednie moduły Gradle (feature/core). W razie potrzeby utwórz nowy moduł wg wzorca.

## Implementacja
- [ ] Utwórz ViewModel + UI state (sealed classes/data classes) z pojedynczym źródłem prawdy.
- [ ] Dodaj Use Case / interaktor gdy logika biznesowa wykracza poza prosty mapping.
- [ ] Repozytorium implementuje interfejs domenowy; mapuj DTO ↔ domain models.
- [ ] Wstrzykuj zależności przez Hilt (@Inject, @HiltViewModel, moduły DI).
- [ ] Zachowaj unidirectional data flow (UI -> ViewModel -> use case -> repo -> data sources).

## Testy
- [ ] Unit test ViewModel/Use Case (JUnit + kotlinx.coroutines-test + MockK). Pokryj scenariusze success/error.
- [ ] Instrumentation test (Compose/Espresso) dla krytycznych interakcji UI.
- [ ] Jeśli wprowadzasz nowy moduł, dodaj test integracyjny lub contract test.
- [ ] Zaktualizuj baseline profile/macrobenchmark gdy funkcja krytyczna wydajnościowo.

## Statyczna analiza i formatowanie
- [ ] ./gradlew lint detekt ktlintCheck loklanie przed PR.
- [ ] Napraw ostrzeżenia lint/detekt (bez tłumienia, chyba że ADR to uzasadnia).

## Dokumentacja
- [ ] Zaktualizuj README / docs/android-guidelines/ gdy wprowadzasz nowe narzędzia/wzorce.
- [ ] Dodaj wpis do docs/android-guidelines/changelog.md (utwórz jeśli brak) z opisem zmian.
- [ ] Scribe zapisuje decyzję i TODO w logu/kronice; Nyx aktualizuje pamięć.

## CI/CD
- [ ] Potwierdź, że pipeline CI uruchamia lint/test/instrumentation (GitHub Actions lub inny).
- [ ] Jeśli potrzebny jest nowy krok CI (np. baseline profile), dodaj workflow i udokumentuj.
- [ ] Upewnij się, że ./gradlew assembleDebug przechodzi i APK instaluje się na emulatorze.

## Po wdrożeniu
- [ ] Zaktualizuj gents/<name>/task.json i gents/status.md (cooldown checklist).
- [ ] Nyx wykonuje snapshot pamięci, jeśli wprowadzasz nowe zasady.
- [ ] Przygotuj QA/Acceptance notes dla Kai (checklist). 
