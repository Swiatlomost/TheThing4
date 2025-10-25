# ADR-2025-10-25: Morfogeneza Form Editor

## Status
Accepted — 2025-10-25

## Context
- Wprowadzamy tryb „Morfogeneza”, który pozwala użytkownikowi tworzyć i aktywować własne formy Cosia.
- Forma składa się z komórek; użytkownik może zarządzać ich liczbą (1..max posiadanych) oraz rozmiarem. Komórki nie mogą zajmować tej samej przestrzeni.
- Aktywna forma musi natychmiast odświeżać wizualizację zarówno w aplikacji, jak i overlay bez dodatkowych cooldownów.
- Storywright zdefiniował otwarte zagadnienia: płótno (siatka vs. wolna przestrzeń), kolizje z organami wewnętrznymi, kontrakt komunikacji z overlay.

## Decision
1. **Model danych formy**
   - `MorphoForm` zawiera: `formId`, `name`, `createdAt`, `cells`, `metadata`.
   - `cells` to lista `MorphoCell` z polami `{id, position (x,y), radius, traits}`.
   - `position` jest zapisywane w lokalnym układzie współrzędnych [0..1] x [0..1]; skalowanie do aktualnego levelu Cosia wykonuje warstwa prezentacji.
2. **Przechowywanie i aktywacja**
   - Formy zapisujemy w lokalnym repozytorium (Room/Datastore) z oznaczeniem `isActive`.
   - Forma 0 jest generowana on-the-fly na podstawie aktualnego stanu lifecycle (niezależna od repozytorium).
   - Aktywacja formy publikuje event `ActivateMorphoForm(formId)` do core oraz overlay (Nodus implementuje kanał).
3. **Warstwa edytora**
   - Edytor operuje na `MorphoEditorState` (kopia robocza formy) i udostępnia operacje: dodawanie komórki, usuwanie, przesuwanie, zmiana promienia.
   - Limit komórek egzekwowany w ViewModelu edytora.
   - Suwak promienia działa w zakresie `[minRadius(level), maxRadius(level)]`, gdzie wartości wynikają z obwodu organizmu.
4. **Walidacja i kolizje**
   - Guard rails: brak nachodzenia komórek (`distance >= r1 + r2`); komórki muszą mieścić się w granicach organizmu (`position` i `radius` mieszczą się w płótnie).
   - Przy naruszeniach UI natychmiast pokazuje czerwony outline, baner „Komórki nachodzą – przesuń lub zmniejsz” i blokuje zapis/aktywację; opcjonalny tryb „autosort” rozsuwa komórki na najbliższe wolne heksy.
5. **Integracja z overlay**
   - Po aktywacji forma przekazywana jest do `CosLifecycleEngine` jako aktualny layout wizualizacji.
   - Overlay nasłuchuje na `ActiveMorphoFormState` i renderuje komórki według tego samego modelu (Nodus zapewnia serializację przez poniższy kanał).
6. **Kanał `forma_aktywna`**
   - Wprowadzamy `MorphoFormChannel` (Hilt singleton w `core/morpho`) z `MutableSharedFlow<ActiveMorphoForm>`; domyślnie `replay = 1`, `extraBufferCapacity = 1`, bez suspending emitów.
   - Aplikacja (UI/VM) emituje zdarzenie poprzez `MorphoFormChannel.emit(ActiveMorphoForm(...))` po zapisaniu formy.
   - `LifecycleOverlayService` subskrybuje `channel.updates` w `CoroutineScope(Dispatchers.Main.immediate)` i natychmiast aktualizuje wizualizację.
   - Dla bezpieczeństwa procesowego utrzymujemy lekki `BroadcastReceiver` (`ACTION_MORPHO_FORM_ACTIVATED`) – overlay rejestruje go tylko gdy działa poza procesem. Payload zdarzenia: `form_id`, `cells_hash`, `timestamp_epoch_ms`.
   - Event logowany jest do logcat z tagiem `MorphoFormChannel` (INFO) oraz do telemetry (TODO, po wdrożeniu analityki).

## UX Guard Rails (Echo)
Źródło: `docs/ux/morphogeneza-ux-research.md`

1. **Limity zasobów:** licznik komórek zmniejsza się przy dodaniu elementu; cofnięcie nie oddaje zasobu (liczone w historii undo).
2. **Nakładanie:** mapa kolizji na siatce heks; czerwony outline + baner błędu; opcjonalne „autosort” rozsuwa kolidujące komórki.
3. **Granice organizmu:** outline + strefa ostrzegawcza 5%; suwak promienia odbija poza zakresem.
4. **Kontrola rozmiaru:** slider zależny od levelu (`minRadius(level)`, `maxRadius(level)`), pokazuje procent zajętej powierzchni.
5. **Historia form:** lokalne undo/redo (min. 5 kroków) + możliwość zapisu szkicu (forma nieaktywna).
6. **Feedback aktywacji:** toast w aplikacji, log overlay „Forma X aktywowana” oraz podświetlenie aktywnej formy na liście.
7. **Bezpieczeństwo danych:** flagujemy szkic jako „dirty”; przy powrocie proponujemy odzyskanie niedokończonej formy.

## Consequences
- Model współdzielony przez aplikację i overlay upraszcza synchronizację oraz testy automatyczne.
- Użycie współrzędnych [0..1] oraz siatki heksagonalnej (snap-to-hex ±5%) pozwala na niezależne od poziomu skalowanie formy.
- Guard rails zostały zdefiniowane; Echo i Kai muszą zadbać o UX/testy zgodne z listą powyżej.
- Kanał `MorphoFormChannel` zapewnia spójny sposób emisji zdarzeń; fallback receiver pozwala rozszerzyć rozwiązanie na osobny proces.
- Repozytorium form może rosnąć; potrzebne będą narzędzia do sprzątania/usuwania form (do rozważenia później).

## Open Questions / TODO
- [x] Orin: zatwierdź model płótna (siatka heksagonalna vs. wolna przestrzeń z podpowiedziami).  
  **Decyzja:** używamy siatki heksagonalnej z możliwością precyzyjnego dostrajania (snap-to-hex + mikro przesunięcia do 5% promienia).
- [x] Echo: dostarcz wytyczne UX (guard rails, zachowanie suwaków, komunikaty walidacyjne).  
  **Notatka:** `docs/ux/morphogeneza-ux-research.md`
- [x] Lumen: potwierdź, czy potrzebne są presety form startowych oprócz formy 0.  
  **Decyzja:** Forma 0 pozostaje jedynym presetem; dodatkowe formy powstają jako zapisy użytkownika.
- [x] Nodus: wybierz kanał przesyłu aktywnej formy (SharedFlow + fallback Broadcast) i zintegruj z eventem.

## Implementation Notes
- Pliki: `core/morpho/` (model + repozytorium), `feature/morphogenesis/` (UI + ViewModel).
- Wzorce DI: Hilt moduł udostępniający `MorphoFormRepository` oraz `MorphoEditorInteractor`.
- Testy: jednostkowe (walidacja kolizji, limity), integracyjne (aktywny layout->overlay).
