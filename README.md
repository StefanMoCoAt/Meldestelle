# Meldestelle - Self-Contained Systems Architecture

This is a Kotlin JVM backend project implementing a Self-Contained Systems (SCS) architecture for an equestrian sport management system.

## Architecture Overview

The project follows Domain-Driven Design (DDD) principles with clearly separated bounded contexts:

### Implemented Modules

* **`shared-kernel`** - Common domain types, enums, serializers, validation utilities, and base DTOs
* **`master-data`** - Master data management (countries, regions, age classes, venues)
* **`member-management`** - Person and club/association management
* **`horse-registry`** - Horse registration and management
* **`event-management`** - Event and tournament management
* **`api-gateway`** - Central API gateway aggregating all services

### Module Dependencies

```
api-gateway
├── shared-kernel
├── master-data
├── member-management
├── horse-registry
└── event-management

event-management
├── shared-kernel
└── horse-registry

horse-registry
├── shared-kernel
└── member-management

member-management
├── shared-kernel
└── master-data

master-data
└── shared-kernel
```

## Technology Stack

- **Kotlin JVM** - Primary programming language
- **Ktor** - Web framework for REST APIs
- **Exposed** - Database ORM
- **PostgreSQL** - Database
- **Kotlinx Serialization** - JSON serialization
- **Gradle** - Build system

## Getting Started

### Prerequisites
- JDK 17 or higher
- PostgreSQL database

### Building the Project
```bash
./gradlew build
```

### Running the API Gateway
```bash
./gradlew :api-gateway:run
```

## Documentation

See the `docs/` directory for detailed architecture documentation and diagrams.

### API Documentation

The project includes comprehensive API documentation for all endpoints:

- **Central API Documentation**: Access the central API documentation page at `/docs` (or `/api` which redirects to `/docs`)
- **Swagger UI**: Access the interactive API documentation at `/swagger` when the application is running
- **OpenAPI Specification**: The OpenAPI specification is available at `/openapi`
- **JSON API Overview**: A JSON representation of the API structure is available at `/api/json`
- **Developer Guidelines**: Guidelines for documenting APIs are available in [docs/API_DOCUMENTATION_GUIDELINES.md](docs/API_DOCUMENTATION_GUIDELINES.md)

The API documentation covers all bounded contexts:
- Authentication API
- Master Data API
- Member Management API
- Horse Registry API
- Event Management API

### How to Use the API Documentation

1. Start the application with `./gradlew :api-gateway:run`
2. For a comprehensive documentation portal, navigate to `http://localhost:8080/docs`
3. For detailed interactive documentation, navigate to `http://localhost:8080/swagger`
4. For the raw OpenAPI specification, navigate to `http://localhost:8080/openapi`
5. Explore the available endpoints, request/response models, and authentication requirements
6. Test API calls directly from the Swagger UI interface

The central documentation page provides:
- Overview of the API architecture
- Details about all API contexts and their endpoints
- Links to additional documentation resources
- Authentication instructions
- Response format examples

### For Developers

When adding or modifying API endpoints, please follow the [API Documentation Guidelines](docs/API_DOCUMENTATION_GUIDELINES.md). These guidelines ensure consistency across all API documentation and make it easier for developers, testers, and API consumers to understand and use our APIs.

## Last Updated

2025-07-21
