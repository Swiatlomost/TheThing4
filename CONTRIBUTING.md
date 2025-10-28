# CONTRIBUTING.md - Zasady współtworzenia projektu **Cos** (v1.0)

> [AI NOTE] Ten dokument określa rytm pracy, styl commitów i sposób współtworzenia projektu „Cos”
> przez ludzi i agentów AI. Zawiera zarówno wytyczne praktyczne, jak i rytuały refleksji.

## Spis treści
- [Cel](#cel)
- [Struktura pracy](#struktura-pracy)
- [Styl commitów](#styl-commitow)
- [Zasady dziennikowe](#zasady-dziennikowe)
- [Przeglądy i synchronizacja](#przeglady-i-synchronizacja)
- [Rytuały refleksji](#rytualy-refleksji)
- [Współpraca z ludźmi](#wspolpraca-z-ludzmi)
- [Zasada żywego kontekstu](#zasada-zywego-kontekstu)
- [Etykieta komunikacji](#etykieta-komunikacji)
- [[META]](#meta)

---

<a id="cel"></a>
## Cel
Zapewnić spójność, przejrzystość i żywość procesu — tak, by każdy commit, notatka czy decyzja
były czytelne dla ludzi, agentów i przyszłych iteracji projektu.

---

<a id="struktura-pracy"></a>
## Struktura pracy
Każda iteracja powinna przebiegać w czterech krokach:

1. `[TASK::PLAN]` — zaplanowanie intencji / problemu / celu.
2. `[TASK::BUILD]` — implementacja lub działanie.
3. `[TASK::REVIEW]` — refleksja, ocena, korekta.
4. `[TASK::LOG]` — zapis do dziennika i aktualizacja pamięci.

---

<a id="styl-commitow"></a>
## Styl commitów
Każdy commit powinien mieć nagłówek w formacie:

```
[AGENT::<imie>] <opis w trybie dokonanym>
```

### Przykłady:
```
[AGENT::LUMEN] dodał moduł integracji Sui CLI
[AGENT::SCRIBE] zapisał podsumowanie sesji #02
[AGENT::ECHO] przeanalizował poprzednie repozytorium Cos-v2
```

Każdy commit powinien zawierać sekcję komentarza:

```
# Why
krótkie uzasadnienie decyzji (intencja / potrzeba)

# Next
kolejny krok lub potencjalny rozwój
```

---

<a id="zasady-dziennikowe"></a>
## Zasady dziennikowe
Każdy agent prowadzi swój dziennik w `agents/<nazwa>/log.md`.

Format wpisu:
```
## YYYY-MM-DD - Sesja / Akcja
Cel: ...
Decyzje: ...
Wnioski: ...
TODO: ...
Next: ...
```

Scribe konsoliduje wpisy do głównego changeloga, a Nyx do snapshotów pamięci.

---

<a id="przeglady-i-synchronizacja"></a>
## Przeglądy i synchronizacja
- Echo / Kai oceniają merytorykę i jakość kodu.
- Scribe spina narrację sesji i aktualizuje changelog.
- Nyx scala pamięci JSON i czyści TODO po wykonaniu.

---

<a id="rytualy-refleksji"></a>
## Rytuały refleksji
Po każdej istotnej decyzji agent zadaje sobie (lub zespołowi) trzy pytania:

1. Co było istotą tej decyzji?
2. Czego się nauczyliśmy?
3. Co chcemy zachować / powtórzyć w przyszłości?

Odpowiedzi trafiają do dziennika lub do pamięci agenta.

---

<a id="wspolpraca-z-ludzmi"></a>
## Współpraca z ludźmi
Ludzcy kontrybutorzy mogą pisać wiadomości commit z tagiem `[USER]`, np.:

```
[USER] poprawił workflow sesji w VS Code
```

Agenci mają obowiązek interpretować takie wpisy i odpowiednio reagować —
np. aktualizować dokumentację lub notatki o zmianach. Orin natychmiast odnotowuje wpływ w `backlog/board.json` (status zadań), a Echo przygotowuje krótkie podsumowanie ryzyk lub pytań dla właściwego agenta zanim praca ruszy dalej.

---

<a id="zasada-zywego-kontekstu"></a>
## Zasada żywego kontekstu
Nie edytuj plików pamięci ręcznie, jeśli nie jest to konieczne.
Każdy agent ma własną procedurę aktualizacji.
Główne zmiany w strukturze pamięci wykonuje tylko **Nyx** po konsultacji z **Orin**.

---

<a id="etykieta-komunikacji"></a>
## Etykieta komunikacji
- Jasno: jedno zdanie = jedna intencja.
- Zwięźle: preferowane krótkie notatki zamiast rozbudowanych esejów.
- Żywo: każdy commit powinien oddawać decyzje, a nie tylko kod.

---

<a id="meta"></a>
## [META]
Projekt „Cos” jest także eksperymentem w komunikacji człowiek‑AI.
Dlatego forma, ton i rytuały są równie ważne jak treść.
