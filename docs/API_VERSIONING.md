# API Versioning Implementation

## Übersicht

Dieses Dokument beschreibt die implementierte Versionierungsstrategie für die Meldestelle API. Das System unterstützt sowohl DTO-Versionierung als auch API-Versionierung für eine saubere Evolution der API.

## Architektur

### 1. DTO Versionierung

Alle DTOs implementieren das `VersionedDto` Interface, welches folgende Eigenschaften bereitstellt:

```kotlin
interface VersionedDto {
    val schemaVersion: String
    val dataVersion: Long?
}
```

#### Beispiel Implementation:

```kotlin
@Serializable
@Since("1.0")
data class ArtikelDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    // ... andere Felder
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto
```

### 2. Version Manager

Der `VersionManager` verwaltet API-Versionen und Kompatibilität:

```kotlin
object VersionManager {
    const val CURRENT_API_VERSION = "1.0"
    val SUPPORTED_VERSIONS = listOf("1.0")
    val DEPRECATED_VERSIONS = emptyList<String>()
    const val MINIMUM_CLIENT_VERSION = "1.0"
}
```

### 3. API Versioning Plugin

Das Ktor-Plugin `VersioningPlugin` behandelt:
- Version-Header Validierung
- Automatische Version-Header in Responses
- Deprecation Warnings
- Unsupported Version Errors

## Verwendung

### Client-seitige Version Headers

Clients können die API-Version über Header spezifizieren:

```http
GET /api/artikel
API-Version: 1.0
```

oder

```http
GET /api/artikel
X-API-Version: 1.0
```

### Server Response Headers

Der Server antwortet mit Version-Informationen:

```http
HTTP/1.1 200 OK
API-Version: 1.0
X-Supported-Versions: 1.0
```

### Versioned Responses

Verwende die Extension-Funktionen für versionierte Antworten:

```kotlin
// Einzelnes DTO
call.respondVersioned(HttpStatusCode.OK, artikelDto)

// Liste von DTOs
call.respondVersionedList(HttpStatusCode.OK, artikelList)
```

## Migration System

### VersionMigrator Interface

```kotlin
interface VersionMigrator<T : VersionedDto> {
    fun migrate(dto: T, fromVersion: String, toVersion: String): T
    fun canMigrate(fromVersion: String, toVersion: String): Boolean
}
```

### Beispiel Migrator

```kotlin
class ArtikelDtoMigrator : VersionMigrator<ArtikelDto> {
    override fun migrate(dto: ArtikelDto, fromVersion: String, toVersion: String): ArtikelDto {
        return when {
            fromVersion == "1.0" && toVersion == "1.1" -> migrateFrom1_0To1_1(dto)
            else -> throw IllegalArgumentException("Unsupported migration")
        }
    }

    private fun migrateFrom1_0To1_1(dto: ArtikelDto): ArtikelDto {
        return dto.copy(
            schemaVersion = "1.1",
            // Neue Felder mit Standardwerten hinzufügen
        )
    }
}
```

## Annotations

### @Since(version)
Markiert, seit welcher Version ein DTO oder Feld verfügbar ist.

### @Deprecated(version, message)
Markiert veraltete DTOs oder Felder.

### @Until(version)
Markiert, bis zu welcher Version ein DTO oder Feld verfügbar war.

## Best Practices

### 1. Neue API Version hinzufügen

1. **VersionManager aktualisieren:**
```kotlin
const val CURRENT_API_VERSION = "1.1"
val SUPPORTED_VERSIONS = listOf("1.1", "1.0")
val DEPRECATED_VERSIONS = listOf("1.0")
```

2. **DTOs erweitern:**
```kotlin
@Serializable
@Since("1.1")
data class ArtikelDto(
    // Bestehende Felder...
    @Since("1.1")
    val neuesFeld: String? = null,
    override val schemaVersion: String = "1.1"
) : VersionedDto
```

3. **Migrator implementieren:**
```kotlin
class ArtikelDtoMigrator : VersionMigrator<ArtikelDto> {
    override fun migrate(dto: ArtikelDto, fromVersion: String, toVersion: String): ArtikelDto {
        return when {
            fromVersion == "1.0" && toVersion == "1.1" -> migrateFrom1_0To1_1(dto)
            // Weitere Migrationen...
        }
    }
}
```

### 2. Backward Compatibility

- Neue Felder sollten optional sein (nullable oder mit Standardwerten)
- Bestehende Felder nicht entfernen, sondern als @Deprecated markieren
- Migratoren für alle unterstützten Versionsübergänge bereitstellen

### 3. Breaking Changes

Bei Breaking Changes:
1. Neue Major Version erstellen
2. Alte Version als deprecated markieren
3. Migration Path bereitstellen
4. Dokumentation aktualisieren

## Beispiel API Calls

### Erfolgreiche Anfrage
```http
GET /api/artikel
API-Version: 1.0

HTTP/1.1 200 OK
API-Version: 1.0
X-Supported-Versions: 1.0
Content-Type: application/json

{
  "data": {
    "id": "...",
    "bezeichnung": "Test Artikel",
    "schemaVersion": "1.0",
    "dataVersion": 1
  },
  "version": {
    "apiVersion": "1.0",
    "supportedVersions": ["1.0"],
    "deprecatedVersions": []
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Unsupported Version
```http
GET /api/artikel
API-Version: 2.0

HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Unsupported API version: 2.0",
  "supportedVersions": ["1.0"],
  "currentVersion": "1.0"
}
```

### Deprecated Version Warning
```http
GET /api/artikel
API-Version: 0.9

HTTP/1.1 200 OK
API-Version: 1.0
X-API-Version-Warning: Version 0.9 is deprecated
```

## Testing

Das Versioning System wird durch `VersioningTest.kt` getestet:

```bash
./gradlew test --tests "at.mocode.VersioningTest"
```

## Implementierte DTOs

Folgende DTOs wurden bereits mit Versionierung ausgestattet:

- ✅ `ArtikelDto`, `CreateArtikelDto`, `UpdateArtikelDto`
- ✅ `VereinDto`, `CreateVereinDto`, `UpdateVereinDto`

### Noch zu implementieren:

- `AbteilungDto`
- `BewerbDto`
- `DomaeneDto`
- `StammdatenDto`
- `TurnierDto`
- `VeranstaltungDto`
- `CommonDto` (alle Klassen)
- `SpecializedDto`

## Nächste Schritte

1. Alle verbleibenden DTOs mit Versionierung ausstatten
2. API Routes auf DTO-Verwendung umstellen
3. Versioning Plugin in Application.kt aktivieren
4. Client-seitige Version-Header Implementation
5. Monitoring für Version-Usage implementieren
