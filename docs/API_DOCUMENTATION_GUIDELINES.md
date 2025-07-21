# API Documentation Guidelines

## Overview

This document provides guidelines for documenting APIs in the Meldestelle project. Following these guidelines ensures consistency across all API documentation and makes it easier for developers, testers, and API consumers to understand and use our APIs.

## Table of Contents

1. [Documentation Approach](#documentation-approach)
2. [OpenAPI Specification](#openapi-specification)
3. [Endpoint Documentation Standards](#endpoint-documentation-standards)
4. [Schema Documentation Standards](#schema-documentation-standards)
5. [Examples](#examples)
6. [Documentation Workflow](#documentation-workflow)
7. [Testing Documentation](#testing-documentation)
8. [Tools and Resources](#tools-and-resources)

## Documentation Approach

The Meldestelle project uses a **static OpenAPI YAML file** for API documentation. This means:

- API documentation is maintained in a dedicated YAML file, not generated from code annotations
- Developers must manually update the documentation when adding or modifying endpoints
- The documentation is served via Swagger UI and as static HTML

### Key Files

- **OpenAPI Specification**: `/api-gateway/src/jvmMain/resources/openapi/documentation.yaml`
- **OpenAPI Configuration**: `/api-gateway/src/jvmMain/kotlin/at/mocode/gateway/config/OpenApiConfig.kt`
- **Documentation Routes**: `/api-gateway/src/jvmMain/kotlin/at/mocode/gateway/routing/DocRoutes.kt`
- **Static HTML Documentation**: `/api-gateway/src/jvmMain/resources/static/docs/index.html`

## OpenAPI Specification

We use OpenAPI 3.0.3 for our API documentation. The specification is maintained in a YAML file at:
`/api-gateway/src/jvmMain/resources/openapi/documentation.yaml`

### Structure

The OpenAPI specification file is structured as follows:

```yaml
openapi: 3.0.3
info:
  title: Meldestelle API
  description: |
    Self-Contained Systems API Gateway for Austrian Equestrian Federation.
  version: 1.0.0
  # Additional info fields...

servers:
  - url: https://api.meldestelle.at
    description: Production server
  - url: https://staging-api.meldestelle.at
    description: Staging server
  - url: http://localhost:8080
    description: Local development server

tags:
  - name: Authentication
    description: Authentication and authorization endpoints
  - name: Horse Registry
    description: Horse registration and management
  - name: Events
    description: Event management endpoints
  - name: Master Data
    description: Master data management

paths:
  # API endpoints...

components:
  schemas:
    # Data models...
  securitySchemes:
    # Security definitions...
```

## Endpoint Documentation Standards

When documenting a new API endpoint, include the following information:

### Required Elements

1. **Path and HTTP Method**: Define the endpoint path and HTTP method (GET, POST, PUT, DELETE)
2. **Tags**: Assign at least one tag to categorize the endpoint (e.g., Authentication, Master Data)
3. **Summary**: A brief one-line description of the endpoint
4. **Description**: A more detailed explanation of what the endpoint does
5. **Operation ID**: A unique identifier for the operation (camelCase)
6. **Responses**: Document all possible response status codes and their content
7. **Security**: Specify authentication requirements if applicable

### Optional Elements (Recommended)

1. **Request Body**: For POST/PUT methods, document the expected request body
2. **Parameters**: Document path, query, and header parameters
3. **Examples**: Provide example requests and responses

### Example Endpoint Documentation

```yaml
/auth/login:
  post:
    tags:
      - Authentication
    summary: User Login
    description: Authenticates a user and returns a JWT token
    operationId: login
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/LoginRequest'
          example:
            username: "user@example.com"
            password: "SecurePassword123!"
    responses:
      '200':
        description: Successful login
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginResponse'
            example:
              success: true
              data:
                token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                userId: "550e8400-e29b-41d4-a716-446655440000"
                personId: "550e8400-e29b-41d4-a716-446655440001"
                username: "user@example.com"
                email: "user@example.com"
              message: "Login successful"
              timestamp: "2024-07-21T13:35:00Z"
      '401':
        description: Invalid credentials
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
```

## Schema Documentation Standards

When documenting data models (schemas), include the following information:

### Required Elements

1. **Schema Name**: Use PascalCase for schema names (e.g., `LoginRequest`)
2. **Type**: Specify the type (usually `object` for complex types)
3. **Properties**: List all properties with their types and descriptions
4. **Required Properties**: Specify which properties are required

### Optional Elements (Recommended)

1. **Examples**: Provide example values for properties
2. **Format**: Specify formats for string types (e.g., `email`, `uuid`, `date-time`)
3. **Enums**: For properties with a fixed set of values, specify the allowed values

### Example Schema Documentation

```yaml
LoginRequest:
  type: object
  properties:
    username:
      type: string
      description: The user's email address or username
      format: email
      example: "user@example.com"
    password:
      type: string
      description: The user's password
      format: password
      example: "SecurePassword123!"
  required:
    - username
    - password
```

## Examples

For a complete example of how to apply these guidelines to a new endpoint, see [API_DOCUMENTATION_EXAMPLE.md](API_DOCUMENTATION_EXAMPLE.md).

### Well-Documented Endpoint Example

Here's an example of a well-documented endpoint:

```yaml
/api/horses/{id}:
  get:
    tags:
      - Horse Registry
    summary: Get Horse by ID
    description: |
      Retrieves detailed information about a specific horse by its unique identifier.
      Requires authentication and appropriate permissions.
    operationId: getHorseById
    parameters:
      - name: id
        in: path
        description: Unique identifier of the horse
        required: true
        schema:
          type: string
          format: uuid
    security:
      - bearerAuth: []
    responses:
      '200':
        description: Successful operation
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/HorseResponse'
            example:
              success: true
              data:
                id: "550e8400-e29b-41d4-a716-446655440000"
                name: "Maestoso Mara"
                birthYear: 2015
                breed: "Lipizzaner"
                color: "Grey"
                gender: "STALLION"
                feiRegistered: true
                ownerId: "550e8400-e29b-41d4-a716-446655440001"
                active: true
              message: "Horse retrieved successfully"
              timestamp: "2024-07-21T13:35:00Z"
      '401':
        description: Unauthorized - Authentication required
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '403':
        description: Forbidden - Insufficient permissions
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '404':
        description: Horse not found
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
```

## Documentation Workflow

Follow these steps when adding or modifying API endpoints:

1. **Implement the API endpoint** in the appropriate controller/route file
2. **Update the OpenAPI specification** in `documentation.yaml`
3. **Generate the documentation** using the Gradle task:
   ```bash
   ./gradlew :api-gateway:generateApiDocs
   ```
4. **Validate the documentation** using the Gradle task:
   ```bash
   ./gradlew :api-gateway:validateOpenApi
   ```
5. **Test the documentation** by accessing the Swagger UI at `http://localhost:8080/swagger`

### CI/CD Pipeline

The project includes a CI/CD pipeline that automatically:
- Validates the OpenAPI specification
- Generates updated documentation
- Deploys the documentation to GitHub Pages

The workflow is defined in `.github/workflows/api-docs.yml` and is triggered:
- On changes to OpenAPI-related files
- On a weekly schedule
- Manually via GitHub Actions UI

## Testing Documentation

Always test your API documentation to ensure it accurately represents the API:

1. **Start the API Gateway**:
   ```bash
   ./gradlew :api-gateway:run
   ```

2. **Access Swagger UI**:
   Open your browser and navigate to `http://localhost:8080/swagger`

3. **Test the documented endpoints**:
   - Verify that all parameters are correctly documented
   - Test example requests
   - Verify that responses match the documentation

4. **Check static HTML documentation**:
   Open your browser and navigate to `http://localhost:8080/docs`

## Tools and Resources

### Recommended Tools

- **Swagger Editor**: [https://editor.swagger.io/](https://editor.swagger.io/) - Online editor for OpenAPI specifications
- **OpenAPI Validator**: Built into our Gradle tasks (`validateOpenApi`)
- **Postman**: For testing APIs and generating collections

### Learning Resources

- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [Swagger UI Documentation](https://swagger.io/docs/open-source-tools/swagger-ui/usage/configuration/)
- [OpenAPI Best Practices](https://oai.github.io/Documentation/best-practices.html)

## Conclusion

Following these guidelines ensures that our API documentation is consistent, comprehensive, and useful for all stakeholders. Good API documentation is a critical part of our development process and helps ensure the usability and maintainability of our APIs.

If you have questions or suggestions for improving these guidelines, please contact the API team.
