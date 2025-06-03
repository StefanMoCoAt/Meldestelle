# ---------- Stage 1: Build ----------
FROM gradle:8.14-jdk-21-and-24-alpine AS build

# Set working directory inside container
WORKDIR /app

# Copy root-level Gradle config, including settings and version catalogs
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle/
COPY shared ./shared/
COPY server ./server/

# Build the Ktor server with caching
RUN --mount=type=cache,target=/root/.gradle,id=backend-gradle-cache \
    gradle :server:build -x test --no-daemon

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Add metadata labels
LABEL maintainer="MoCode"
LABEL description="Backend for Meldestelle application"
LABEL version="1.0"

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/server/build/libs/*.jar app.jar

# Create a directory for data persistence and set proper permissions
RUN mkdir -p /app/data && \
    chown -R appuser:appgroup /app
VOLUME /app/data

# Switch to non-root user
USER appuser

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
    CMD wget -q --spider http://localhost:8080/health || exit 1

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables for JVM tuning
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.awt.headless=true -Dio.ktor.development=false"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
