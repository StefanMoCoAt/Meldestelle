# Infrastructure/Event-Store Module

## Überblick

Das **Event-Store-Modul** ist eine kritische Komponente der Infrastruktur, die für die Persistenz und Veröffentlichung von Domänen-Events zuständig ist. Es bildet die technische Grundlage für **Event Sourcing** und eine allgemeine **ereignisgesteuerte Architektur**. Anstatt nur den aktuellen Zustand einer Entität zu speichern, speichert der Event Store die gesamte Kette von Ereignissen, die zu diesem Zustand geführt haben.

## Architektur: Port-Adapter-Muster

Das Modul folgt streng dem **Port-Adapter-Muster**, um eine maximale Entkopplung von der konkreten Speichertechnologie zu erreichen.

* **`:infrastructure:event-store:event-store-api`**: Definiert den abstrakten "Vertrag" (`EventStore`-Interface), gegen den die Fach-Services programmieren.
* **`:infrastructure:event-store:redis-event-store`**: Die konkrete Implementierung des Vertrags, die **Redis Streams** als hoch-performantes, persistentes Log verwendet.

## Schlüsselfunktionen

* **Garantierte Konsistenz:** Schreibvorgänge in den aggregat spezifischen Stream und den globalen "all-events"-Stream werden innerhalb einer **atomaren Redis-Transaktion (`MULTI`/`EXEC`)** ausgeführt. Dies stellt sicher, dass der Event-Store niemals in einen inkonsistenten Zustand gerät.
* **Resiliente Event-Verarbeitung:** Der `RedisEventConsumer` nutzt **Redis Consumer Groups**, um eine skalierbare und ausfallsichere Verarbeitung von Events zu ermöglichen. Er enthält eine robuste Logik zum "Claimen" von Nachrichten, die von ausgefallenen Consumern nicht bestätigt wurden, sodass keine Events verloren gehen.
* **Optimistische Nebenhäufigkeitskontrolle:** Verhindert Race Conditions, indem beim Speichern von Events eine `expectedVersion` überprüft wird. Bei Konflikten wird eine `ConcurrencyException` geworfen.
* **Intelligente Serialisierung:** Der `JacksonEventSerializer` speichert Event-Metadaten und die eigentliche Nutzlast getrennt in der Redis-Stream-Nachricht, was eine effiziente Analyse von Streams ermöglicht.

## Verwendung

Ein Anwendung-Service bindet `:infrastructure:event-store:redis-event-store` ein und lässt sich das `EventStore`-Interface per Dependency Injection geben.

```kotlin
@Service
class MemberApplicationService(
    private val eventStore: EventStore // Nur das Interface wird verwendet!
) {
    fun registerNewMember(command: RegisterMemberCommand) {
        // 1. Geschäftslogik ausführen und Event erzeugen
        val memberRegisteredEvent = MemberRegisteredEvent(
            aggregateId = uuid4(),
            version = 1L,
            name = command.name
        )

        // 2. Event im Event Store speichern (mit Concurrency Check)
        // hier wird erwartet, dass der Stream neu ist (Version 0)
        eventStore.appendToStream(memberRegisteredEvent, memberRegisteredEvent.aggregateId, 0)
    }
}
```

## Testing-Strategie
Die Qualität des Moduls wird durch eine robuste Teststrategie sichergestellt:

* *Integrationstests mit Testcontainer: Die Kernfunktionalität wird gegen eine echte Redis-Datenbank getestet, die zur Laufzeit in einem Docker-Container gestartet wird.*

* *Zuverlässige Consumer-Tests: Die asynchrone Logik des Event-Consumers wird in den Tests synchron und deterministisch überprüft, indem der pollEvents()-Zyklus manuell angestoßen wird. Dies vermeidet unzuverlässige Tests, die auf Thread.sleep basieren.*

* *Saubere Test-Daten: Test-Event-Klassen werden durch die Verwendung der @Transient-Annotation sauber und frei von Boilerplate-Code gehalten.*

**Letzte Aktualisierung**: 9. August 2025
