# ----------- Stage 1: Build Stage -----------
FROM gradle:8.14.3-jdk21 AS build
WORKDIR /home/gradle/src

# Copy only the files needed for dependency resolution first
# This improves caching of dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Download dependencies and cache them in separate layer
RUN gradle dependencies --no-daemon --quiet

# Copy source code in order of change frequency (least to most likely to change)
COPY core ./core
COPY platform ./platform
COPY infrastructure ./infrastructure
COPY masterdata ./masterdata
COPY members ./members
COPY horses ./horses
COPY events ./events

# Build with optimized settings
RUN gradle :infrastructure:gateway:shadowJar --no-daemon --parallel --build-cache --quiet

# ----------- Stage 2: Runtime Stage -----------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install curl for health checks and ca-certificates for SSL
RUN apk add --no-cache curl ca-certificates tzdata

# Add non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Set timezone
ENV TZ=Europe/Vienna
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /home/gradle/src/infrastructure/gateway/build/libs/*.jar ./app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Add metadata labels (OCI Image Format Specification)
LABEL org.opencontainers.image.title="Meldestelle API Gateway"
LABEL org.opencontainers.image.description="API Gateway for Meldestelle horse sport registration system"
LABEL org.opencontainers.image.vendor="MoCode"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.created="2025-07-24"
LABEL org.opencontainers.image.source="https://github.com/mocode/meldestelle"
LABEL org.opencontainers.image.documentation="https://github.com/mocode/meldestelle/blob/main/README.md"
LABEL org.opencontainers.image.licenses="MIT"

# Expose the application port
EXPOSE 8081

# Define health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/health || exit 1

# Run the application with optimized JVM settings for containerized environment
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=100", \
  "-XX:+ParallelRefProcEnabled", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-XX:+UnlockExperimentalVMOptions", \
  "-XX:+UseCGroupMemoryLimitForHeap", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=Europe/Vienna", \
  "-jar", "/app/app.jar"]
