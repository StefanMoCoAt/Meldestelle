---
type: Reference
status: ACTIVE
owner: Lead Architect
tags: [kotlin, release-notes, tech-stack]
---

# What's new in Kotlin 2.3.0

**Quelle:** [Original Kotlin Documentation](https://kotlinlang.org/docs/whatsnew23.html)
**Datum des Dokuments:** 16. Dezember 2025
**Kontext:** Dieses Dokument dient als Referenz für die im Projekt verwendete Kotlin-Version.

---

The Kotlin 2.3.0 release is out! Here are the main highlights:

*   **Language:** More stable and default features, unused return value checker, explicit backing fields, and changes to context-sensitive resolution.
*   **Kotlin/JVM:** Support for Java 25.
*   **Kotlin/Native:** Improved interop through Swift export, faster build time for release tasks, C and Objective-C library import in Beta.
*   **Kotlin/Wasm:** Fully qualified names and new exception handling proposal enabled by default, as well as new compact storage for Latin-1 characters.
*   **Kotlin/JS:** New experimental suspend function export, `LongArray` representation, unified companion object access, and more.
*   **Gradle:** Compatibility with Gradle 9.0 and a new API for registering generated sources.
*   **Compose compiler:** Stack traces for minified Android applications.
*   **Standard library:** Stable time tracking functionality and improved UUID generation and parsing.

## Language

Kotlin 2.3.0 focuses on feature stabilization, introduces a new mechanism for detecting unused return values, and improves context-sensitive resolution.

### Stable features

The following features have now graduated to Stable:
*   Support for nested type aliases
*   Data-flow-based exhaustiveness checks for `when` expressions

### Features enabled by default
*   Support for `return` statements in expression bodies with explicit return types is now enabled by default.

### Experimental: Unused return value checker
Kotlin 2.3.0 introduces the unused return value checker to help prevent ignored results.

### Experimental: Explicit backing fields
A new syntax for explicitly declaring the underlying field that holds a property's value, simplifying the common backing properties pattern.

## Kotlin/JVM: Support for Java 25
Starting with Kotlin 2.3.0, the compiler can generate classes containing Java 25 bytecode.

## Kotlin/Native
*   **Improved Swift Export:** Direct mapping for native enum classes and variadic function parameters.
*   **C and Objective-C Library Import is in Beta:** Better diagnostics for binary compatibility issues.
*   **Faster Build Time:** Up to 40% faster release builds, especially for iOS targets.

## Kotlin/Wasm
*   **Fully Qualified Names Enabled by Default:** `KClass.qualifiedName` is now available at runtime without extra configuration.
*   **Compact Storage for Latin-1 Characters:** Reduces metadata and binary size.
*   **New Exception Handling for `wasmWasi`:** Enabled by default for better compatibility with modern WebAssembly runtimes.

## Kotlin/JS
*   **Experimental Suspend Function Export:** Export suspend functions directly to JavaScript using `@JsExport`.
*   **`BigInt64Array` for `LongArray`:** Simplifies interop with JavaScript APIs that use typed arrays.
*   **Unified Companion Object Access:** Consistent access to companion objects in interfaces across all JS module systems.

## Gradle
*   Fully compatible with Gradle 7.6.3 through 9.0.0.
*   New experimental API for registering generated sources.

## Standard library
*   **Stable Time Tracking:** `kotlin.time.Clock` and `kotlin.time.Instant` are now stable.
*   **Improved UUID Generation:** New functions like `Uuid.parseOrNull()`, `Uuid.generateV4()`, and `Uuid.generateV7()`.

## Compose compiler
*   **Stack Traces for Minified Android Apps:** The compiler now outputs ProGuard mappings for Compose stack traces when applications are minified by R8.
