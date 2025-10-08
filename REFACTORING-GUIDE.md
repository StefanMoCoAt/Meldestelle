# Gradle Build Refactoring Guide

## Overview
This guide documents the comprehensive Gradle build optimization completed in Phases 1-5, including practical refactoring patterns for all remaining modules.

---

## ✅ Completed Changes

### Phase 1: Bundles & Version Catalog
**File:** `gradle/libs.versions.toml`

#### Added Missing Libraries:
- `slf4j-api` (version 2.0.16)
- `kotlin-reflect` (uses kotlin version reference)

#### Added 5 New Complete Bundles:

1. **`ktor-server-complete`** - Full Ktor server stack (14 dependencies)
   - Includes: core, netty, content-negotiation, serialization, status-pages, cors, default-headers, auth, auth-jwt, call-logging, metrics-micrometer, openapi, swagger, rate-limit

2. **`spring-boot-service-complete`** - Full Spring Boot service stack (12 dependencies)
   - Includes: web, validation, actuator, security, oauth2-client, oauth2-resource-server, oauth2-jose, data-jpa, data-redis, micrometer-prometheus, tracing-bridge-brave, zipkin-reporter-brave

3. **`database-complete`** - Complete database stack (8 dependencies)
   - Includes: exposed-core, exposed-dao, exposed-jdbc, exposed-kotlin-datetime, postgresql-driver, hikari-cp, flyway-core, flyway-postgresql

4. **`testing-kmp`** - KMP testing stack (8 dependencies)
   - Includes: kotlin-test, junit-jupiter-api, junit-jupiter-engine, junit-jupiter-params, junit-platform-launcher, mockk, assertj-core, kotlinx-coroutines-test

5. **`monitoring-complete`** - Complete monitoring stack (8 dependencies)
   - Includes: actuator, micrometer-prometheus, tracing-bridge-brave, zipkin-reporter-brave, zipkin-sender-okhttp3, kotlin-logging-jvm, logback-classic, slf4j-api

### Phase 2: Root Build File
**File:** `build.gradle.kts`

#### Enhancements:
- ✅ Added `allprojects` configuration block with group, version, and common repositories
- ✅ Added dependency-analysis plugin (version 2.6.1)
- ✅ Added versions plugin (version 0.51.0)
- ✅ Updated wrapper task to Gradle 9.1.0

### Phase 3: Convention Plugins
**Directory:** `buildSrc/src/main/kotlin/`

#### Created 3 Convention Plugins:

1. **`ktor-server-conventions.gradle.kts`**
   - For Ktor server modules
   - Applies: kotlin-jvm, ktor, serialization plugins
   - Configures: Java 21 toolchain, compiler options, test configuration

2. **`spring-boot-service-conventions.gradle.kts`**
   - For Spring Boot service modules
   - Applies: kotlin-jvm, spring-boot, dependency-management, kotlin-spring, kotlin-jpa plugins
   - Configures: Java 21 toolchain, compiler options, test configuration, JPA all-open

3. **`kotlin-multiplatform-conventions.gradle.kts`**
   - For KMP modules (clients, shared)
   - Applies: kotlin-multiplatform, serialization plugins
   - Configures: Java 21 toolchain, compiler options for all targets

### Phase 5: Gradle Properties
**File:** `gradle.properties`

#### Added Kotlin Compiler Optimizations:
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

## 🔄 Refactoring Patterns

### Pattern 1: Spring Boot Services
**Applies to:** Gateway, Auth-Server, Monitoring-Server, Ping-Service, and all Spring Boot-based services

#### Before:
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
    // ... more dependencies
}
```

#### After:
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

    // Project dependencies
    implementation(projects.core.coreUtils)
    implementation(projects.platform.platformDependencies)

    // Complete bundles
    implementation(libs.bundles.spring.boot.service.complete)
    implementation(libs.bundles.resilience)
    implementation(libs.bundles.jackson.kotlin)
    implementation(libs.bundles.logging)

    // Specific dependencies
    implementation(libs.kotlin.reflect)
    // ... other specific dependencies

    // Tests
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
}
```

**Benefits:**
- ~30-40% reduction in lines of code
- No manual toolchain/compiler configuration
- Centralized dependency management through bundles
- Consistent configuration across all Spring Boot services

---

### Pattern 2: Ktor Server Services
**Applies to:** Any Ktor-based backend services (currently not in project, but pattern available)

#### Usage:
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

    // Use complete Ktor bundle
    implementation(libs.bundles.ktor.server.complete)

    // Or use specific bundles for fine-grained control
    implementation(libs.bundles.ktor.server.common)
    implementation(libs.bundles.ktor.server.security)
    implementation(libs.bundles.ktor.server.observability)

    // Database
    implementation(libs.bundles.database.complete)

    // Monitoring
    implementation(libs.bundles.monitoring.complete)

    // Tests
    testImplementation(libs.bundles.testing.kmp)
    testImplementation(libs.ktor.server.tests)
}
```

---

### Pattern 3: Kotlin Multiplatform Modules
**Applies to:** clients/app, clients/ping-feature, clients/auth-feature, clients/shared, core/core-utils, core/core-domain

#### Before:
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

#### After:
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
            // Use bundles
            implementation(libs.bundles.kotlinx.core)
            implementation(libs.bundles.ktor.client.common)
            implementation(libs.bundles.compose.common)

            // Compose (from plugin)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}
```

