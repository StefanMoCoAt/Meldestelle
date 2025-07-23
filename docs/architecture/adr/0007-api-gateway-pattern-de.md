# ADR-0007: API-Gateway-Muster

## Status

Akzeptiert

## Kontext

Mit unserer Microservices-Architektur ([ADR-0003](0003-microservices-architecture-de.md)) standen wir vor mehreren Herausforderungen im Zusammenhang mit der Client-Service-Kommunikation:

1. Clients müssten die Standorte und Schnittstellen mehrerer Dienste kennen
2. Verschiedene Clients (Web, Desktop, Mobil) müssten mehrere Aufrufe an verschiedene Dienste tätigen
3. Authentifizierung und Autorisierung müssten konsistent über alle Dienste hinweg implementiert werden
4. Querschnittsbelange wie Rate-Limiting, Logging und Monitoring müssten in jedem Dienst implementiert werden
5. API-Versionierung und Abwärtskompatibilität müssten über alle Dienste hinweg verwaltet werden
6. Die Netzwerksicherheit wäre komplexer, wenn mehrere Dienste direkt exponiert würden

Wir benötigten eine Lösung, die die Client-Service-Kommunikation vereinfachen und gleichzeitig diese Herausforderungen adressieren würde.

## Entscheidung

Wir haben uns entschieden, das API-Gateway-Muster mit Ktor als Framework zu implementieren. Das API-Gateway dient als einziger Eingangspunkt für alle Client-Anfragen und bietet die folgenden Funktionen:

1. **Anfrage-Routing**: Leitet Anfragen an die entsprechenden Microservices weiter
2. **Authentifizierung und Autorisierung**: Integriert sich mit Keycloak ([ADR-0006](0006-authentication-authorization-keycloak-de.md)), um Benutzer zu authentifizieren und Tokens zu validieren
3. **Rate-Limiting**: Verhindert Missbrauch durch Begrenzung der Anzahl von Anfragen von einem einzelnen Client
4. **Anfrage/Antwort-Transformation**: Transformiert Anfragen und Antworten nach Bedarf für verschiedene Clients
5. **Logging und Monitoring**: Bietet zentralisiertes Logging und Monitoring aller API-Anfragen
6. **Caching**: Speichert Antworten im Cache, um die Leistung zu verbessern
7. **API-Dokumentation**: Hostet OpenAPI-Dokumentation für alle Dienste
8. **Service-Discovery**: Entdeckt Dienstinstanzen dynamisch

Unsere Implementierung umfasst:
- Ein Ktor-basiertes API-Gateway, das als containerisierter Dienst bereitgestellt wird
- Integration mit Keycloak für Authentifizierung und Autorisierung
- Benutzerdefinierte Plugins für Rate-Limiting, Logging und Monitoring
- OpenAPI-Dokumentationsgenerierung
- Service-Discovery-Integration

## Konsequenzen

### Positive

- **Vereinfachte Client-Entwicklung**: Clients müssen nur mit einem einzigen Endpunkt kommunizieren
- **Konsistente Sicherheit**: Authentifizierung und Autorisierung werden konsistent gehandhabt
- **Zentralisierte Querschnittsbelange**: Rate-Limiting, Logging und Monitoring werden einmal implementiert
- **Verbesserte Sicherheit**: Interne Dienste werden nicht direkt Clients ausgesetzt
- **Flexibilität**: Das Gateway kann Anfragen und Antworten für verschiedene Clients anpassen

### Negative

- **Single Point of Failure**: Das Gateway wird zu einer kritischen Komponente, die hochverfügbar sein muss
- **Leistungs-Overhead**: Anfragen durchlaufen einen zusätzlichen Netzwerk-Hop
- **Komplexität**: Das Gateway muss eine breite Palette von Funktionalitäten handhaben
- **Entwicklungs-Engpass**: Änderungen am Gateway können Koordination über Teams hinweg erfordern

### Neutral

- **Deployment-Überlegungen**: Das Gateway muss angemessen bereitgestellt und skaliert werden
- **Versionierungsstrategie**: API-Versionierung muss immer noch verwaltet werden, wenn auch an einem Ort

## Betrachtete Alternativen

### Direkte Client-zu-Service-Kommunikation

Wir haben in Betracht gezogen, Clients die direkte Kommunikation mit Diensten zu ermöglichen. Dies hätte den Netzwerk-Hop durch das Gateway eliminiert, hätte aber die Client-Entwicklung komplexer gemacht und hätte die Implementierung von Querschnittsbelangen in jedem Dienst erfordert.

### Backend for Frontend (BFF)-Muster

Wir haben die Implementierung separater Backend for Frontend (BFF)-Dienste für jeden Client-Typ in Betracht gezogen. Dies hätte mehr clientspezifische Optimierungen ermöglicht, hätte aber den Entwicklungs- und Betriebsaufwand erhöht.

### Service Mesh

Wir haben die Verwendung eines Service Mesh wie Istio oder Linkerd zur Handhabung der Service-zu-Service-Kommunikation in Betracht gezogen. Dies hätte viele der gleichen Vorteile für die Service-zu-Service-Kommunikation geboten, hätte aber die Herausforderungen der Client-zu-Service-Kommunikation nicht so effektiv adressiert.

## Referenzen

- [API-Gateway-Muster](https://microservices.io/patterns/apigateway.html)
- [Ktor-Dokumentation](https://ktor.io/docs/)
- [Gateway-Routing-Muster](https://docs.microsoft.com/de-de/azure/architecture/patterns/gateway-routing)
- [Backend for Frontend-Muster](https://samnewman.io/patterns/architectural/bff/)
