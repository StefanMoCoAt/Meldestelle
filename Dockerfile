# ----------- Stage 1: Build Stage -----------
FROM gradle:8.14-jdk21 AS build
WORKDIR /home/gradle/src

# Copy only the files needed for dependency resolution first
# This improves caching of dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Download dependencies and cache them
RUN gradle dependencies --no-daemon

# Copy source code
COPY shared-kernel ./shared-kernel
COPY api-gateway ./api-gateway
COPY master-data ./master-data
COPY member-management ./member-management
COPY horse-registry ./horse-registry
COPY event-management ./event-management
COPY composeApp ./composeApp
COPY server ./server

# Build with optimized settings
RUN gradle :api-gateway:shadowJar --no-daemon --parallel --build-cache

# ----------- Stage 2: Runtime Stage -----------
FROM openjdk:21-slim-bookworm AS runtime

# Add non-root user for security
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --gid 1001 appuser

# Set timezone
ENV TZ=Europe/Vienna
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /home/gradle/src/api-gateway/build/libs/*.jar ./app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Add metadata labels
LABEL org.opencontainers.image.title="Meldestelle API Gateway"
LABEL org.opencontainers.image.description="API Gateway for Meldestelle application"
LABEL org.opencontainers.image.vendor="MoCode"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.created="2025-07-21"

# Expose the application port
EXPOSE 8081

# Define health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/health || exit 1

# Run the application with optimized JVM settings
ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=100", \
  "-XX:+ParallelRefProcEnabled", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/app.jar"]