**Benefits:**
- Removes toolchain and compiler configuration boilerplate
- Bundles group related dependencies
- Cleaner and more maintainable

---

## 📋 Module Refactoring Checklist

### Infrastructure Modules

#### ✅ Gateway (Completed - Example)
- Pattern: Spring Boot Service
- Lines reduced: 113 → 90 (20%)

#### 🔲 Auth-Server
- Pattern: Spring Boot Service
- Replace plugins with `spring-boot-service-conventions`
- Use `spring-boot-service-complete`, `resilience`, `logging` bundles

#### 🔲 Messaging-Config
- Pattern: Spring Boot Service
- Use `spring-boot-service-complete`, `kafka-config` bundles

#### 🔲 Redis-Cache
- Pattern: Spring Boot Service
- Use `spring-boot-service-complete`, `redis-cache` bundles

#### 🔲 Redis-Event-Store
- Pattern: Spring Boot Service
- Use `spring-boot-service-complete`, `redis-cache` bundles

#### 🔲 Monitoring-Server
- Pattern: Spring Boot Service
- Use `spring-boot-service-complete`, `monitoring-complete` bundles

### Service Modules

#### ✅ Ping-Service (Completed - Example)
- Pattern: Spring Boot Service
- Lines reduced: 79 → 50 (37%)

#### 🔲 Ping-API
- Pattern: Kotlin Multiplatform (if KMP) or Kotlin JVM
- Use `kotlinx-core` bundle

### Client Modules

#### 🔲 clients/app
- Pattern: Kotlin Multiplatform
- Use `kotlin-multiplatform-conventions`
- Use `kotlinx-core`, `compose-common` bundles

#### 🔲 clients/ping-feature
- Pattern: Kotlin Multiplatform
- Already uses some bundles, ensure all are used

#### 🔲 clients/auth-feature
- Pattern: Kotlin Multiplatform
- Use `kotlin-multiplatform-conventions`
- Use `kotlinx-core`, `ktor-client-common`, `compose-common` bundles

#### 🔲 clients/shared modules
- Pattern: Kotlin Multiplatform
- Use `kotlin-multiplatform-conventions`
- Use `kotlinx-core` bundle

### Core & Platform Modules

#### 🔲 core/core-utils
- Pattern: Kotlin Multiplatform
- Use `kotlin-multiplatform-conventions`
- Use `kotlinx-core` bundle

#### 🔲 core/core-domain
- Pattern: Kotlin Multiplatform
- Use `kotlin-multiplatform-conventions`
- Use `kotlinx-core` bundle

#### 🔲 platform/platform-testing
- Pattern: Kotlin JVM
- Use `testing-kmp` or `testing-jvm` bundles

---

## 🚀 Recommended Build Commands

### Dependency Analysis
```bash
./gradlew buildHealth
```

### Check for Dependency Updates
```bash
./gradlew dependencyUpdates
```

### Build with Scan
```bash
./gradlew build --scan
```

### Dry Run for Task Dependencies
```bash
./gradlew :services:ping:ping-service:build --dry-run
```

### Measure Build Performance
```bash
time ./gradlew clean build
```

---

## 📊 Expected Benefits

### Build Performance
- **Incremental compilation**: ~20-40% faster rebuilds
- **Configuration cache**: ~30-50% faster configuration phase (when enabled)
- **Parallel execution**: Better utilization of multi-core systems

### Maintainability
- **Reduced duplication**: Convention plugins eliminate repetitive configuration
- **Centralized versioning**: Single source of truth in `libs.versions.toml`
- **Easier updates**: Update dependency versions in one place

### Code Metrics
- **Average reduction**: 20-40% fewer lines per build file
- **Consistency**: All modules follow same patterns
- **Type safety**: Version catalog provides IDE support and compile-time checking

---

## ⚠️ Important Notes

### Configuration Cache
Currently disabled due to JS test serialization issues. Can be re-enabled once resolved:
```properties
org.gradle.configuration-cache=true
```

### WASM Support
Optional, enable via:
```properties
enableWasm=true
```

### Incremental Refactoring
- Refactor one module at a time
- Test after each change
- Use demonstrated examples as templates

---

## 📚 References

- **Gradle Version Catalogs**: https://docs.gradle.org/current/userguide/platforms.html
- **Convention Plugins**: https://docs.gradle.org/current/samples/sample_convention_plugins.html
- **Kotlin DSL**: https://docs.gradle.org/current/userguide/kotlin_dsl.html
