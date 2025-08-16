# Gradle Dependency Resolution Fix für Docker Build

## Problemanalyse
Der Docker Build für den api-gateway Service schlug fehl mit folgendem Fehler:

```
> No matching variant of project :infrastructure:auth:auth-client was found. The consumer was configured to find a library for use during runtime, compatible with Java 21, packaged as a jar, preferably optimized for standard JVMs, and its dependencies declared externally, as well as attribute 'org.jetbrains.kotlin.platform.type' with value 'jvm' but:
  - No variants exist.
```

Das gleiche Problem trat auch bei `:infrastructure:monitoring:monitoring-client` auf.

## Grundursache
Die Bibliotheksmodule `auth-client` und `monitoring-client` waren nicht korrekt als Gradle-Bibliotheken konfiguriert und exponierten keine konsumierbare Varianten (API/Runtime) für abhängige Projekte wie das `api-gateway`.

## Angewendete Lösungen

### 1. Auth-Client Modul konfiguriert ✅

**Datei**: `/infrastructure/auth/auth-client/build.gradle.kts`

**Vorher**:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.boot)  // ❌ Falsch für Bibliotheksmodul
    alias(libs.plugins.spring.dependencyManagement)
}

// Manuelle JAR-Konfiguration erforderlich
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}
tasks.getByName<Jar>("jar") {
    enabled = true
}
```

**Nachher**:
```kotlin
plugins {
    `java-library`  // ✅ Erzeugt automatisch API/Runtime Varianten
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
    // Spring Boot Plugin entfernt ✅
    alias(libs.plugins.spring.dependencyManagement)
}

// JAR-Konfiguration automatisch durch java-library Plugin ✅
```

### 2. Monitoring-Client Modul konfiguriert ✅

**Datei**: `/infrastructure/monitoring/monitoring-client/build.gradle.kts`

**Vorher**:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependencyManagement)
    // Kein java-library Plugin ❌
}
```

**Nachher**:
```kotlin
plugins {
    `java-library`  // ✅ Hinzugefügt für Varianten-Exposition
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependencyManagement)
}
```

### 3. Gradle Projekt-Struktur vervollständigt ✅

Erstellt fehlende parent `build.gradle` Dateien für korrekte Multi-Modul-Struktur:

- `/infrastructure/build.gradle` ✅
- `/infrastructure/auth/build.gradle` ✅
- `/infrastructure/monitoring/build.gradle` ✅

Diese Dateien sind minimal und dienen als Container für Subprojekte:
```gradle
// Infrastructure Module Container
// This is a container module for infrastructure-related subprojects
```

## Technische Details

### Was das `java-library` Plugin bewirkt:
- **Automatische Varianten-Erstellung**: Erstellt `apiElements` und `runtimeElements` Konfigurationen
- **Konsumierbare Artefakte**: Andere Projekte können diese Module als Abhängigkeiten verwenden
- **Transitive Abhängigkeiten**: Korrekte Behandlung von API vs. Implementation Dependencies
- **JAR-Erstellung**: Automatisches Erstellen von Standard-JAR-Dateien (nicht executable)

### Warum Spring Boot Plugin entfernt wurde:
- Spring Boot Plugin ist für **ausführbare Anwendungen** gedacht, nicht für Bibliotheken
- Erzeugt `bootJar` statt Standard-JAR, was für Bibliotheken ungeeignet ist
- Verhindert die Erstellung konsumierbarer Gradle-Varianten

### Multi-Modul-Struktur:
```
infrastructure/
├── build.gradle                    # Container
├── auth/
│   ├── build.gradle               # Container
│   └── auth-client/
│       └── build.gradle.kts       # Bibliothek mit java-library
└── monitoring/
    ├── build.gradle               # Container
    └── monitoring-client/
        └── build.gradle.kts       # Bibliothek mit java-library
```

## Verifikation

### Gradle-Konfiguration prüfen:
```bash
# Projekt-Struktur anzeigen
./gradlew projects

# Abhängigkeiten anzeigen
./gradlew :infrastructure:gateway:dependencies

# Varianten prüfen
./gradlew :infrastructure:auth:auth-client:outgoingVariants
./gradlew :infrastructure:monitoring:monitoring-client:outgoingVariants
```

### Docker Build testen:
```bash
# Sauberer Build ohne Cache
docker-compose -f docker-compose.yml -f docker-compose.services.yml build --no-cache api-gateway

# Vollständiger Stack
docker-compose \
  -f docker-compose.yml \
  -f docker-compose.services.yml \
  -f docker-compose.clients.yml \
  up -d --build
```

## Erwartetes Ergebnis

Nach Anwendung dieser Konfigurationen sollten:

1. ✅ `auth-client` und `monitoring-client` korrekte Gradle-Varianten exponieren
2. ✅ `api-gateway` diese Module erfolgreich als Abhängigkeiten auflösen können
3. ✅ Docker Build ohne "No variants exist" Fehler durchlaufen
4. ✅ Alle Services korrekt starten und funktionieren

## Zusätzliche Hinweise

- **BOM-Management**: Die zentrale Versionierung über `platform-bom` bleibt unverändert
- **Dependency Management**: Spring Dependency Management Plugin sorgt für konsistente Versionen
- **Kotlin Multiplatform**: Core-Module verwenden weiterhin Kotlin Multiplatform Plugin
- **Testing**: Platform-Testing Bundle stellt einheitliche Test-Dependencies bereit

## Rollback (falls nötig)

Um die Änderungen rückgängig zu machen:
```bash
git checkout HEAD -- infrastructure/auth/auth-client/build.gradle.kts
git checkout HEAD -- infrastructure/monitoring/monitoring-client/build.gradle.kts
rm infrastructure/build.gradle
rm infrastructure/auth/build.gradle
rm infrastructure/monitoring/build.gradle
```

## Status: ✅ IMPLEMENTIERT

Alle Konfigurationsänderungen wurden angewendet und sind bereit für Testing.
