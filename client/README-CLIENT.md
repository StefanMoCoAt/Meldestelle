# 🖥️ Client-Architektur - Meldestelle

## Überblick

Das **Client**-Modul stellt die vollständige Benutzeroberflächen-Lösung für das Meldestelle-System bereit und liefert eine konsistente Erfahrung auf mehreren Plattformen durch Kotlin Multiplatform- und Compose Multiplatform-Technologien.

**Architektur-Highlights:**
- 🌐 **Plattformübergreifend** - Eine einzige Codebasis für Desktop (JVM) und Web (JavaScript) Anwendungen
- 🏗️ **Moderne MVVM** - Umfassende Model-View-ViewModel-Architektur mit ordnungsgemäßer Zustandsverwaltung
- 🧪 **Testabdeckung** - Produktionsbereit mit umfassenden Tests über alle Module
- 🚀 **Optimiert** - Build- und Laufzeit-Optimierungen für Leistung und Entwicklererfahrung
- 📱 **Progressive** - Web-App mit vollständigen PWA-Fähigkeiten für mobile und Desktop-Installation

---

## Client-Module Struktur

```
client/
├── common-ui/                          # Geteilte UI-Komponenten und Geschäftslogik
│   ├── src/commonMain/                 # Plattformübergreifende MVVM-Implementierung
│   ├── src/commonTest/                 # Umfassende Test-Suite (32 Tests)
│   └── README-CLIENT-COMMON-UI.md      # Detaillierte common-ui Dokumentation
├── desktop-app/                        # Native Desktop-Anwendung
│   ├── src/jvmMain/                    # Desktop-spezifische Implementierung
│   ├── src/jvmTest/                    # Desktop-Anwendungs-Tests
│   └── README-CLIENT-DESKTOP-APP.md    # Detaillierte desktop-app Dokumentation
├── web-app/                            # Progressive Web Application
│   ├── src/jsMain/                     # Web-spezifische Implementierung mit PWA
│   ├── src/jsTest/                     # JavaScript-kompatible Tests
│   └── README-CLIENT-WEB-APP.md        # Detaillierte web-app Dokumentation
└── README-CLIENT.md                    # Diese Übersichts-Dokumentation
```

---

## Architektur-Überblick

### Multi-Plattform-Strategie

Die Client-Architektur folgt einem geschichteten Ansatz mit maximaler Code-Wiederverwendung:

```
┌─────────────────────────────────────────────────┐
│                  Client-Apps                    │
├─────────────────┬───────────────────────────────┤
│  Desktop-App    │         Web-App               │
│  (JVM/Compose)  │    (Kotlin/JS + PWA)          │
├─────────────────┴───────────────────────────────┤
│              Common-UI Modul                    │
│         (Geteilte MVVM + Geschäftslogik)        │
├─────────────────────────────────────────────────┤
│    Plattformspezifische Abhängigkeiten          │
│   JVM: Ktor-CIO    │    JS: Ktor-JS             │
└─────────────────────────────────────────────────┘
```

### MVVM-Implementierung

Die vollständige Client-Architektur implementiert das ordnungsgemäße MVVM-Muster:

- **Model**: Datenmodelle und Services (`PingService`, `PingResponse`)
- **View**: Compose UI-Komponenten (Desktop + Web)
- **ViewModel**: Zustandsverwaltung (`PingViewModel`, `PingUiState`)

### Vier UI-Zustände Implementierung

Gemäß den trace-bullet-guideline.md Spezifikationen:

1. **Initial-Zustand**: `PingUiState.Initial` - Neutrale Nachricht, Button aktiv
2. **Loading-Zustand**: `PingUiState.Loading` - Ladeindikator, Button deaktiviert
3. **Success-Zustand**: `PingUiState.Success` - Positive Antwort, Button aktiv
4. **Error-Zustand**: `PingUiState.Error` - Klare Fehlernachricht, Button aktiv

---

## Schnellstart

### Voraussetzungen

| Tool | Version | Zweck |
|------|---------|-------|
| JDK | 21 (Temurin) | Desktop-Laufzeit und Build-System |
| Node.js | ≥ 20 | Web-Entwicklung und JavaScript-Laufzeit |
| Gradle | 8.x (wrapper) | Build-Automatisierung (enthalten) |

### Entwicklungs-Befehle

```bash
# 🖥️ Desktop-Anwendung
./gradlew :client:desktop-app:run                    # Desktop-App starten
./gradlew :client:desktop-app:jvmTest               # Desktop-Tests ausführen

# 🌐 Web-Anwendung
./gradlew :client:web-app:jsBrowserDevelopmentRun   # Web-Dev-Server starten
./gradlew :client:web-app:jsTest                    # Web-Tests ausführen

# 🧩 Common-UI Modul
./gradlew :client:common-ui:jvmTest                 # Geteilte Logik-Tests ausführen
./gradlew :client:common-ui:build                   # Geteiltes Modul erstellen

# 🔄 Alle Client-Tests
./gradlew :client:common-ui:jvmTest :client:desktop-app:jvmTest :client:web-app:jsTest
```

