---
type: Guide
status: ACTIVE
owner: Frontend Expert
last_update: 2026-01-17
---

# Feature-Implementierungs-Guide

Dieser Guide beschreibt das Standard-Vorgehen zur Implementierung eines neuen Features im Meldestelle-Frontend, basierend auf der Referenz-Implementierung des `ping-feature`.

## Architektur-Muster

Jedes Feature folgt einer strikten Trennung und nutzt Dependency Injection (Koin).

### 1. API Client (Network Layer)

Anstatt einen eigenen `HttpClient` zu instanziieren, nutzen wir den zentralen, authentifizierten Client aus dem Core.

**Muster:**
*   Erstelle ein Interface für die API (z.B. `PingApi` im Contract-Modul).
*   Implementiere den Client im Feature-Modul und lasse dir den `HttpClient` injizieren.

```kotlin
// Feature-Modul: src/commonMain/.../MyFeatureApiClient.kt
class MyFeatureApiClient(private val client: HttpClient) : MyFeatureApi {
    override suspend fun getData(): MyData {
        // Der 'client' ist bereits mit BaseURL und Auth-Token konfiguriert
        return client.get("/api/my-feature/data").body()
    }
}
```

### 2. Dependency Injection (Koin)

Jedes Feature definiert sein eigenes Koin-Modul.

**Muster:**
*   Nutze `named("apiClient")` um den authentifizierten Client zu erhalten.
*   Registriere den API-Client und das ViewModel.

```kotlin
// Feature-Modul: src/commonMain/.../di/MyFeatureModule.kt
val myFeatureModule = module {
    // API Client mit Shared HttpClient
    single<MyFeatureApi> { MyFeatureApiClient(get(named("apiClient"))) }

    // ViewModel
    factory { MyFeatureViewModel(get()) }
}
```

### 3. ViewModel

Das ViewModel erhält die API (oder das Repository) via Konstruktor-Injektion.

```kotlin
class MyFeatureViewModel(private val api: MyFeatureApi) : ViewModel() {
    // ...
}
```

### 4. Integration (Shell)

Das Feature-Modul muss in der Shell (z.B. `meldestelle-portal`) registriert werden.

1.  **Gradle:** `implementation(projects.frontend.features.myFeature)` in `build.gradle.kts` der Shell.
2.  **Koin Init:** Füge das Modul zur `initKoin`-Liste in `main.kt` (Desktop & Web) hinzu.

## Web-Spezifika (Worker)

Falls das Feature Web-Worker benötigt (z.B. für SQLDelight), muss sichergestellt werden, dass diese korrekt kopiert werden.

*   **Build-Script:** In der Shell `build.gradle.kts` muss der Copy-Task angepasst werden, falls neue Worker hinzukommen.
*   **Pfad:** `rootProject.layout.buildDirectory.dir("js/packages/${rootProject.name}-frontend-shells-meldestelle-portal/kotlin")`

## Referenz

Siehe `frontend/features/ping-feature` für die vollständige Implementierung inkl. Tests.
