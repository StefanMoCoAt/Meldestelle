# Infrastructure/Messaging Module

## Überblick

Das **Messaging-Modul** stellt die Infrastruktur für die asynchrone Kommunikation zwischen den Microservices des Meldestelle-Systems bereit. Es nutzt **Apache Kafka** als hochperformanten, verteilten Message-Broker. Dieses Modul ist entscheidend für die Entkopplung von Services und die Implementierung von Mustern wie Publish/Subscribe, um eine skalierbare und resiliente Architektur zu ermöglichen.

## Architektur

Ähnlich wie andere Infrastruktur-Module ist auch dieses in zwei spezialisierte Komponenten aufgeteilt, um Konfiguration von der Client-Logik zu trennen:


infrastructure/messaging/
├── messaging-config/   # Stellt die zentrale Kafka-Konfiguration bereit
└── messaging-client/   # Stellt wiederverwendbare Producer- und Consumer-Clients bereit


### `messaging-config`

Dieses Modul ist die Basis für jede Kafka-Interaktion. Es ist dafür verantwortlich, die gesamte **Konfiguration** zu zentralisieren.

* **Zweck:** Definiert Spring-Beans für die grundlegende Kafka-Konfiguration. Dazu gehören:
    * Die Adresse der Kafka-Broker (`bootstrap-servers`).
    * Konfiguration für Serializer und Deserializer (z.B. `JsonSerializer` von Spring Kafka), um sicherzustellen, dass alle Services Nachrichten im selben Format (JSON) austauschen.
    * Konfiguration für Topics, Partitionen und Replikationsfaktoren.
* **Vorteil:** Jeder Service, der Kafka nutzt, kann sich auf diese zentrale Konfiguration verlassen, was die Konsistenz sicherstellt und die Einrichtung neuer Producer oder Consumer vereinfacht.

### `messaging-client`

Dieses Modul baut auf `messaging-config` auf und stellt **wiederverwendbare High-Level-Komponenten** für die Interaktion mit Kafka bereit.

* **Zweck:** Stellt einfach zu verwendende Klassen oder Services zur Verfügung, z.B. einen `KafkaProducerService` zum Senden von Nachrichten und einen `KafkaConsumerService` zum Empfangen von Nachrichten. Es nutzt **Project Reactor** (`reactor-kafka`), um eine reaktive und nicht-blockierende Verarbeitung von Nachrichten zu ermöglichen.
* **Vorteil:** Kapselt die Komplexität der Kafka-Producer- und -Consumer-API. Ein Fach-Service muss nur noch eine Methode wie `producer.sendMessage("topic", message)` aufrufen, ohne sich um die Details der Verbindung, Serialisierung oder Fehlerbehandlung kümmern zu müssen.

## Verwendung in anderen Modulen

Ein Microservice, der Nachrichten senden oder empfangen möchte, geht wie folgt vor:

1.  **Abhängigkeit deklarieren:** Das Service-Modul (z.B. `events-service`) fügt eine `implementation`-Abhängigkeit zu `:infrastructure:messaging:messaging-client` in seiner `build.gradle.kts` hinzu.

2.  **Client-Service injizieren:** Im Service-Code wird der `KafkaProducerService` oder `KafkaConsumerService` per Dependency Injection angefordert.

    ```kotlin
    // Beispiel für das Senden einer Nachricht
    @Service
    class EventNotificationService(
        private val kafkaProducer: KafkaProducerService
    ) {
        fun notifyNewEvent(eventDetails: EventDetails) {
            val topic = "new-events-topic"
            // Einfacher Aufruf zum Senden der Nachricht.
            // Die Komplexität der Serialisierung und des Sendens ist gekapselt.
            kafkaProducer.sendMessage(topic, eventDetails.id, eventDetails)
                .subscribe(
                    { result -> logger.info("Message sent successfully to topic '{}'", topic) },
                    { error -> logger.error("Failed to send message to topic '{}'", topic, error) }
                )
        }
    }
    ```kotlin
    // Beispiel für das Empfangen von Nachrichten
    @Component
    class EventListener(
        private val kafkaConsumer: KafkaConsumerService
    ) {
        @PostConstruct
        fun listenForEvents() {
            val topic = "new-events-topic"
            // Reaktiv auf eingehende Nachrichten lauschen.
            kafkaConsumer.receiveMessages<EventDetails>(topic)
                .subscribe { event ->
                    logger.info("Received new event with ID: {}", event.id)
                    // Geschäftslogik zur Verarbeitung des Events...
                }
        }
    }
    ```

Diese Architektur ermöglicht eine saubere, robuste und hochgradig entkoppelte Kommunikation zwischen den Diensten.

---
**Letzte Aktualisierung**: 31. Juli 2025
