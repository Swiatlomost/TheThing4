# ADR-2025-10-23-paczkowanie-komorek.md

## Context
Mechanika paczkowania wymaga zdefiniowania reguł styku, zużycia energii oraz walidacji ciągłości struktury. Celem jest zapewnienie, że tylko dojrzałe komórki mogą być paczkowane, a proces ten nie narusza integralności systemu.

## Decision
- Paczkowanie możliwe wyłącznie dla komórek oznaczonych jako dojrzałe (`isMature == true`).
- Przed paczkowaniem wykonywana jest walidacja styku:
    - Komórka docelowa musi być wolna.
    - Sąsiedztwo nie może powodować konfliktu energetycznego (sprawdzane przez `validateContactRules()`).
- Zużycie energii jest obliczane na podstawie liczby sąsiadów i typu komórki.
- Po udanym paczkowaniu tworzona jest nowa komórka, a stan systemu jest aktualizowany.
- Każda operacja paczkowania logowana jest w systemie (Scribe).

## Consequences
- Zwiększona spójność i bezpieczeństwo procesu paczkowania.
- Możliwość łatwego testowania reguł styku (testy jednostkowe).
- Ułatwiona rozbudowa mechaniki o kolejne typy komórek lub reguły energetyczne.

## Status
Accepted

## Authors
Vireal

## Date
2025-10-23
