# Meldestelle_Pro: Entwicklungs-Guideline

**Status:** Finalisiert & Verbindlich
**Version:** 1.0
**Stand:** 15. August 2025

## 1. Vision & Architektonische Grundpfeiler

Dieses Dokument definiert die verbindlichen technischen Richtlinien und Qualitätsstandards für das Projekt "
Meldestelle_Pro". Ziel ist die Schaffung einer modernen, skalierbaren und wartbaren Plattform für den Pferdesport.

Unsere Architektur basiert auf **vier Säulen**:

1. **Modularität & Skalierbarkeit** durch eine **Microservices-Architektur**
2. **Fachlichkeit im Code** durch **Domain-Driven Design (DDD)**
3. **Entkopplung & Resilienz** durch eine **ereignisgesteuerte Architektur (EDA)**
4. **Effizienz & Konsistenz** durch eine **Multiplattform-Client-Strategie (KMP)**

> **Grundsatz:** Jede Code-Änderung muss diese vier Grundprinzipien respektieren.

---

## 2. Coding Conventions & Code-Qualität

### 2.1. Sprach- und Stilstandards

* **Primärsprache:** Kotlin (JVM/Multiplatform)
* **Java-Kompatibilität:** Ziel ist Java 21+
* **Code-Stil:** Offizielle Kotlin Coding Conventions, durch `Detekt` geprüft.

### 2.2. Namenskonventionen

* **Klassen & Interfaces:** `PascalCase` (z.B. `MemberService`, `EventRepository`)
* **Funktionen & Variablen:** `camelCase` (z.B. `authenticateUser`, `memberRepository`)
* **Testmethoden:** Beschreibend mit Backticks (z.B. `` `should return Success for valid credentials` ``)
* **Konstanten:** `SCREAMING_SNAKE_CASE` (z.B. `MAX_RETRY_ATTEMPTS`)
* **Enums:** `PascalCase` für Werte (z.B. `MemberStatus.ACTIVE`)

### 2.3. Value Classes für Typsicherheit

Primitive Typen (UUID, String, Long) für IDs oder spezifische Werte müssen in typsichere `value class`-Wrapper gekapselt
werden.

```kotlin
    @JvmInline
value class MemberId(val value: UUID) {
    companion object {
        fun of(value: String): Result<MemberId, ValidationError> =
            runCatching { UUID.fromString(value) }
                .map { MemberId(it) }
                .mapError { ValidationError.INVALID_UUID }
    }
}
```

### 2.4. Error-Handling & Logging

* **`Result`-Pattern:** Für erwartbare Geschäftsfehler ist das `Result`-Pattern zu verwenden. Exceptions sind für
  unerwartete, technische Fehler reserviert.

* **Fehler-Hierarchie:** Wir verwenden eine `sealed class`-Hierarchie, um Fehlerarten klar zu kategorisieren (
  `DomainError`, `ValidationError`, `BusinessError`, `TechnicalError`).

* **Structured Logging:** Logs müssen strukturiert sein und eine Korrelations-ID enthalten, um Anfragen über
  Service-Grenzen hinweg zu verfolgen.

```kotlin
    logger.info {
    "Creating member" with mapOf(
        "memberId" to command.memberId.value,
        "correlationId" to MDC.get("correlationId")
    )
}
```

---

## 3. Backend-Entwicklungsrichtlinien

### 3.1. Microservice-Struktur (Clean Architecture)

Jeder fachliche Microservice folgt der 4-Layer-Struktur (`api`, `application`, `domain`, `infrastructure`).

### 3.2. Repository-Pattern

Jede Repository-Methode muss das `Result`-Pattern verwenden.

```kotlin
    interface MemberRepository {
    suspend fun findById(id: MemberId): Result<Member?, RepositoryError>
    suspend fun save(member: Member): Result<Unit, RepositoryError>
}
```

### 3.3. Messaging & Event-Naming

* **Event-Naming Convention:** Domänen-Events folgen dem Muster `{Domain}{Entity}{Action}Event`.

```kotlin
    data class MemberPersonalDataUpdatedEvent(...) : DomainEvent(...)
```

---

## 4. Frontend-Entwicklungsrichtlinien

Das Frontend folgt konsequent dem **Model-View-ViewModel (MVVM)**-Muster und der **Kotlin Multiplatform (KMP)**
-Strategie. Der UI-Code wird nach **fachlichen Features** (vertikale Schnitte) strukturiert.

---

## 5. Testing

Tests sind ein integraler Bestandteil jedes Features und müssen einen hohen Standard erfüllen.

### 5.1. Test-Pyramide & Werkzeuge

* **Unit-Tests (80 %+ Abdeckung):** Für Domänen- und Anwendungslogik (JUnit 5, MockK).

* **Integrationstests:** Decken alle Repository-Implementierungen und externen Integrationen ab.

* **Testcontainers als Goldstandard:** Jede Interaktion mit externer Infrastruktur (DB, Cache, Broker) **muss** mit
  **Testcontainers** getestet werden.

### 5.2. Debugging

Debug-Ausgaben im Test-Code müssen mit `[DEBUG_LOG]` beginnen, um sie leicht identifizieren und filtern zu können.

---

## 6. Infrastruktur- & Betriebs-Spezifikationen

### 6.1. Kafka-Konfiguration

Die Konfiguration muss auf maximale Zuverlässigkeit ausgelegt sein:

```yaml
    # in application.yml
    kafka:
        producer:
            acks: all
            enable-idempotence: true
            max-in-flight-requests-per-connection: 1
        consumer:
            group-id-prefix: "meldestelle-${spring.application.name}"
            auto-offset-reset: earliest
            enable-auto-commit: false
```

### 6.2. Datenbank-Migrationen (Flyway)

Migrations-Skripte müssen einer klaren Namenskonvention folgen.

* **Pattern:** `V{version}__{description}.sql` (z.B., `V001__Create_member_tables.sql`)

* **Repeatable:** `R__{description}.sql` (z.B., `R__Update_member_view.sql`)

### 6.3. API-Dokumentation (OpenAPI)

Alle öffentlichen REST-Endpunkte müssen mit OpenAPI-Annotationen (`@Operation`, `@ApiResponse`) dokumentiert werden, um
eine klare und interaktive API-Dokumentation zu generieren.

---

## 7. Dokumentationsstandards

### 7.1. Sprache für Dokumentation

* **README-Dateien:** Alle README-Dokumentationen im Projekt müssen in **deutscher Sprache** verfasst werden.
  Dies gewährleistet Konsistenz und Zugänglichkeit für das deutsche Entwicklungsteam.

* **Code-Kommentare:** Komplexe Geschäftslogik und fachliche Zusammenhänge sollen in deutscher Sprache kommentiert werden.

* **API-Dokumentation:** OpenAPI-Beschreibungen und -Beispiele sind bevorzugt in deutscher Sprache zu verfassen,
  sofern keine internationalen Anforderungen bestehen.

### 7.2. Dokumentationsstruktur

* README-Dateien sollen eine einheitliche Struktur befolgen: Überblick, Architektur, Entwicklung, Tests, Deployment.

* Technische Begriffe dürfen in englischer Originalform verwendet werden, wenn keine etablierte deutsche Übersetzung existiert.
