# API Documentation - Meldestelle Self-Contained Systems

## Overview

This document provides comprehensive documentation for the Meldestelle API Gateway, which aggregates all bounded context APIs into a unified interface while maintaining the independence of each context.

## Features Implemented

### ✅ OpenAPI/Swagger Integration
- **OpenAPI 3.0 specification** using static YAML file
- **Swagger UI** interactive documentation
- **Comprehensive API documentation** for all bounded contexts
- **Multiple server environments** (development, staging, production)

### ✅ Postman Collections
- **Comprehensive API collection** covering all endpoints
- **Environment variables** for easy configuration
- **Authentication token management** with automatic token extraction
- **Pre-configured request examples** for all endpoints

### ✅ API Tests
- **Integration tests** for all major endpoints
- **Authentication flow testing**
- **CRUD operation validation**
- **Error handling verification**

## API Structure

The API Gateway aggregates the following bounded contexts:

### 1. System Information
- `GET /` - API Gateway information
- `GET /health` - Health check for all contexts
- `GET /docs` - Central API documentation page
- `GET /api` - Redirects to central API documentation page
- `GET /api/json` - API documentation overview in JSON format
- `GET /swagger` - Interactive Swagger UI
- `GET /openapi` - Raw OpenAPI specification

### 2. Authentication Context (`/auth/*`)
- `POST /auth/register` - User registration
- `POST /auth/login` - User authentication
- `GET /auth/profile` - Get user profile
- `PUT /auth/profile` - Update user profile
- `POST /auth/change-password` - Change password

### 3. Master Data Context (`/api/masterdata/*`)
- `GET /api/masterdata/countries` - Get all countries
- `GET /api/masterdata/countries/active` - Get active countries
- `GET /api/masterdata/countries/{id}` - Get country by ID
- `GET /api/masterdata/countries/iso/{code}` - Get country by ISO code
- `POST /api/masterdata/countries` - Create country
- `PUT /api/masterdata/countries/{id}` - Update country
- `DELETE /api/masterdata/countries/{id}` - Delete country

### 4. Horse Registry Context (`/api/horses/*`)
- `GET /api/horses` - Get all horses
- `GET /api/horses/active` - Get active horses
- `GET /api/horses/{id}` - Get horse by ID
- `GET /api/horses/search` - Search horses by name
- `GET /api/horses/owner/{ownerId}` - Get horses by owner
- `POST /api/horses` - Create horse
- `PUT /api/horses/{id}` - Update horse
- `DELETE /api/horses/{id}` - Delete horse
- `DELETE /api/horses/batch` - Batch delete horses
- `GET /api/horses/stats` - Get horse statistics

### 5. Event Management Context (`/api/events/*`)
- `GET /api/events` - Get all events
- `GET /api/events/stats` - Get event statistics
- `POST /api/events` - Create event
- `GET /api/events/{id}` - Get event by ID
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event
- `GET /api/events/search` - Search events
- `GET /api/events/organizer/{organizerId}` - Get events by organizer

## Getting Started

### 1. Start the API Gateway

```bash
# Navigate to the project root
cd /path/to/meldestelle

# Run the API Gateway
./gradlew :api-gateway:run
```

The API will be available at `http://localhost:8080`

### 2. Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/swagger
```

This provides an interactive interface to explore and test all API endpoints.

### 3. Use Postman Collection

1. Import the Postman collection from `docs/postman/Meldestelle_API_Collection.json`
2. Set the `baseUrl` variable to `http://localhost:8080`
3. Start with the "System Information" folder to verify the API is running
4. Use the "Authentication Context" to get an auth token
5. The token will be automatically saved and used for authenticated endpoints

## Authentication

The API uses JWT (JSON Web Token) based authentication:

1. **Register** a new user via `POST /auth/register`
2. **Login** with credentials via `POST /auth/login`
3. **Extract the JWT token** from the login response
4. **Include the token** in the `Authorization` header: `Bearer <token>`

### Example Authentication Flow

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePassword123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+43123456789"
  }'

# 2. Login to get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePassword123!"
  }'

# 3. Use token for authenticated requests
curl -X GET http://localhost:8080/api/horses \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Response Format

All API responses follow a consistent format using the `BaseDto` wrapper:

```json
{
  "success": true,
  "data": {
    "example": "Actual response data goes here"
  },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response Format

```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Testing

### Running API Tests

