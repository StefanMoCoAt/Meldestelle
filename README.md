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

## Last Updated

2025-07-21
