# Gradle Build Refactoring-Leitfaden

## Überblick
Dieser Leitfaden dokumentiert die umfassende Gradle Build-Optimierung, die in den Phasen 1-5 abgeschlossen wurde, einschließlich praktischer Refactoring-Patterns für alle verbleibenden Module.

---

## ✅ Abgeschlossene Änderungen

### Phase 1: Bundles & Version Catalog
**Datei:** `gradle/libs.versions.toml`

#### Fehlende Bibliotheken hinzugefügt:
- `slf4j-api` (Version 2.0.16)
- `kotlin-reflect` (verwendet Kotlin-Versionsreferenz)

#### 5 neue Complete Bundles hinzugefügt:

1. **`ktor-server-complete`** - Vollständiger Ktor-Server-Stack (14 Abhängigkeiten)
   - Enthält: core, netty, content-negotiation, serialization, status-pages, cors, default-headers, auth, auth-jwt, call-logging, metrics-micrometer, openapi, swagger, rate-limit

2. **`spring-boot-service-complete`** - Vollständiger Spring Boot Service-Stack (12 Abhängigkeiten)
   - Enthält: web, validation, actuator, security, oauth2-client, oauth2-resource-server, oauth2-jose, data-jpa, data-redis, micrometer-prometheus, tracing-bridge-brave, zipkin-reporter-brave

3. **`database-complete`** - Vollständiger Datenbank-Stack (8 Abhängigkeiten)
   - Enthält: exposed-core, exposed-dao, exposed-jdbc, exposed-kotlin-datetime, postgresql-driver, hikari-cp, flyway-core, flyway-postgresql

4. **`testing-kmp`** - KMP-Test-Stack (8 Abhängigkeiten)
   - Enthält: kotlin-test, junit-jupiter-api, junit-jupiter-engine, junit-jupiter-params, junit-platform-launcher, mockk, assertj-core, kotlinx-coroutines-test

5. **`monitoring-complete`** - Vollständiger Monitoring-Stack (8 Abhängigkeiten)
   - Enthält: actuator, micrometer-prometheus, tracing-bridge-brave, zipkin-reporter-brave, zipkin-sender-okhttp3, kotlin-logging-jvm, logback-classic, slf4j-api

### Phase 2: Root Build-Datei
**Datei:** `build.gradle.kts`

#### Verbesserungen:
- ✅ `allprojects`-Konfigurationsblock mit Gruppe, Version und gemeinsamen Repositories hinzugefügt
- ✅ Dependency-Analysis-Plugin hinzugefügt (Version 2.6.1)
- ✅ Versions-Plugin hinzugefügt (Version 0.51.0)
- ✅ Wrapper-Task auf Gradle 9.1.0 aktualisiert

### Phase 3: Convention Plugins
**Verzeichnis:** `buildSrc/src/main/kotlin/`

#### 3 Convention Plugins erstellt:

1. **`ktor-server-conventions.gradle.kts`**
   - Für Ktor-Server-Module
   - Wendet an: kotlin-jvm, ktor, serialization Plugins
   - Konfiguriert: Java 21 Toolchain, Compiler-Optionen, Test-Konfiguration

2. **`spring-boot-service-conventions.gradle.kts`**
   - Für Spring Boot Service-Module
   - Wendet an: kotlin-jvm, spring-boot, dependency-management, kotlin-spring, kotlin-jpa Plugins
   - Konfiguriert: Java 21 Toolchain, Compiler-Optionen, Test-Konfiguration, JPA All-Open

3. **`kotlin-multiplatform-conventions.gradle.kts`**
   - Für KMP-Module (clients, shared)
   - Wendet an: kotlin-multiplatform, serialization Plugins
   - Konfiguriert: Java 21 Toolchain, Compiler-Optionen für alle Targets

### Phase 5: Gradle Properties
**Datei:** `gradle.properties`

#### Kotlin-Compiler-Optimierungen hinzugefügt:
```properties
kotlin.incremental=true
kotlin.incremental.multiplatform=true
kotlin.incremental.js=true
kotlin.caching.enabled=true
kotlin.compiler.execution.strategy=in-process
kotlin.compiler.preciseCompilationResultsBackup=true
kotlin.stdlib.default.dependency=true
```

---

