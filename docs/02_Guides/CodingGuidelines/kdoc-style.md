---
type: Guide
status: ACTIVE
owner: Lead Architect
tags: [coding-style, kdoc, documentation]
---

# KDoc-Styleguide (Kurzfassung)

Dieser Styleguide definiert die wichtigsten Regeln für KDoc-Kommentare in Kotlin-Projekten der Meldestelle. Ziel:
Verständliche, konsistente API-Dokumentation via Dokka (GFM/HTML).

## Grundregeln

- Sprache: Deutsch für Fließtexte; Code/Bezeichner bleiben Englisch.
- Jeder public class, interface, object, enum, public function und public property erhält einen KDoc-Block.
- KDoc beginnt mit einem vollständigen, aussagekräftigen Satz in der dritten Person.
- Beispiele und wichtige Hinweise als kurze Absätze oder Listen, keine Romane.

## Struktur eines KDoc-Blocks

```kotlin
/**
 * Beschreibt prägnant, was das Element macht und warum es existiert.
 *
 * Details: Optionale Erläuterung von Parametern, Nebenwirkungen, Fehlerfällen.
 *
 * @param id Eindeutige Kennung des Members
 * @return Das gefundene Objekt oder null, wenn nicht vorhanden
 * @throws IllegalArgumentException Falls Parameter ungültig sind
 */
fun findMember(id: MemberId): Member?
```

## Tags

- @param: Für jeden Parameter bei public Funktionen
- @return: Wenn Rückgabewert semantisch relevant ist
- @throws: Relevante Exceptions dokumentieren
- @since, @see: Sparsam verwenden, wenn es wirklichen Mehrwert bringt

## Stil & Sprache

- Klar, knapp, aktiv. Keine Redundanz.
- Domänenbegriffe verwenden (BCs: members, horses, events, masterdata, infrastructure).
- Keine Interna oder Secrets dokumentieren.

## Beispielschnipsel

```kotlin
/** Erstellt einen neuen Event und persistiert ihn transaktional. */
fun createEvent(cmd: CreateEventCommand): EventId
```

## Dokka-Hinweise

- Dokka erzeugt GFM (Markdown) unter build/dokka/gfm und HTML unter build/dokka/html.
- Source-Link führt auf GitHub (main-Branch). Prüfe Links in der CI.

## Review

- PR-Checklist: "KDoc vollständig?" anhaken, wenn neue public APIs hinzugekommen sind.
- Vale/markdownlint gelten nur für .md; KDoc wird redaktionell in Code-Reviews geprüft.
