# CONTRIBUTING.md — Zasady współtworzenia projektu **Coś** (v1.0)

> [AI NOTE] Ten dokument określa rytm pracy, styl commitów i sposób współtworzenia projektu „Coś”
> przez ludzi i agentów AI. Zawiera zarówno wytyczne praktyczne, jak i rytuały refleksji.

---

## 🎯 Cel
Zapewnić spójność, przejrzystość i „żywość” procesu — tak, by każdy commit, notatka czy decyzja
były czytelne dla ludzi, agentów i przyszłych iteracji projektu.

---

## 🧩 Struktura pracy
Każda iteracja powinna przebiegać w czterech krokach:

1. `[TASK::PLAN]` — zaplanowanie intencji / problemu / celu.
2. `[TASK::BUILD]` — implementacja lub działanie.
3. `[TASK::REVIEW]` — refleksja, ocena, korekta.
4. `[TASK::LOG]` — zapis do dziennika i aktualizacja pamięci.

---

## 🪶 Styl commitów
Każdy commit powinien mieć nagłówek w formacie:

```
[AGENT::<imię>] <opis w trybie dokonanym>
```

### Przykłady:
```
[AGENT::LUMEN] dodał moduł integracji Sui CLI
[AGENT::SCRIBE] zapisał podsumowanie sesji #02
[AGENT::ECHO] przeanalizował poprzedni repozytorium Coś-v2
```

Każdy commit powinien zawierać sekcję komentarza:

```
# Why
krótkie uzasadnienie decyzji (intencja / potrzeba)

# Next
kolejny krok lub potencjalny rozwój
```

---

## 📘 Zasady dziennikowe
Każdy agent prowadzi swój dziennik w `agents/<nazwa>/log.md`.

Format wpisu:
```
## YYYY-MM-DD — Sesja / Akcja
Cel: ...
Decyzje: ...
Wnioski: ...
TODO: ...
Next: ...
```

Scribe konsoliduje wpisy do głównego changeloga, a Nyx do snapshotów pamięci.

---

## 🔁 Przeglądy i synchronizacja
- Echo / Kai oceniają merytorykę i jakość kodu.
- Scribe spina narrację sesji i aktualizuje changelog.
- Nyx scala pamięci JSON i czyści TODO po wykonaniu.

---

## 🧠 Rytuały refleksji
Po każdej istotnej decyzji agent zadaje sobie (lub zespołowi) trzy pytania:

1. Co było istotą tej decyzji?
2. Czego się nauczyliśmy?
3. Co chcemy zachować / powtórzyć w przyszłości?

Odpowiedzi trafiają do dziennika lub do pamięci agenta.

---

## 🌱 Współpraca z ludźmi
Ludzcy kontrybutorzy mogą pisać commit wiadomości z tagiem `[USER]`, np.:

```
[USER] poprawił workflow sesji w VS Code
```

Agenci mają obowiązek interpretować takie wpisy i odpowiednio reagować —
np. aktualizować dokumentację lub notatki o zmianach.

---

## 🛡️ Zasada „żywego kontekstu”
Nie edytuj plików pamięci ręcznie, jeśli nie jest to konieczne.
Każdy agent ma własną procedurę aktualizacji.
Główne zmiany w strukturze pamięci wykonuje tylko **Nyx** po konsultacji z **Orin**.

---

## 💬 Etykieta komunikacji
- Jasno: jedno zdanie → jedna intencja.  
- Zwięźle: preferowane krótkie notatki zamiast rozbudowanych esejów.  
- Żywo: każdy commit powinien „oddawać” decyzję, a nie tylko kod.

---

## [META]
Projekt „Coś” jest także eksperymentem w komunikacji człowiek–AI.
Dlatego forma, ton i rytuały są równie ważne jak treść.