## 🔄 Refactoring-Patterns

### Pattern 1: Spring Boot Services
**Gilt für:** Gateway, Auth-Server, Monitoring-Server, Ping-Service und alle Spring Boot-basierten Services

#### Vorher:
```kotlin
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

springBoot {
    mainClass.set("com.example.MainKt")
    buildInfo()
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.reactor)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)
    implementation("ch.qos.logback:logback-classic")
    implementation("ch.qos.logback:logback-core")
    implementation("org.slf4j:slf4j-api")
    // ... weitere Abhängigkeiten
}
```

#### Nachher:
```kotlin
plugins {
    id("spring-boot-service-conventions")
}

springBoot {
    mainClass.set("com.example.MainKt")
}

dependencies {
    // Platform BOM
    implementation(platform(projects.platform.platformBom))

    // Projekt-Abhängigkeiten
    implementation(projects.core.coreUtils)
    implementation(projects.platform.platformDependencies)

    // Complete Bundles
    implementation(libs.bundles.spring.boot.service.complete)
    implementation(libs.bundles.resilience)
    implementation(libs.bundles.jackson.kotlin)
    implementation(libs.bundles.logging)

    // Spezifische Abhängigkeiten
    implementation(libs.kotlin.reflect)
    // ... andere spezifische Abhängigkeiten

    // Tests
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
}
```

**Vorteile:**
- ~30-40% Reduktion der Codezeilen
- Keine manuelle Toolchain-/Compiler-Konfiguration
- Zentralisiertes Dependency-Management durch Bundles
- Konsistente Konfiguration über alle Spring Boot Services

---

### Pattern 2: Ktor Server Services
**Gilt für:** Alle Ktor-basierten Backend-Services (derzeit nicht im Projekt, aber Pattern verfügbar)

#### Verwendung:
```kotlin
plugins {
    id("ktor-server-conventions")
}

ktor {
    fatJar {
        archiveFileName.set("service-name.jar")
    }
}

dependencies {
    implementation(platform(projects.platform.platformBom))

    // Verwende Complete Ktor Bundle
    implementation(libs.bundles.ktor.server.complete)

    // Oder verwende spezifische Bundles für feinkörnige Kontrolle
    implementation(libs.bundles.ktor.server.common)
    implementation(libs.bundles.ktor.server.security)
    implementation(libs.bundles.ktor.server.observability)

    // Datenbank
    implementation(libs.bundles.database.complete)

    // Monitoring
    implementation(libs.bundles.monitoring.complete)

    // Tests
    testImplementation(libs.bundles.testing.kmp)
    testImplementation(libs.ktor.server.tests)
}
```

---

### Pattern 3: Kotlin Multiplatform Module
**Gilt für:** clients/app, clients/ping-feature, clients/auth-feature, clients/shared, core/core-utils, core/core-domain