```bash
# Run all API Gateway tests
./gradlew :api-gateway:test

# Run specific test class
./gradlew :api-gateway:test --tests "ApiIntegrationTest"

# Run with verbose output
./gradlew :api-gateway:test --info
```

### Test Coverage

The test suite covers:
- ✅ API Gateway information endpoints
- ✅ Health check functionality
- ✅ OpenAPI/Swagger integration
- ✅ Authentication endpoints structure
- ✅ Master data CRUD operations
- ✅ Horse registry endpoints
- ✅ Error handling and validation
- ✅ CORS configuration
- ✅ Content negotiation

## Development

### Adding New Endpoints

1. **Create the endpoint** in the appropriate controller
2. **Add route configuration** in `RoutingConfig.kt`
3. **Update Postman collection** with new requests
4. **Add integration tests** for the new functionality
5. **Update this documentation**

### OpenAPI Documentation

The API documentation is maintained in a static OpenAPI YAML file:

```yaml
# Location: api-gateway/src/jvmMain/resources/openapi/documentation.yaml

paths:
  /api/horses:
    get:
      tags:
        - Horse Registry
      summary: Get All Horses
      description: Returns a list of all horses
      operationId: getAllHorses
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Successful operation
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/HorsesResponse'
```

To update the API documentation:

1. Edit the `documentation.yaml` file in `api-gateway/src/jvmMain/resources/openapi/`
2. Follow the OpenAPI 3.0.3 specification format
3. Restart the application to see changes in Swagger UI

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | API Gateway port | `8080` |
| `DATABASE_URL` | Database connection URL | `jdbc:h2:mem:test` |
| `JWT_SECRET` | JWT signing secret | Generated |
| `CORS_ORIGINS` | Allowed CORS origins | `*` |

### Application Configuration

The API Gateway can be configured via `application.conf`:

```hocon
ktor {
    application {
        modules = [ at.mocode.gateway.ApplicationKt.module ]
    }

    deployment {
        port = 8080
        port = ${?SERVER_PORT}
    }
}

database {
    url = "jdbc:h2:mem:test"
    url = ${?DATABASE_URL}
    user = "sa"
    password = ""
}
```

## Monitoring and Logging

### Health Checks

The `/health` endpoint provides status information for all bounded contexts:

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "contexts": {
      "authentication": "UP",
      "master-data": "UP",
      "horse-registry": "UP"
    }
  }
}
```

### Logging

The API Gateway uses structured logging with the following levels:
- `ERROR` - System errors and exceptions
- `WARN` - Business logic warnings
- `INFO` - Request/response logging
- `DEBUG` - Detailed debugging information

## Security

### Authentication & Authorization

- **JWT-based authentication** for stateless security
- **Role-based access control** (RBAC) for fine-grained permissions
- **Password hashing** using bcrypt
- **Token expiration** and refresh mechanisms

### CORS Configuration

Cross-Origin Resource Sharing (CORS) is configured to allow:
- **Specific origins** for production environments
- **All HTTP methods** (GET, POST, PUT, DELETE, OPTIONS)
- **Custom headers** including Authorization

### Input Validation

All API endpoints implement:
- **Request body validation** using Kotlin serialization
- **Parameter validation** for path and query parameters
- **Business rule validation** in use case layers
- **SQL injection prevention** through parameterized queries

## Troubleshooting

### Common Issues

1. **Port already in use**
   ```bash
   # Check what's using port 8080
   lsof -i :8080
   # Kill the process or use a different port
   SERVER_PORT=8081 ./gradlew :api-gateway:run
   ```

2. **Database connection issues**
   ```bash
   # Check database configuration
   # Verify connection string and credentials
   # Ensure database server is running
   ```

3. **Authentication failures**
   ```bash
   # Verify JWT token is valid and not expired
   # Check Authorization header format: "Bearer <token>"
   # Ensure user has required permissions
   ```

### Debug Mode

Enable debug logging for troubleshooting:

```bash
# Run with debug logging
./gradlew :api-gateway:run --debug

# Or set log level in application.conf
logger.level = DEBUG
```

## Contributing

When contributing to the API:

1. **Follow REST conventions** for endpoint design
2. **Maintain backward compatibility** when possible
3. **Update documentation** for any API changes
4. **Add comprehensive tests** for new functionality
5. **Use consistent error handling** patterns

## Support

For API support and questions:
- **Documentation**: This file and Swagger UI
- **Issues**: Create GitHub issues for bugs
- **Testing**: Use Postman collection for manual testing
- **Monitoring**: Check `/health` endpoint for system status