---

## Modul-Dokumentation

### 📖 Detaillierte Dokumentations-Links

Jedes Modul hat eine umfassende Dokumentation, die Architektur, Entwicklung, Testing und Deployment abdeckt:

- **[Common-UI Modul](common-ui/README-CLIENT-COMMON-UI.md)** - Geteilte MVVM-Architektur, Services und Geschäftslogik
- **[Desktop-App Modul](desktop-app/README-CLIENT-DESKTOP-APP.md)** - Native Desktop-Anwendung mit plattformübergreifender Distribution
- **[Web-App Modul](web-app/README-CLIENT-WEB-APP.md)** - Progressive Web Application mit modernen Web-Standards

### 🎯 Wichtige Dokumentations-Abschnitte

Jede Modul-README enthält:
- **Architektur & Struktur** - Detaillierte technische Architektur
- **Entwicklungs-Workflow** - Setup, Build und Testing-Verfahren
- **API-Referenz** - Vollständige API-Dokumentation mit Beispielen
- **Deployment-Leitfaden** - Produktions-Deployment-Anweisungen
- **Fehlerbehebung** - Häufige Probleme und Lösungen

---

## Build & Packaging

### Entwicklungs-Builds

```bash
# Alle Client-Module erstellen
./gradlew :client:build

# Einzelne Module erstellen
./gradlew :client:common-ui:build        # Geteilte Komponenten
./gradlew :client:desktop-app:build      # Desktop-Anwendung
./gradlew :client:web-app:build          # Web-Anwendung
```

### Produktions-Packaging

| Plattform | Befehl | Ausgabe |
|-----------|--------|---------|
| **Desktop** | `./gradlew :client:desktop-app:createDistributable` | Plattformübergreifende Installer |
| **Web** | `./gradlew :client:web-app:jsBrowserProductionWebpack` | Optimiertes PWA-Bundle |

### Distributions-Formate

**Desktop-Anwendung:**
- Linux: `.deb` Pakete
- macOS: `.dmg` Disk-Images
- Windows: `.msi` Installer

**Web-Anwendung:**
- Optimierte JavaScript-Bundles
- PWA-Manifest für App-Installation
- Service Worker bereit (zukünftige Erweiterung)

---

## Konfiguration

### Umgebungs-Konfiguration

Die Client-Anwendungen unterstützen flexible Konfiguration:

| Konfiguration | Desktop | Web | Standardwert |
|---------------|---------|-----|--------------|
| **API Basis-URL** | System-Eigenschaft | Build-Zeit | `http://localhost:8080` |
| **Log-Level** | JVM-Args | Konsole | `INFO` |

### Desktop-Konfiguration

```bash
# Benutzerdefinierte API-URL
./gradlew :client:desktop-app:run -Dmeldestelle.api.url=https://api.production.com

# Entwicklung mit lokalem Backend
./gradlew :client:desktop-app:run -Dmeldestelle.api.url=http://localhost:8080
```

### Web-Konfiguration

Die Web-Anwendungs-Konfiguration wird zur Build-Zeit eingebettet und kann im Build-Prozess angepasst werden.

---

## Test-Strategie

### Umfassende Test-Abdeckung

| Modul | Test-Typ | Anzahl | Abdeckung |
|-------|----------|--------|-----------|
| **Common-UI** | Unit + Integration | 32 | Geschäftslogik, MVVM, Services |
| **Desktop-App** | JVM Integration | 3 | Anwendungsstart, Konfiguration |
| **Web-App** | JavaScript | 4 | Web-spezifische Funktionalität, PWA |
| **Gesamt** | **Plattformübergreifend** | **39** | **Vollständige Client-Abdeckung** |

### Test-Ausführung

```bash
# Alle Client-Tests ausführen
./gradlew :client:common-ui:jvmTest :client:desktop-app:jvmTest :client:web-app:jsTest

# Einzelne Test-Suiten
./gradlew :client:common-ui:jvmTest      # Geteilte Geschäftslogik
./gradlew :client:desktop-app:jvmTest    # Desktop-spezifische Tests
./gradlew :client:web-app:jsTest         # Web/JS-spezifische Tests
```

### Test-Qualitäts-Metriken