#### Vorher:
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)

    jvm()
    js(IR) { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }
}
```

#### Nachher:
```kotlin
plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    js(IR) { browser() }

    sourceSets {
        commonMain.dependencies {
            // Verwende Bundles
            implementation(libs.bundles.kotlinx.core)
            implementation(libs.bundles.ktor.client.common)
            implementation(libs.bundles.compose.common)

            // Compose (vom Plugin)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}
```

**Vorteile:**
- Entfernt Toolchain- und Compiler-Konfiguration-Boilerplate
- Bundles gruppieren verwandte Abhängigkeiten
- Sauberer und wartbarer

---

## 📋 Modul-Refactoring-Checkliste

### Infrastruktur-Module

#### ✅ Gateway (Abgeschlossen - Beispiel)
- Pattern: Spring Boot Service
- Zeilen reduziert: 113 → 90 (20%)

#### 🔲 Auth-Server
- Pattern: Spring Boot Service
- Ersetze Plugins durch `spring-boot-service-conventions`
- Verwende `spring-boot-service-complete`, `resilience`, `logging` Bundles

#### 🔲 Messaging-Config
- Pattern: Spring Boot Service
- Verwende `spring-boot-service-complete`, `kafka-config` Bundles

#### 🔲 Redis-Cache
- Pattern: Spring Boot Service
- Verwende `spring-boot-service-complete`, `redis-cache` Bundles

#### 🔲 Redis-Event-Store
- Pattern: Spring Boot Service
- Verwende `spring-boot-service-complete`, `redis-cache` Bundles

#### 🔲 Monitoring-Server
- Pattern: Spring Boot Service
- Verwende `spring-boot-service-complete`, `monitoring-complete` Bundles

### Service-Module

#### ✅ Ping-Service (Abgeschlossen - Beispiel)
- Pattern: Spring Boot Service
- Zeilen reduziert: 79 → 50 (37%)

#### 🔲 Ping-API
- Pattern: Kotlin Multiplatform (falls KMP) oder Kotlin JVM
- Verwende `kotlinx-core` Bundle

### Client-Module

#### 🔲 clients/app
- Pattern: Kotlin Multiplatform
- Verwende `kotlin-multiplatform-conventions`
- Verwende `kotlinx-core`, `compose-common` Bundles

#### 🔲 clients/ping-feature
- Pattern: Kotlin Multiplatform
- Verwendet bereits einige Bundles, stelle sicher, dass alle verwendet werden

#### 🔲 clients/auth-feature
- Pattern: Kotlin Multiplatform
- Verwende `kotlin-multiplatform-conventions`
- Verwende `kotlinx-core`, `ktor-client-common`, `compose-common` Bundles

#### 🔲 clients/shared Module
- Pattern: Kotlin Multiplatform
- Verwende `kotlin-multiplatform-conventions`
- Verwende `kotlinx-core` Bundle

### Core & Platform Module

#### 🔲 core/core-utils
- Pattern: Kotlin Multiplatform
- Verwende `kotlin-multiplatform-conventions`
- Verwende `kotlinx-core` Bundle

#### 🔲 core/core-domain
- Pattern: Kotlin Multiplatform
- Verwende `kotlin-multiplatform-conventions`
- Verwende `kotlinx-core` Bundle

#### 🔲 platform/platform-testing
- Pattern: Kotlin JVM
- Verwende `testing-kmp` oder `testing-jvm` Bundles

---

## 🚀 Empfohlene Build-Befehle

### Dependency-Analyse
```bash
./gradlew buildHealth
```

### Nach Dependency-Updates suchen
```bash
./gradlew dependencyUpdates
```

### Build mit Scan
```bash
./gradlew build --scan
```

### Dry Run für Task-Abhängigkeiten
```bash
./gradlew :services:ping:ping-service:build --dry-run
```

### Build-Performance messen
```bash
time ./gradlew clean build
```

---

## 📊 Erwartete Vorteile

### Build-Performance
- **Inkrementelle Kompilierung**: ~20-40% schnellere Rebuilds
- **Configuration Cache**: ~30-50% schnellere Konfigurationsphase (wenn aktiviert)
- **Parallele Ausführung**: Bessere Auslastung von Multi-Core-Systemen

### Wartbarkeit
- **Reduzierte Duplikation**: Convention Plugins eliminieren sich wiederholende Konfiguration
- **Zentralisierte Versionierung**: Single Source of Truth in `libs.versions.toml`
- **Einfachere Updates**: Dependency-Versionen an einem Ort aktualisieren

### Code-Metriken
- **Durchschnittliche Reduktion**: 20-40% weniger Zeilen pro Build-Datei
- **Konsistenz**: Alle Module folgen denselben Patterns
- **Type-Safety**: Version Catalog bietet IDE-Support und Compile-Time-Checking

---

## ⚠️ Wichtige Hinweise

### Configuration Cache
Derzeit deaktiviert aufgrund von JS-Test-Serialisierungsproblemen. Kann wieder aktiviert werden, sobald gelöst:
```properties
org.gradle.configuration-cache=true
```

### WASM-Unterstützung
Optional, aktivierbar über:
```properties
enableWasm=true
```

### Inkrementelles Refactoring
- Ein Modul nach dem anderen refactoren
- Nach jeder Änderung testen
- Demonstrierte Beispiele als Templates verwenden

---

## 📚 Referenzen

- **Gradle Version Catalogs**: https://docs.gradle.org/current/userguide/platforms.html
- **Convention Plugins**: https://docs.gradle.org/current/samples/sample_convention_plugins.html
- **Kotlin DSL**: https://docs.gradle.org/current/userguide/kotlin_dsl.html
