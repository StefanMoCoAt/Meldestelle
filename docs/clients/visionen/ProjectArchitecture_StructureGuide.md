# 🏗 Project Architecture & Structure Guide

> **"Code is liability. Structure is asset."**
> Wir bauen dieses System nicht für den schnellsten Start, sondern für die **Wartbarkeit über Jahre**, Offline-Fähigkeit und Skalierbarkeit über mehrere Teams hinweg.

-----

## 1\. Die Große Übersicht: The Monorepo Strategy

Wir organisieren Backend und Frontend in einem einzigen Repository (Monorepo).

### **Warum Monorepo? (Decision Record)**

* ❌ **Alternative:** Getrennte Repositories für Backend, Web-Frontend, Desktop-App.
* **Problem dabei:** "Version Hell". Backend ändert API v1 zu v2, aber Frontend-Repo ist noch auf v1. Refactorings über die ganze Kette sind schmerzhaft.
* ✅ **Unsere Entscheidung:** Monorepo.
  * **Atomic Commits:** Ein Pull Request enthält Backend-Änderungen UND die dazugehörige Frontend-Anpassung.
  * **Single Versioning:** Wir nutzen `gradle/libs.versions.toml` als einzige Quelle der Wahrheit für Library-Versionen (z.B. Kotlin Version) über das gesamte System hinweg.

-----

## 2\. Der "Deep Dive" in die Ordnerstruktur

Hier ist der detaillierte Aufriss unseres Dateisystems. Jeder Ordner hat einen spezifischen architektonischen Zweck.

```text
/my-project-root
│
├── ⚙️ docker-compose.yml       <-- Die lokale "Cloud". Startet DBs, Gateway & Services.
├── 📄 settings.gradle.kts      <-- Definiert die Module (Frontend & Backend).
├── 📂 gradle
│   └── libs.versions.toml      <-- 🛑 STOP! Hier werden Versionen definiert. Nirgendwo sonst.
│
├── 📂 backend                  <-- ARCHITEKTUR: Hexagonal / DDD
│   ├── 📂 gateway              <-- Der "Türsteher". Routing & Auth-Check.
│   ├── 📂 discovery            <-- Das "Telefonbuch" (Consul/Service Registry).
│   └── 📂 services             <-- Die Business Logic (Microservices)
│       ├── 📂 inventory-service
│       │   ├── 📄 Dockerfile   <-- Jedes Service ist ein isolierter Container!
│       │   └── 📂 src/main/kotlin/.../domain  <-- Reine Logik, kein Spring!
│       └── 📂 auth-service
│
└── 📂 frontend                 <-- ARCHITEKTUR: Kotlin Multiplatform (KMP)
    │
    ├── 📂 shells               <-- 💡 CONCEPT: "The Assembler"
    │   │   Das sind die ausführbaren Anwendungen. Sie enthalten KEINE Logik.
    │   │   Sie "kleben" nur Features zusammen und konfigurieren DI.
    │   │
    │   ├── 📂 warehouse-app    <-- Desktop-App (Windows/Linux) für Lageristen
    │   │   └── build.gradle.kts (bindet :features:inventory ein)
    │   └── 📂 admin-portal     <-- Web-App (JS/Wasm) für Management
    │       └── build.gradle.kts (bindet alle Features ein)
    │
    ├── 📂 features             <-- 💡 CONCEPT: "Vertical Slices" (Micro-Frontends)
    │   │   Hier passiert die Arbeit. Ein Feature gehört einem Team.
    │   │
    │   ├── 📂 inventory-feature
    │   │   ├── 📂 src/commonMain
    │   │   │   ├── 📂 api      <-- Public Interface (Der Vertrag nach außen)
    │   │   │   ├── 📂 ui       <-- Screens & Components (Internal)
    │   │   │   └── 📂 data     <-- Repository & SSoT (Internal)
    │   │   └── build.gradle.kts
    │   └── 📂 auth-feature
    │
    └── 📂 core                 <-- 💡 CONCEPT: "Shared Foundation"
        │   Code, der sich selten ändert, aber überall genutzt wird.
        │
        ├── 📂 design-system    <-- UI-Baukasten (Farben, Typo, Buttons)
        ├── 📂 network          <-- HTTP Clients & Auth-Interceptor
        ├── 📂 local-db         <-- SQLDelight Schemas (Die Offline-Wahrheit)
        └── 📂 auth             <-- OAuth2 Logik (Browser Bridge für Desktop)
```

-----

## 3\. Architectural Decision Records (ADRs)

Warum haben wir das so gebaut? Hier sind die Antworten auf die "Warum nicht X?" Fragen.

### ADR 001: Kotlin Multiplatform vs. Electron / Web-Wrapper