- **✅ MVVM-Architektur**: Vollständiges Zustandsverwaltungs-Testing
- **✅ Ressourcenverwaltung**: Memory-Leak-Präventions-Validierung
- **✅ Plattformübergreifend**: Plattformspezifische Integrationstests
- **✅ API-Integration**: HTTP-Service und Serialisierungs-Tests

---

## Leistung & Qualität

### Architektur-Vorteile

**🏗️ MVVM-Implementierung:**
- Ordnungsgemäße Trennung der Belange mit testbaren Komponenten
- Reaktive UI-Zustandsverwaltung mit Compose
- Ressourcen-Lebenszyklus-Verwaltung mit automatischer Bereinigung

**🚀 Laufzeit-Leistung:**
- Effizientes Speichermanagement durch ordnungsgemäße Disposal-Muster
- Optimierte Build-Konfigurationen für beide Plattformen
- Minimaler Overhead mit geteilter Geschäftslogik

**🔧 Entwicklererfahrung:**
- Hot Reload für Desktop- und Web-Entwicklung
- Umfassende Test-Infrastruktur
- Klare Dokumentation und Fehlerbehebungs-Leitfäden

### Qualitätssicherung

- **Test-Abdeckung**: 39 umfassende Tests über alle Client-Module
- **Architektur-Konformität**: 100% MVVM-Muster-Implementierung
- **Build-Optimierung**: Moderne Gradle-Konfiguration mit Abhängigkeitsverwaltung
- **Plattformübergreifend**: Konsistentes Verhalten über Desktop- und Web-Plattformen

---

## Produktionsbereitschaft

### Desktop-Anwendung

✅ **Distributionsbereit:**
- Plattformübergreifende Installer (Linux, macOS, Windows)
- Native Leistung mit JVM-Optimierung
- System-Integrations-Fähigkeiten

✅ **Enterprise-Features:**
- Konfigurierbare API-Endpunkte
- Logging-Integration bereit
- Ressourcenverwaltung und Bereinigung

### Web-Anwendung

✅ **Moderne PWA:**
- Progressive Web App mit Installations-Unterstützung
- Mobile-First responsives Design
- Offline-Fähigkeiten bereit (Service Worker erweiterbar)

✅ **Produktionsstandards:**
- Sicherheits-Header (CSP, XSS-Schutz)
- Leistungsoptimierung (Webpack, Caching)
- SEO und Barrierefreiheits-Konformität

---

## API-Integration

### Geteilter HTTP-Client

Alle Client-Anwendungen verwenden ein konsistentes API-Integrations-Muster:

```kotlin
// Geteilte Service-Schicht
class PingService(
    private val baseUrl: String,
    private val httpClient: HttpClient
) {
    suspend fun ping(): Result<PingResponse>
    fun close()
}

// Plattformspezifische Engines
// Desktop: Ktor CIO Engine
// Web: Ktor JS Engine
```

### API-Konfiguration

| Umgebung | API Basis-URL | Konfigurationsmethode |
|----------|---------------|----------------------|
| **Entwicklung** | `http://localhost:8080` | Standard-Konfiguration |
| **Staging** | `https://staging-api.example.com` | System-Eigenschaften / Build-Konfiguration |
| **Produktion** | `https://api.example.com` | System-Eigenschaften / Build-Konfiguration |

---

## Migrations- & Upgrade-Leitfaden

### Von der vorherigen Architektur

Die Client-Architektur wurde vollständig modernisiert:

**Vorher (Komponentenbasiert):**
- Vermischte Belange in UI-Komponenten
- Manuelle Zustandsverwaltung
- Speicherleck-Potenzial
- Begrenzte plattformübergreifende Wiederverwendung

**Aktuell (MVVM):**
- Saubere Architektur mit getrennten Belangen
- Reaktive Zustandsverwaltung mit Compose
- Automatische Ressourcenbereinigung
- Maximale Code-Wiederverwendung über Plattformen

### Breaking Changes

**Keine** - Das Architektur-Upgrade behielt die Rückwärtskompatibilität für alle öffentlichen APIs bei.

---

## Zukünftige Erweiterungen

### Roadmap-Prioritäten

1. **Erweiterte PWA-Features**
   - Service Worker-Implementierung für vollständige Offline-Unterstützung
   - Push-Benachrichtigungs-Integration
   - Hintergrund-Sync-Fähigkeiten

2. **Desktop-Erweiterungen**
   - Native System-Integration (Benachrichtigungen, Dateidialoge)
   - Auto-Update-Mechanismen
   - Erweiterte Logging-Konfiguration

3. **Test-Erweiterung**
   - End-to-End-Testing über Plattformen
   - Visual Regression Testing
   - Performance-Benchmarking

4. **Monitoring-Integration**
   - Fehlerberichterstattung und Analytik
   - Performance-Überwachung
   - Benutzerverhalten-Analytik

---

