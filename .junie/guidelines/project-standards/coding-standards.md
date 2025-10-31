# Coding Standards und Code-Qualität

---
guideline_type: "project-standards"
scope: "coding-standards"
audience: ["developers", "ai-assistants"]
last_updated: "2025-09-15"
dependencies: ["master-guideline.md"]
related_files: ["build.gradle.kts", "detekt.yml", "*.kt"]
ai_context: "Coding conventions, naming standards, type safety, error handling, and logging practices"
---

## 📋 Coding Conventions & Code-Qualität

### Sprach- und Stilstandards

* **Primärsprache:** Kotlin (JVM/Multiplatform)
* **Java-Kompatibilität:** Ziel ist Java 21+
* **Code-Stil:** Offizielle Kotlin Coding Conventions, durch `Detekt` geprüft.

> **🤖 AI-Assistant Hinweis:**
> Alle Kotlin-Code müssen den offiziellen Kotlin Coding Conventions entsprechen:
> - **Detekt-Validierung:** Automatische Code-Style-Prüfung
> - **Java 21+ Kompatibilität:** Nutze moderne Java-Features wo sinnvoll
> - **Multiplatform:** Code sollte plattformübergreifend funktionieren

### Namenskonventionen

* **Klassen & Interfaces:** `PascalCase` (z.B. `MemberService`, `EventRepository`)
* **Funktionen & Variablen:** `camelCase` (z.B. `authenticateUser`, `memberRepository`)
* **Testmethoden:** Beschreibend mit Backticks (z.B. `` `should return Success for valid credentials` ``)
* **Konstanten:** `SCREAMING_SNAKE_CASE` (z.B. `MAX_RETRY_ATTEMPTS`)
* **Enums:** `PascalCase` für Werte (z.B. `MemberStatus.ACTIVE`)

### Value Classes für Typsicherheit

Primitive Typen (UUID, String, Long) für IDs oder spezifische Werte müssen in typsichere `value class`-Wrapper gekapselt werden.

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

### Error-Handling & Logging

* **`Result`-Pattern:** Für erwartbare Geschäftsfehler ist das `Result`-Pattern zu verwenden. Exceptions sind für unerwartete, technische Fehler reserviert.

* **Fehler-Hierarchie:** Wir verwenden eine `sealed class`-Hierarchie, um Fehlerarten klar zu kategorisieren (`DomainError`, `ValidationError`, `BusinessError`, `TechnicalError`).

* **Structured Logging:** Logs müssen strukturiert sein und eine Korrelations-ID enthalten, um Anfragen über Service-Grenzen hinweg zu verfolgen.

```kotlin
logger.info {
    "Creating member" with mapOf(
        "memberId" to command.memberId.value,
        "correlationId" to MDC.get("correlationId")
    )
}
```

## 🎯 AI-Assistenten: Coding-Standards-Schnellreferenz

### Namenskonventionen-Übersicht

| Element              | Convention             | Beispiel                                            |
|----------------------|------------------------|-----------------------------------------------------|
| Klassen/Interfaces   | PascalCase             | `MemberService`, `EventRepository`                  |
| Funktionen/Variablen | camelCase              | `authenticateUser`, `memberRepository`              |
| Konstanten           | SCREAMING_SNAKE_CASE   | `MAX_RETRY_ATTEMPTS`                                |
| Test-Methoden        | Backticks beschreibend | `` `should return Success for valid credentials` `` |
| Enum-Werte           | PascalCase             | `MemberStatus.ACTIVE`                               |

### Code-Qualitäts-Checkliste

- [ ] **Detekt-Prüfung:** Code-Stil entspricht Kotlin Conventions
- [ ] **Value Classes:** Primitive Typen sind in typsichere Wrapper gekapselt
- [ ] **Result-Pattern:** Geschäftsfehler verwenden Result statt Exceptions
- [ ] **Structured Logging:** Logs enthalten Korrelations-IDs
- [ ] **Error-Hierarchie:** Sealed Classes für Fehlerkategorisierung

### Häufige Code-Patterns

#### Typsichere IDs

```kotlin
@JvmInline
value class EntityId(val value: UUID) {
    companion object {
        fun generate(): EntityId = EntityId(UUID.randomUUID())
        fun of(value: String): Result<EntityId, ValidationError> =
            runCatching { UUID.fromString(value) }
                .map { EntityId(it) }
                .mapError { ValidationError.INVALID_UUID }
    }
}
```

#### Error-Handling mit Result

```kotlin
interface EntityRepository {
    suspend fun findById(id: EntityId): Result<Entity?, RepositoryError>
    suspend fun save(entity: Entity): Result<Unit, RepositoryError>
}

// Verwendung
when (val result = repository.findById(entityId)) {
    is Result.Success -> processEntity(result.value)
    is Result.Failure -> handleError(result.error)
}
```

#### Structured Logging

```kotlin
class EntityService {
    private val logger = LoggerFactory.getLogger(EntityService::class.java)

    suspend fun processEntity(command: ProcessEntityCommand): Result<Unit, ProcessingError> {
        val correlationId = MDC.get("correlationId")

        logger.info {
            "Processing entity" with mapOf(
                "entityId" to command.entityId.value,
                "correlationId" to correlationId,
                "operation" to "process"
            )
        }

        return try {
            // Processing logic
            Result.Success(Unit)
        } catch (e: Exception) {
            logger.error {
                "Entity processing failed" with mapOf(
                    "entityId" to command.entityId.value,
                    "correlationId" to correlationId,
                    "error" to e.message
                )
            }
            Result.Failure(ProcessingError.TECHNICAL_ERROR)
        }
    }
}
```

#### Sealed Class Hierarchie für Fehler

```kotlin
sealed interface DomainError {
    val message: String
    val code: String
}

sealed interface ValidationError : DomainError {
    data object INVALID_UUID : ValidationError {
        override val message = "Invalid UUID format"
        override val code = "VALIDATION_INVALID_UUID"
    }

    data object REQUIRED_FIELD_MISSING : ValidationError {
        override val message = "Required field is missing"
        override val code = "VALIDATION_REQUIRED_FIELD_MISSING"
    }
}

sealed interface BusinessError : DomainError {
    data object ENTITY_NOT_FOUND : BusinessError {
        override val message = "Entity not found"
        override val code = "BUSINESS_ENTITY_NOT_FOUND"
    }
}

sealed interface TechnicalError : DomainError {
    data object DATABASE_CONNECTION_FAILED : TechnicalError {
        override val message = "Database connection failed"
        override val code = "TECHNICAL_DATABASE_CONNECTION_FAILED"
    }
}
```

### Detekt-Konfiguration

Wichtige Detekt-Regeln für das Projekt:

```yaml
# detekt.yml
style:
  MaxLineLength:
    maxLineLength: 120
  FunctionNaming:
    functionPattern: '^[a-z][a-zA-Z0-9]*$'
  ClassNaming:
    classPattern: '^[A-Z][a-zA-Z0-9]*$'

complexity:
  ComplexMethod:
    threshold: 15
  LongParameterList:
    functionThreshold: 6

potential-bugs:
  UnsafeCallOnNullableType:
    active: true
```

---

**Navigation:**
- [Master-Guideline](../master-guideline.md) - übergeordnete Projektrichtlinien
- [Testing-Standards](./testing-standards.md) - Test-Qualitätsstandards
- [Documentation-Standards](./documentation-standards.md) - Dokumentationsrichtlinien
- [Architecture-Principles](./architecture-principles.md) - Architektur-Grundsätze