* **Kontext:** Wir brauchen eine Web-App UND eine Desktop-App.
* **Entscheidung:** Wir nutzen **Kotlin Multiplatform (Compose)**.
* **Begründung:**
  * *Performance:* Electron braucht pro App \~200MB RAM (Chromium Instanz). Unsere Desktop-Apps (Lager, Kasse) laufen auf schwacher Hardware. JVM/Native ist effizienter.
  * *Type Safety:* Wir teilen Business-Logik (Validation, SSoT) zwischen Web und Desktop. Mit JS/Electron müssten wir Logik duplizieren oder transpilen.
  * *Offline:* Echte SQL-Datenbank (SQLite) Integration ist in nativem Code robuster als im Browser-Storage.

### ADR 002: Multiple App Shells vs. One "Super-App"

* **Kontext:** Wir haben Lagerarbeiter, Kassierer und Manager.
* **Entscheidung:** Wir bauen **pro Rolle eine eigene "Shell"** (Executable).
* **Begründung:**
  * *Security (Web):* "Tree Shaking". Wenn der Code für "Admin-User-Löschen" gar nicht erst in der `warehouse-app.js` enthalten ist, kann er auch nicht gehackt werden.
  * *Focus (Desktop):* Die Lager-App startet schneller und hat weniger Bugs, weil sie den Code für das Rechnungswesen gar nicht lädt.
  * *Flexibilität:* Wir können Features wiederverwenden. Das Feature `auth-feature` ist in ALLEN Apps, `inventory-feature` nur in zweien.

### ADR 003: Single Source of Truth (SSoT) via Database

* **Kontext:** Desktop-Apps werden in Hallen mit schlechtem WLAN genutzt.
* **Entscheidung:** **Database First Architecture**.
* **Begründung:**
  * Klassisch (`UI -> API -> UI`) führt zu weißen Screens und Ladekreisen bei Netzschwankungen.
  * Wir nutzen `UI -> Local DB <- Sync -> API`.
  * Das UI zeigt **immer** Daten an (auch wenn sie 10 Minuten alt sind). Der User kann arbeiten. Sync passiert transparent im Hintergrund.

### ADR 004: Docker für alles (außer Desktop Runtime)

* **Kontext:** "Bei mir läuft's aber..." Probleme.
* **Entscheidung:** Das gesamte Backend + Web-Frontend Build-Pipeline läuft in Docker.
* **Begründung:**
  * Die `docker-compose.yml` ist die Wahrheit.
  * Für die Desktop-Entwicklung nutzen wir Gradle lokal, aber der Server, gegen den entwickelt wird, läuft im Container. Das garantiert Identität zwischen Dev und Prod.

-----

## 4\. Guidelines: Wo gehört mein Code hin?

Wenn du neuen Code schreibst, stelle dir diese Fragen:

### Q1: Ist es Business Logik (z.B. "Preis berechnen")?

* ➡️ Gehört in **`/backend/services/.../domain`** (Server-Side Validierung ist Pflicht).
* ➡️ UND optional in **`/frontend/features/.../domain`** (für schnelle UI-Feedback, aber Server hat das letzte Wort).

### Q2: Ist es ein UI-Element (z.B. "Runder Button")?

* ➡️ Gehört in **`/frontend/core/design-system`**.
* 🛑 *Stop\!* Baue keine Custom Buttons in deinem Feature-Ordner. Nutze das Design System. Wenn etwas fehlt, erweitere das Design System.

### Q3: Ich brauche Daten von einem anderen Service.

* **Szenario:** Im "Checkout" (Kasse) brauche ich den Produktnamen aus dem "Inventory".
* ❌ **Falsch:** `CheckoutService` ruft `InventoryService` Datenbank direkt ab.
* ✅ **Richtig (Backend):** `CheckoutService` ruft `InventoryService` via REST/gRPC über das Gateway.
* ✅ **Richtig (Frontend):** Das `Checkout-Feature` kennt das `Inventory-Feature` nicht. Es bekommt nur eine `productId`. Wenn es Details anzeigen muss, nutzt es entweder ein eigenes minimales Datenmodell oder fragt das Backend.

### Q4: Auth Token Handling

* ❌ **Niemals:** `httpClient.header("Authorization", token)` manuell aufrufen.
* ✅ **Immer:** Nutze den konfigurierten Client aus dem DI-Container: `get(named("apiClient"))`. Die Architektur kümmert sich um Refresh und Injection.

-----

## 5\. Das "Mental Model" für Entwickler

Stell dir unsere App wie einen **Lego-Baukasten** vor.

1.  **Core (Platte):** Das Fundament (Auth, Network, Design). Muss immer da sein.
2.  **Features (Steine):** Bunte Bausteine (Inventory, Cart, Profile). Sie berühren sich seitlich nicht (keine direkten Abhängigkeiten).
3.  **Shells (Modelle):** Das fertige Haus.
  * Haus A (Admin Portal) nutzt alle Steine.
  * Haus B (Lager App) nutzt nur die grünen Steine (Inventory).

Dein Job als Entwickler ist es meistens, **einen neuen Stein (Feature)** zu bauen oder einen bestehenden zu verbessern. Du musst dich selten um das Fundament oder das fertige Haus kümmern.
