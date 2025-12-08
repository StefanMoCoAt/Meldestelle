# ADR-0009: Final KMP Architecture

Status: Accepted

Kontext

Wir schließen die Architektur-Entscheidungen für das Frontend als Kotlin Multiplatform (KMP) Projekt ab und bestätigen die finalen Bausteine sowie die Modulaufteilung. Die Plattformen Web (JS/WASM) und JVM/Desktop werden unterstützt.

Entscheidung

1. Plattformen: Kotlin Multiplatform mit Targets Web (JS/WASM) und JVM/Desktop.
2. Dependency Injection: Koin als DI-Framework für gemeinsame und plattformspezifische Layer.
3. Persistenz (Offline-First): SQLDelight als lokale Datenbank (Single Source of Truth), synchronisiert im Hintergrund.
4. Modulaufteilung im Frontend:
   - shells: Ausführbare App-Shells (Bootstrap/Assembler, DI-Start, Plattformintegration)
   - features: Vertikale Slices mit UI, Domain-Logik und Navigation pro Feature
   - core: Gemeinsame Basis (design-system, domain, network, local-db, navigation)
5. Kommunikation: Features reden nicht direkt miteinander; Navigation + Shared-Domain-Modelle in core/domain.

Begründung

- KMP erlaubt maximale Codewiederverwendung über Web und JVM bei konsistentem Tooling.
- Koin bietet leichtgewichtige, idiomatische DI ohne Code-Generierung, geeignet für KMP.
- SQLDelight liefert typsichere Queries, portable Schemas und ist für Offline-First praxiserprobt.
- Die Trennung in shells/features/core fördert klare Zuständigkeiten, Testbarkeit und schrittweise Erweiterbarkeit.

Konsequenzen

- Projektstruktur und Gradle-Module folgen strikt der Aufteilung in `shells`, `features`, `core`.
- Der `apiClient` (core/network) wird via Koin als Named Binding injiziert; manuelles Setzen von Authorization-Headern ist untersagt.
- UI liest aus der lokalen Datenbank (SQLDelight); Synchronisation erfolgt über Hintergrundjobs.

Status / Nacharbeiten

- Diese ADR konsolidiert die KMP-Entscheidung und ersetzt frühere verstreute Notizen. Weitere Details (z. B. konkrete Module, Pfade) sind in `docs/ARCHITECTURE.md` dokumentiert.
