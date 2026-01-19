# Frontend Architecture & Modularization Strategy

**Status:** DRAFT
**Last Updated:** 2026-01-19
**Context:** Migration to Clean Architecture & Feature Modules

---

## 1. Overview
The frontend architecture of **Meldestelle** is based on **Kotlin Multiplatform (KMP)** with **Compose Multiplatform** for UI. We follow a strict **Clean Architecture** approach to ensure testability, scalability, and separation of concerns.

## 2. Module Structure
The project is organized into the following layers:

### 2.1 Core Modules (`frontend/core`)
Reusable components that are agnostic of specific business features.
*   `core-network`: Central HTTP Client configuration (Auth, Logging, ContentNegotiation).
*   `core-sync`: Generic synchronization logic (`SyncManager`, `SyncableRepository`).
*   `core-ui`: Shared UI components and design system.

### 2.2 Feature Modules (`frontend/features`)
Each business domain (e.g., `ping`, `auth`, `events`) resides in its own module.
A feature module MUST follow the **Clean Architecture** package structure:

*   `at.mocode.{feature}.feature.domain`
    *   **Entities:** Pure data classes.
    *   **Interfaces:** Repository interfaces, Service interfaces.
    *   **Use Cases:** Business logic (optional, for complex logic).
*   `at.mocode.{feature}.feature.data`
    *   **Implementations:** Repository implementations, API Clients.
    *   **DTOs:** Data Transfer Objects (if different from domain entities).
*   `at.mocode.{feature}.feature.presentation`
    *   **ViewModels:** State management.
    *   **Screens:** Composable functions.
*   `at.mocode.{feature}.feature.di`
    *   **Koin Module:** Dependency injection configuration.

### 2.3 Shells (`frontend/shells`)
Application entry points that wire everything together.
*   `meldestelle-portal`: The main web/desktop application.

## 3. Migration Strategy (Transition Phase)
We are currently migrating from a monolithic `clients` package structure to modular feature modules.

**Rules for Migration:**
1.  **New Features:** Must be implemented directly in `frontend/features/{name}` using the Clean Architecture structure.
2.  **Existing Features:** Will be migrated incrementally.
3.  **Coexistence:** During the transition, legacy code in `clients/` is permitted but deprecated.
4.  **Dependency Injection:** Legacy code must use the new Koin modules if available.
5.  **No Ghost Classes:** Do not duplicate classes. If a class is moved to a feature module, delete the old one in `clients/`.

## 4. Key Decisions (ADRs)

### ADR-001: Sync Logic Decoupling
*   **Decision:** ViewModels must not depend directly on `SyncManager`.
*   **Reason:** To allow easier testing and to hide the complexity of the generic sync mechanism.
*   **Implementation:** Introduce a domain service interface (e.g., `PingSyncService`) that wraps the `SyncManager` call.

### ADR-002: Feature Module Isolation
*   **Decision:** Feature modules should not depend on each other directly if possible.
*   **Communication:** Use shared Core modules or loose coupling via interfaces/events if cross-feature communication is needed.

---

**Approved by:** Lead Architect
