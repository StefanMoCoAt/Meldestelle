# syntax=docker/dockerfile:1

# Build stage: compile the ping-service JAR inside Docker
FROM gradle:8.7-jdk17-alpine AS builder
WORKDIR /workspace

# Copy the entire repository (simplest and most robust for multi-module setups)
# This allows building the ping-service even if it depends on shared build files or platforms.
COPY . .

# Build only the ping-service artifact
RUN gradle :temp:ping-service:bootJar --no-daemon

# Runtime stage: slim JRE image to run the service
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Install curl for health checks (small footprint on Alpine)
RUN apk add --no-cache curl

# Create a non-root user for better security
RUN addgroup -S app && adduser -S app -G app
USER app

# Copy the built JAR from the builder stage
COPY --from=builder /workspace/temp/ping-service/build/libs/*.jar app.jar

# Expose application port
EXPOSE 8082

# Health check against the ping endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -fsS http://localhost:8082/ping || exit 1

# JVM options can be overridden at runtime via JAVA_OPTS env
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
