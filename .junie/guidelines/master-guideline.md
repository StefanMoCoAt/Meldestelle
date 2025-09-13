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

## 2. Coding Standards & Code-Qualität

Detaillierte Coding-Standards und Qualitätsrichtlinien finden Sie in:
**→ [Coding Standards](./project-standards/coding-standards.md)**

Kernpunkte:
- **Primärsprache:** Kotlin (JVM/Multiplatform) mit Java 21+ Kompatibilität
- **Namenskonventionen:** PascalCase für Klassen, camelCase für Funktionen
- **Value Classes:** Typsichere Wrapper für primitive Typen
- **Result-Pattern:** Für erwartbare Geschäftsfehler
- **Structured Logging:** Mit Korrelations-IDs

---

## 3. Architecture Principles & Backend-Entwicklung

Detaillierte Architektur-Prinzipien und Backend-Entwicklungsrichtlinien finden Sie in:
**→ [Architecture Principles](./project-standards/architecture-principles.md)**

Kernpunkte:
- **Clean Architecture:** 4-Layer-Struktur (api, application, domain, infrastructure)
- **DDD:** Domain-Driven Design mit Bounded Contexts
- **EDA:** Event-Driven Architecture mit Kafka
- **Repository-Pattern:** Alle Methoden verwenden Result-Pattern

---

## 4. Frontend-Entwicklung & Multiplatform

Detaillierte Frontend-Entwicklungsrichtlinien finden Sie in:
**→ [Web App Guideline](./technology-guides/web-app-guideline.md)**

Kernpunkte:
- **MVVM-Pattern:** Model-View-ViewModel für UI-Struktur
- **Kotlin Multiplatform:** Code-Sharing zwischen Desktop und Web
- **Compose Multiplatform:** Deklarative UI mit @Composable-Funktionen
- **Feature-basierte Struktur:** Vertikale Schnitte nach Fachlichkeit

---

## 5. Testing Standards

Detaillierte Testing-Standards finden Sie in:
**→ [Testing Standards](./project-standards/testing-standards.md)**

Kernpunkte:
- **Test-Pyramide:** 80%+ Unit-Tests, Integrationstests für externe Systeme
- **Testcontainers:** Goldstandard für Infrastruktur-Tests
- **Result-Pattern:** Tests für Success- und Failure-Cases
- **Debug-Logs:** `[DEBUG_LOG]`-Präfix für Test-Ausgaben

---

## 6. Docker & Infrastructure

Detaillierte Docker- und Infrastruktur-Richtlinien finden Sie in:
**→ [Docker Guidelines](./technology-guides/docker/)**

Kernpunkte:
- **Docker-Architektur:** Microservices mit Service Discovery
- **Zentrale Versionsverwaltung:** Single Source of Truth
- **Monitoring:** Prometheus & Grafana
- **Security:** Non-Root-Container, SSL/TLS everywhere

---

## 7. Documentation Standards

Detaillierte Dokumentationsstandards finden Sie in:
**→ [Documentation Standards](./project-standards/documentation-standards.md)**

Kernpunkte:
- **Sprache:** README-Dateien auf Deutsch, Code-Kommentare je nach Kontext
- **Struktur:** Einheitliche README-Template
- **API-Docs:** OpenAPI-Annotationen mit deutschen Beschreibungen
- **Versionierung:** Dokumentation wird mit Code versioniert
