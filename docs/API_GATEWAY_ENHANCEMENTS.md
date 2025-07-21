# API Gateway Enhancements

This document describes the enhancements made to the API Gateway service, including rate limiting, improved request/response logging, and cross-service tracing with unique request IDs.

## Table of Contents

1. [Rate Limiting](#rate-limiting)
2. [Request/Response Logging](#requestresponse-logging)
3. [Cross-Service Tracing](#cross-service-tracing)
4. [Testing and Verification](#testing-and-verification)

## Rate Limiting

The API Gateway now includes enhanced rate limiting capabilities to protect the API from abuse and ensure fair usage.

### Features

- **Global Rate Limiting**: Limits the total number of requests across all endpoints
- **Endpoint-Specific Rate Limiting**: Different limits for different API endpoints
- **User-Type-Specific Rate Limiting**: Different limits based on user type (anonymous, authenticated, admin)
- **Sophisticated Request Key Generation**: Uses IP address, user agent, and user ID for more precise rate limiting
- **Rate Limit Headers**: Informs clients about rate limits and remaining requests

### Configuration

Rate limiting can be configured in the application properties:

```properties
# Enable/disable rate limiting
ratelimit.enabled=true

# Global rate limit settings
ratelimit.global.limit=100
ratelimit.global.periodMinutes=1

# Include rate limit headers in responses
ratelimit.includeHeaders=true

# Endpoint-specific rate limits can be configured in AppConfig.kt
```

### Response Headers

When rate limiting is enabled, the following headers are included in responses:

- `X-RateLimit-Enabled`: Indicates if rate limiting is enabled
- `X-RateLimit-Limit`: The global rate limit
- `X-RateLimit-Policy`: Description of the rate limit policy
- `X-RateLimit-Endpoint`: The endpoint being rate limited (if applicable)
- `X-RateLimit-Endpoint-Limit`: The limit for the specific endpoint (if applicable)
- `X-RateLimit-Endpoint-Period`: The period for the specific endpoint (if applicable)
- `X-RateLimit-UserType`: The user type (if authenticated)
- `X-RateLimit-UserType-Limit`: The limit for the user type (if authenticated)
- `X-RateLimit-UserType-Period`: The period for the user type (if authenticated)
- `Retry-After`: Seconds to wait before retrying (if rate limited)

## Request/Response Logging

The API Gateway now includes enhanced request and response logging to provide better visibility into API usage and performance.

### Features

- **Structured Logging**: JSON-like structured logging format for easier parsing
- **Sensitive Data Filtering**: Masks sensitive information in logs (passwords, tokens, etc.)
- **Performance Metrics**: Includes memory usage and other performance metrics
- **Configurable Logging Levels**: Different logging levels for different environments
- **Request/Response Correlation**: Links requests and responses with unique request IDs

### Configuration

Logging can be configured in the application properties:

```properties
# Logging level
logging.level=INFO

# Request/response logging
logging.requests=true
logging.responses=true

# Request header/parameter logging
logging.request.headers=true
logging.request.parameters=true

# Response header logging
logging.response.headers=true
logging.response.time=true

# Structured logging
logging.structured=true

# Exclude paths from logging
logging.exclude.paths=/health,/metrics,/favicon.ico
```

### Sensitive Data Filtering

The following types of data are automatically masked in logs:

- Authorization headers
- Cookies
- API keys
- Passwords
- Tokens
- Other sensitive parameters (configurable)

## Cross-Service Tracing

The API Gateway now includes enhanced cross-service tracing capabilities to track requests across multiple services.

### Features

- **Unique Request IDs**: Generates unique request IDs with context information
- **Request ID Propagation**: Propagates request IDs to downstream services
- **Additional Tracing Headers**: Includes additional headers for better correlation
- **W3C Trace Context Compatibility**: Compatible with the W3C Trace Context standard
- **Enhanced Logging**: Includes tracing information in logs

### Request ID Format

Request IDs now include more context information:

```
req-{environment}-{service}-{timestamp}-{uuid}
```

Example:
```
req-prod-gateway-1627384950123-550e8400-e29b-41d4-a716-446655440000
```

### Tracing Headers

The following headers are included in responses:

- `X-Request-ID`: The unique request ID
- `X-Correlation-ID`: Same as the request ID, for compatibility
- `X-Request-Start-Time`: When the request started
- `X-Service-Name`: The name of the service
- `X-Service-Version`: The version of the service
- `X-Response-Time`: How long the request took to process
- `traceparent`: W3C Trace Context compatible trace parent header

## Testing and Verification

### Rate Limiting

To test rate limiting:

1. Send multiple requests to the same endpoint in quick succession
2. Observe the rate limit headers in the responses
3. After exceeding the rate limit, you should receive a 429 Too Many Requests response
4. Check the logs for rate limit exceeded messages

Example using curl:
```bash
# Send multiple requests
for i in {1..150}; do
  curl -i -X GET http://localhost:8080/api/v1/events
done
```

### Request/Response Logging

To verify enhanced logging:

1. Send requests to various endpoints
2. Check the logs for structured log entries
3. Verify that sensitive data is properly masked
4. Check for performance metrics in the logs

Example log entry:
```
timestamp=2025-07-21T16:45:23.456 method=GET path=/api/v1/events status=200 client=127.0.0.1 requestId=req-prod-gateway-1627384950123-550e8400-e29b-41d4-a716-446655440000 duration=42ms memoryUsage=1234567b
```

### Cross-Service Tracing

To verify cross-service tracing:

1. Send a request to an endpoint that calls other services
2. Check the response headers for tracing headers
3. Verify that the request ID is propagated to downstream services
4. Check the logs for correlated request and response entries

Example using curl:
```bash
# Send a request and check headers
curl -i -X GET http://localhost:8080/api/v1/events
```

Look for headers like:
```
X-Request-ID: req-prod-gateway-1627384950123-550e8400-e29b-41d4-a716-446655440000
X-Correlation-ID: req-prod-gateway-1627384950123-550e8400-e29b-41d4-a716-446655440000
X-Request-Start-Time: 1627384950123
X-Service-Name: API Gateway
X-Service-Version: 1.0.0
X-Response-Time: 42
traceparent: 00-550e8400e29b41d4a716446655440000-abcdef0123456789-01
```
