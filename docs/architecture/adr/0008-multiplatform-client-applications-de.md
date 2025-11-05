# ADR-0008: Multiplatform-Client-Anwendungen

## Status

Akzeptiert

## Kontext

Unser System benötigt Client-Anwendungen für verschiedene Benutzerrollen und Plattformen:

1. Desktop-Anwendungen für Administratoren und Veranstaltungsorganisatoren, die umfangreiche Funktionalität benötigen
2. Web-Anwendungen für Mitglieder und Pferdebesitzer, die von verschiedenen Geräten aus auf das System zugreifen müssen
3. Potenzielle zukünftige mobile Anwendungen für den Zugriff unterwegs

Die Entwicklung und Wartung separater Codebasen für jede Plattform würde erfordern:

- Doppelte Implementierung von Geschäftslogik und UI-Komponenten
- Mehrere Teams mit unterschiedlicher Plattformexpertise
- Koordination, um eine konsistente Benutzererfahrung über Plattformen hinweg zu gewährleisten
- Höhere Wartungskosten, da Funktionen und Fehlerbehebungen mehrfach implementiert werden müssten

Wir benötigten eine Lösung, die es uns ermöglicht, Code über Plattformen hinweg zu teilen und gleichzeitig auf jeder
Plattform eine native Benutzererfahrung zu bieten.

## Entscheidung

Wir haben uns entschieden, Kotlin Multiplatform und Compose Multiplatform für unsere Client-Anwendungen zu verwenden:

1. **Kotlin Multiplatform**: Ermöglicht die gemeinsame Nutzung von Geschäftslogik, Datenmodellen und API-Client-Code
   über Plattformen hinweg
2. **Compose Multiplatform**: Bietet ein deklaratives UI-Framework, das auf Desktop-, Web- und mobilen Plattformen
   funktioniert

Unsere Implementierung umfasst:

- **common-ui**: Gemeinsame UI-Komponenten und Geschäftslogik
- **desktop-app**: Desktop-Anwendung für Administratoren und Veranstaltungsorganisatoren
- **web-app**: Web-Anwendung für Mitglieder und Pferdebesitzer

Die Architektur folgt einem Model-View-ViewModel (MVVM)-Muster:

- **Model**: Gemeinsame Datenmodelle und Repository-Implementierungen
- **ViewModel**: Gemeinsame Geschäftslogik und Zustandsverwaltung
- **View**: Plattformspezifische UI-Implementierungen mit Compose Multiplatform

Wir verwenden einen modularen Ansatz, bei dem plattformspezifischer Code minimiert wird und der größte Teil des Codes
über Plattformen hinweg geteilt wird.

## Konsequenzen

### Positive

- **Codesharing**: Wesentliche Teile des Codes werden über Plattformen hinweg geteilt, was Duplizierung reduziert
- **Konsistente Benutzererfahrung**: UI-Komponenten und Verhalten sind über Plattformen hinweg konsistent
- **Einheitliche Sprache**: Kotlin wird für alle Plattformen verwendet, was die Entwicklung vereinfacht
- **Reduzierter Wartungsaufwand**: Fehlerbehebungen und Funktionen können einmal implementiert und über Plattformen
  hinweg angewendet werden
- **Team-Effizienz**: Entwickler können mit demselben Skillset an mehreren Plattformen arbeiten

### Negative

- **Lernkurve**: Kotlin Multiplatform und Compose Multiplatform haben eine Lernkurve
- **Reife**: Compose Multiplatform entwickelt sich noch weiter, besonders für Web-Targets
- **Leistungsüberlegungen**: Es kann im Vergleich zu Plattform nativen Lösungen zu Leistungs-Overhead kommen
- **Plattformspezifische Funktionen**: Einige plattformspezifische Funktionen können schwieriger zu implementieren sein
- **Debugging-Komplexität**: Das Debugging über Plattformen hinweg kann komplexer sein

### Neutral

- **Komplexität des Build-Systems**: Das Build-System ist mit Multiplatform-Targets komplexer
- **Abhängigkeitsverwaltung**: Die Verwaltung von Abhängigkeiten über Plattformen hinweg erfordert sorgfältige
  Überlegungen

## Betrachtete Alternativen

### Separate native Anwendungen

Wir haben die Entwicklung separater nativer Anwendungen für jede Plattform in Betracht gezogen (Java/JavaFX für Desktop,
JavaScript/React für Web). Dies hätte die beste Leistung und Zugriff auf Plattformfunktionen geboten, hätte aber eine
doppelte Implementierung von Geschäftslogik und UI-Komponenten erfordert.

### React Native

Wir haben die Verwendung von React Native für Mobile und Web mit einer separaten Desktop-Anwendung in Betracht gezogen.
Dies hätte Codesharing zwischen Mobile und Web ermöglicht, hätte aber immer noch eine separate Desktop-Lösung erfordert
und hätte JavaScript-Expertise erfordert.

### Flutter

Wir haben die Verwendung von Flutter für alle Plattformen in Betracht gezogen. Flutter bietet gute
plattformübergreifende Unterstützung, hätte aber das Erlernen von Dart erfordert und hätte weniger Integration mit
unseren Kotlin-basierten Backend-Diensten gehabt.

## Referenzen

- [Kotlin Multiplatform Dokumentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform Dokumentation](https://www.jetbrains.com/lp/compose-multiplatform/)
- [MVVM-Architekturmuster](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/lp/mobile/)
