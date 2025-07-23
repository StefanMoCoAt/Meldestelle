# ADR-0004: Event-Driven Communication

## Status

Accepted

## Context

With the adoption of a microservices architecture ([ADR-0003](0003-microservices-architecture.md)), we needed to determine the most effective way for services to communicate with each other. Key considerations included:

1. Loose coupling between services to maintain their independence
2. Asynchronous processing capabilities to improve system resilience and scalability
3. Reliable communication to ensure that important information is not lost
4. Support for complex workflows that span multiple services
5. Ability to reconstruct the state of the system for auditing and debugging purposes

## Decision

We decided to implement an event-driven communication pattern using Apache Kafka as the message broker. The key aspects of this approach include:

1. **Domain Events**: Services publish domain events when significant state changes occur
2. **Event Sourcing**: For critical data, we store all events that led to the current state
3. **Message-Based Communication**: Services communicate primarily through asynchronous messages
4. **Choreography**: Complex workflows are implemented through event choreography rather than orchestration
5. **Event Schema Registry**: We maintain a registry of event schemas to ensure compatibility

The implementation includes:
- Kafka as the central message broker
- Schema registry for managing event schemas
- Event handlers in each service to process events from other services
- Event publishers in each service to publish domain events

## Consequences

### Positive

- **Loose coupling**: Services are decoupled, only sharing the event contracts
- **Scalability**: Asynchronous processing allows for better scalability under load
- **Resilience**: Services can continue to function even when other services are unavailable
- **Audit trail**: Event sourcing provides a complete audit trail of all state changes
- **Flexibility**: New consumers can be added without modifying publishers

### Negative

- **Eventual consistency**: The system is eventually consistent, which can be challenging to reason about
- **Complexity**: Event-driven systems are more complex to design, implement, and debug
- **Ordering guarantees**: Ensuring the correct ordering of events can be challenging
- **Idempotency**: Services must handle duplicate events correctly
- **Learning curve**: Developers need to learn event-driven patterns and practices

### Neutral

- **Monitoring needs**: Comprehensive monitoring is required to track event flow
- **Testing approach**: Testing strategies need to account for asynchronous behavior

## Alternatives Considered

### Synchronous REST APIs

We considered using synchronous REST APIs as the primary communication mechanism. This would have been simpler to implement and debug but would have led to tighter coupling between services and reduced resilience.

### Request-Response Messaging

We considered a request-response messaging pattern where services would send requests and wait for responses. This would have provided some of the benefits of asynchronous communication while maintaining a familiar request-response model, but would not have supported the publish-subscribe pattern as effectively.

### GraphQL Federation

We considered using GraphQL federation to compose APIs from multiple services. This would have provided a unified API for clients but would have maintained tight coupling between services and would not have supported asynchronous workflows as effectively.

## References

- [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/)
- [Event-Driven Architecture by Martin Fowler](https://martinfowler.com/articles/201701-event-driven.html)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Event Sourcing Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/event-sourcing)
