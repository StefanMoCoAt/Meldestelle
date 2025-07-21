# API Documentation Example

This document demonstrates how to apply the API documentation guidelines to a new endpoint. It serves as a practical example for developers to follow when documenting their own API endpoints.

## Example Scenario

Let's say we're adding a new endpoint to the Horse Registry context that allows users to search for horses by multiple criteria.

## Step 1: Implement the API Endpoint

First, we would implement the endpoint in the appropriate route file:

```kotlin
// In HorseRoutes.kt
route("/api/horses") {
    // Other endpoints...

    // Advanced search endpoint
    get("/advanced-search") {
        // Parameter validation
        val name = call.request.queryParameters["name"]
        val breed = call.request.queryParameters["breed"]
        val minAge = call.request.queryParameters["minAge"]?.toIntOrNull()
        val maxAge = call.request.queryParameters["maxAge"]?.toIntOrNull()
        val gender = call.request.queryParameters["gender"]
        val ownerName = call.request.queryParameters["ownerName"]

        // Call service to perform search
        val horses = horseService.advancedSearch(
            name = name,
            breed = breed,
            minAge = minAge,
            maxAge = maxAge,
            gender = gender,
            ownerName = ownerName
        )

        // Return response
        call.respond(
            ApiResponse.success(
                data = horses,
                message = "Horses retrieved successfully"
            )
        )
    }
}
```

## Step 2: Document the Endpoint in OpenAPI Specification

Following our API documentation guidelines, we would add the following to the OpenAPI specification file (`documentation.yaml`):

```yaml
/api/horses/advanced-search:
    get:
        tags:
            - Horse Registry
        summary: Advanced Horse Search
        description: |
            Searches for horses using multiple optional criteria.
            Returns a list of horses matching the specified criteria.
            If no criteria are provided, returns all horses (subject to pagination).
        operationId: advancedSearchHorses
        parameters:
            -   name: name
                in: query
                description: Full or partial horse name to search for
                required: false
                schema:
                    type: string
                    example: "Maestoso"
            -   name: breed
                in: query
                description: Horse breed
                required: false
                schema:
                    type: string
                    example: "Lipizzaner"
            -   name: minAge
                in: query
                description: Minimum age in years
                required: false
                schema:
                    type: integer
                    format: int32
                    minimum: 0
                    example: "3"
            -   name: maxAge
                in: query
                description: Maximum age in years
                required: false
                schema:
                    type: integer
                    format: int32
                    minimum: 0
                    example: "15"
            -   name: gender
                in: query
                description: Horse gender
                required: false
                schema:
                    type: string
                    enum: [ STALLION, MARE, GELDING ]
                    example: "MARE"
            -   name: ownerName
                in: query
                description: Full or partial name of the horse's owner
                required: false
                schema:
                    type: string
                    example: "Schmidt"
        security:
            -   bearerAuth: [ ]
        responses:
            '200':
                description: Successful operation
                content:
                    application/json:
                        schema:
                            type: object
                            properties:
                                success:
                                    type: boolean
                                    example: true
                                data:
                                    type: array
                                    items:
                                        $ref: '#/components/schemas/HorseResponse'
                                message:
                                    type: string
                                    example: "Horses retrieved successfully"
                                timestamp:
                                    type: string
                                    format: date-time
                                    example: "2024-07-21T13:35:00Z"
                        example:
                            success: true
                            data: [
                                {
                                    "id": "550e8400-e29b-41d4-a716-446655440000",
                                    "name": "Maestoso Mara",
                                    "birthYear": 2015,
                                    "breed": "Lipizzaner",
                                    "color": "Grey",
                                    "gender": "MARE",
                                    "feiRegistered": true,
                                    "ownerId": "550e8400-e29b-41d4-a716-446655440001",
                                    "active": true
                                },
                                {
                                    "id": "550e8400-e29b-41d4-a716-446655440002",
                                    "name": "Maestoso Belvedere",
                                    "birthYear": 2018,
                                    "breed": "Lipizzaner",
                                    "color": "Grey",
                                    "gender": "STALLION",
                                    "feiRegistered": false,
                                    "ownerId": "550e8400-e29b-41d4-a716-446655440001",
                                    "active": true
                                }
                            ]
                            message: "Horses retrieved successfully"
                            timestamp: "2024-07-21T13:35:00Z"
            '400':
                description: Invalid parameters
                content:
                    application/json:
                        schema:
                            $ref: '#/components/schemas/ErrorResponse'
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
```

## Step 3: Generate and Validate Documentation

After updating the OpenAPI specification, we would generate and validate the documentation:

```bash
# Generate API documentation
./gradlew :api-gateway:generateApiDocs

# Validate OpenAPI specification
./gradlew :api-gateway:validateOpenApi
```

## Step 4: Test the Documentation

Finally, we would test the documentation by:

1. Starting the API Gateway:
   ```bash
   ./gradlew :api-gateway:run
   ```

2. Accessing Swagger UI at `http://localhost:8080/swagger`

3. Testing the new endpoint through the Swagger UI interface

4. Verifying that the documentation accurately represents the API behavior

## Summary

This example demonstrates how to apply the API documentation guidelines to a new endpoint. By following these steps, we ensure that:

1. The endpoint is well-documented with clear descriptions
2. All parameters are properly documented with types and examples
3. All possible responses are documented with status codes and examples
4. The documentation is validated and tested
5. The documentation is consistent with the rest of the API

This approach makes it easier for other developers, testers, and API consumers to understand and use the API.
