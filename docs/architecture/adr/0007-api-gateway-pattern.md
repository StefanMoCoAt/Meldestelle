# ADR-0007: API Gateway Pattern

## Status

Accepted

## Context

With our microservices architecture ([ADR-0003](0003-microservices-architecture.md)), we faced several challenges related to client-service communication:

1. Clients would need to know the locations and interfaces of multiple services
2. Different clients (web, desktop, mobile) would need to make multiple calls to different services
3. Authentication and authorization would need to be implemented consistently across all services
4. Cross-cutting concerns like rate limiting, logging, and monitoring would need to be implemented in each service
5. API versioning and backward compatibility would need to be managed across all services
6. Network security would be more complex with multiple services exposed directly

We needed a solution that would simplify client-service communication while addressing these challenges.

## Decision

We decided to implement the API Gateway pattern using Ktor as the framework. The API Gateway serves as the single entry point for all client requests and provides the following functionality:

1. **Request Routing**: Routes requests to the appropriate microservices
2. **Authentication and Authorization**: Integrates with Keycloak ([ADR-0006](0006-authentication-authorization-keycloak.md)) to authenticate users and validate tokens
3. **Rate Limiting**: Prevents abuse by limiting the number of requests from a single client
4. **Request/Response Transformation**: Transforms requests and responses as needed for different clients
5. **Logging and Monitoring**: Provides centralized logging and monitoring of all API requests
6. **Caching**: Caches responses to improve performance
7. **API Documentation**: Hosts OpenAPI documentation for all services
8. **Service Discovery**: Discovers service instances dynamically

Our implementation includes:
- A Ktor-based API Gateway deployed as a containerized service
- Integration with Keycloak for authentication and authorization
- Custom plugins for rate limiting, logging, and monitoring
- OpenAPI documentation generation
- Service discovery integration

## Consequences

### Positive

- **Simplified client development**: Clients only need to communicate with a single endpoint
- **Consistent security**: Authentication and authorization are handled consistently
- **Centralized cross-cutting concerns**: Rate limiting, logging, and monitoring are implemented once
- **Improved security**: Internal services are not exposed directly to clients
- **Flexibility**: The gateway can adapt requests and responses for different clients

### Negative

- **Single point of failure**: The gateway becomes a critical component that must be highly available
- **Performance overhead**: Requests go through an additional network hop
- **Complexity**: The gateway needs to handle a wide range of functionality
- **Development bottleneck**: Changes to the gateway may require coordination across teams

### Neutral

- **Deployment considerations**: The gateway needs to be deployed and scaled appropriately
- **Versioning strategy**: API versioning still needs to be managed, albeit in one place

## Alternatives Considered

### Direct Client-to-Service Communication

We considered allowing clients to communicate directly with services. This would have eliminated the network hop through the gateway but would have made client development more complex and would have required implementing cross-cutting concerns in each service.

### Backend for Frontend (BFF) Pattern

We considered implementing separate Backend for Frontend (BFF) services for each client type. This would have allowed for more client-specific optimizations but would have increased development and operational overhead.

### Service Mesh

We considered using a service mesh like Istio or Linkerd to handle service-to-service communication. This would have provided many of the same benefits for service-to-service communication but would not have addressed the client-to-service communication challenges as effectively.

## References

- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)
- [Ktor Documentation](https://ktor.io/docs/)
- [Gateway Routing Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/gateway-routing)
- [Backend for Frontend Pattern](https://samnewman.io/patterns/architectural/bff/)
