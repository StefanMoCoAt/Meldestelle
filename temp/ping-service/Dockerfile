# syntax=docker/dockerfile:1

# Build stage: compile the ping-service JAR inside Docker
FROM gradle:8.14-jdk21-alpine AS builder
WORKDIR /workspace

# Enable Gradle build cache and daemon for faster builds
ENV GRADLE_OPTS="-Dorg.gradle.caching=true -Dorg.gradle.daemon=false"

# Copy gradle files first for better layer caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY build.gradle.kts ./
COPY platform/ platform/

# Copy only necessary source files for the ping-service and its dependencies
COPY temp/ping-service/ temp/ping-service/

# Download dependencies first (better caching)
RUN gradle :temp:ping-service:dependencies --no-daemon

# Build only the ping-service artifact
RUN gradle :temp:ping-service:bootJar --no-daemon

# Runtime stage: slim JRE image to run the service
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install curl for health checks (small footprint on Alpine)
RUN apk add --no-cache curl

# Create a non-root user for better security
RUN addgroup -S app && adduser -S app -G app

# Copy the built JAR from the builder stage
COPY --from=builder --chown=app:app /workspace/temp/ping-service/build/libs/*.jar app.jar

# Switch to non-root user
USER app

# Expose application port
EXPOSE 8082

# Health check against the actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8082/actuator/health || exit 1

# Enhanced JVM options for containerized Spring Boot applications
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
  -XX:+UseStringDeduplication \
  -XX:+UseG1GC \
  -XX:+UseContainerSupport \
  -XX:+OptimizeStringConcat \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.jmx.enabled=false"

# Run the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
