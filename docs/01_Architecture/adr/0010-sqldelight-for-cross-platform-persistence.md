# ADR-0010: SQLDelight für Cross-Platform-Persistenz

## Status

Akzeptiert

## Kontext

Das "Meldestelle Portal" wird als Kotlin Multiplatform (KMP) Anwendung für Desktop (JVM) und Web (JS/Wasm) entwickelt. Eine zentrale Anforderung ist die **Offline-Fähigkeit**, was eine robuste, plattformübergreifende Persistenzlösung erfordert.

Die ursprünglich evaluierte Lösung, **Room**, unterstützt die Web-Targets (JS/Wasm) nicht, was die Implementierung der Offline-Fähigkeit im Browser blockiert. Wir benötigen eine Datenbank-Bibliothek, die auf allen Zielplattformen funktioniert und eine moderne, asynchrone API bietet.

## Entscheidung

Wir werden **SQLDelight** als primäre Persistenz-Bibliothek für das KMP-Frontend einsetzen.

*   **SQLDelight** generiert typsichere Kotlin-APIs aus SQL-Anweisungen und unterstützt alle KMP-Ziele, einschließlich JVM, JS und Wasm.
*   **Web (JS/Wasm):** Wir nutzen den `WebWorkerDriver` in Kombination mit dem **Origin Private File System (OPFS)**. Dies ermöglicht eine performante, persistente Speicherung, die im Browser-Kontext in einem Hintergrund-Thread läuft, um die UI nicht zu blockieren.
*   **Desktop (JVM):** Wir verwenden den `JdbcSqliteDriver`, um eine SQLite-Datenbank im Dateisystem des Benutzers zu speichern.
*   **Async-First:** Die Datenbank-Schnittstellen werden durch die Option `generateAsync = true` standardmäßig als `suspend`-Funktionen generiert, um den asynchronen Anforderungen der Web-Plattform gerecht zu werden.

## Konsequenzen

*   **Positive:**
    *   **Echte Cross-Platform-Persistenz:** Wir können dieselbe Datenbanklogik und dasselbe Schema auf allen Zielplattformen wiederverwenden.
    *   **Offline-Fähigkeit im Web:** Die Nutzung von OPFS ermöglicht eine robuste und performante Offline-Speicherung im Browser.
    *   **Typsicherheit:** SQLDelight bietet eine hohe Typsicherheit bei der Interaktion mit der Datenbank.
    *   **Asynchrone API:** Die standardmäßig asynchrone API passt gut zu Kotlin Coroutines und den Anforderungen moderner UIs.

*   **Negative:**
    *   **Erhöhte Komplexität im Web-Setup:** Die Konfiguration von OPFS erfordert spezifische HTTP-Header (`Cross-Origin-Opener-Policy`, `Cross-Origin-Embedder-Policy`), die im Webserver bzw. im Webpack Dev Server gesetzt werden müssen.
    *   **Lernkurve:** Das Team muss sich mit SQLDelight und den Besonderheiten der plattformspezifischen Treiber vertraut machen.

## Betrachtete Alternativen

*   **Room:** War die ursprüngliche Wahl, wurde aber aufgrund der fehlenden Unterstützung für JS/Wasm verworfen.
*   **Realm:** Eine weitere plattformübergreifende Datenbank, wurde aber aufgrund der komplexeren Lizenzierung und der engeren Bindung an das Realm-Ökosystem nicht weiter verfolgt.
*   **IndexedDB (manuell):** Die manuelle Verwendung von IndexedDB im Browser wäre eine Option gewesen, hätte aber zu einer inkonsistenten API zwischen den Plattformen und zu einem erheblichen Mehraufwand bei der Implementierung geführt.

## Referenzen

*   [Frontend Status Report 01-2026](../../../90_Reports/Frontend_Status_Report_01-2026.md)
*   [SQLDelight Dokumentation](https://cashapp.github.io/sqldelight/)