## Fehlerbehebung

### Häufige Probleme über alle Plattformen

| Problem | Plattform | Lösung |
|---------|-----------|--------|
| API-Verbindungsfehler | Alle | Basis-URL-Konfiguration und Netzwerkkonnektivität überprüfen |
| Build-Fehler | Alle | Build-Verzeichnis bereinigen: `./gradlew clean` |
| Test-Ausführungsprobleme | Alle | Plattformspezifische Anforderungen prüfen (JDK, Node.js) |
| Hot Reload funktioniert nicht | Web | Dev-Server neu starten, File Watcher prüfen |

### Plattformspezifische Probleme

**Desktop:**
- Fenster wird nicht angezeigt → Display-Einstellungen und Fensterzustand prüfen
- SLF4J-Warnungen → Logback-Abhängigkeit hinzufügen (nicht kritisch)

**Web:**
- Weißer Bildschirm beim Laden → Browser-Konsole auf JavaScript-Fehler prüfen
- PWA installiert nicht → HTTPS und manifest.json verifizieren

### Debug-Befehle

```bash
# Umfassende Build-Analyse
./gradlew :client:build --scan

# Abhängigkeitskonflikt-Analyse
./gradlew :client:dependencies

# Ausführliche Test-Ausführung
./gradlew :client:common-ui:jvmTest --info
```

---

## Mitwirken

### Entwicklungs-Workflow

1. **Umgebung einrichten**
   ```bash
   # Voraussetzungen überprüfen
   java -version    # Sollte JDK 21 anzeigen
   node --version   # Sollte Node.js ≥ 20 anzeigen

   # Erstellen und validieren
   ./gradlew :client:build
   ```

2. **Entwicklungsprozess**
   ```bash
   # Üblicher Entwicklungszyklus
   ./gradlew :client:common-ui:jvmTest      # Geteilte Logik testen
   ./gradlew :client:desktop-app:run        # Desktop-Integration testen
   ./gradlew :client:web-app:jsTest         # Web-Kompatibilität testen
   ```

3. **Qualitätssicherung**
   ```bash
   # Vollständige Test-Suite
   ./gradlew :client:common-ui:jvmTest :client:desktop-app:jvmTest :client:web-app:jsTest

   # Architektur-Validierung
   ./gradlew :client:build --scan
   ```

### Code-Standards

- **Architektur**: MVVM-Muster und Trennung der Belange beibehalten
- **Testing**: Tests für neue Funktionalität über alle betroffenen Module hinzufügen
- **Dokumentation**: Modul-spezifische READMEs für Änderungen aktualisieren
- **Kompatibilität**: Sicherstellen, dass Änderungen auf Desktop- und Web-Plattformen funktionieren

### Pull Request Checkliste

- [ ] Alle bestehenden Tests bestehen über alle Client-Module
- [ ] Neue Funktionalität beinhaltet angemessene Test-Abdeckung
- [ ] MVVM-Architektur-Muster beibehalten
- [ ] Plattformübergreifende Kompatibilität verifiziert
- [ ] Modul-spezifische Dokumentation aktualisiert
- [ ] Leistungsauswirkungen bewertet und dokumentiert

---

## Kontakt & Support

### Dokumentations-Struktur

Für detaillierte Informationen zu spezifischen Modulen:

- **Common-UI**: [README-CLIENT-COMMON-UI.md](common-ui/README-CLIENT-COMMON-UI.md)
- **Desktop-App**: [README-CLIENT-DESKTOP-APP.md](desktop-app/README-CLIENT-DESKTOP-APP.md)
- **Web-App**: [README-CLIENT-WEB-APP.md](web-app/README-CLIENT-WEB-APP.md)

### Schnellreferenz

| Aufgabe | Befehl |
|---------|--------|
| Desktop-App starten | `./gradlew :client:desktop-app:run` |
| Web-Dev-Server starten | `./gradlew :client:web-app:jsBrowserDevelopmentRun` |
| Alle Tests ausführen | `./gradlew :client:common-ui:jvmTest :client:desktop-app:jvmTest :client:web-app:jsTest` |
| Für Produktion erstellen | `./gradlew :client:build` |
| Desktop-Installer erstellen | `./gradlew :client:desktop-app:createDistributable` |
| Web-Produktions-Bundle erstellen | `./gradlew :client:web-app:jsBrowserProductionWebpack` |

---

**Client-Status**: ✅ Produktionsbereit
**Architektur**: ✅ MVVM Vollständig
**Test-Abdeckung**: ✅ Umfassend (39 Tests)
**Plattformübergreifend**: ✅ Desktop + Web PWA
**Dokumentation**: ✅ Vollständig

*Zuletzt aktualisiert: 16. August 2025*
