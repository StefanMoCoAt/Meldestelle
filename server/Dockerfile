FROM gradle:8.14-jdk-21-and-24-alpine AS build
LABEL authors="Stefan Mogeritsch"
LABEL org.opencontainers.image.source="https://github.com/mogeritsch/WsMeldestelle"
LABEL org.opencontainers.image.description="Backend server for Meldestelle application"
LABEL org.opencontainers.image.version="1.0.0"

WORKDIR /home/gradle/src

# Copy only the files needed for dependency resolution first
# This improves layer caching - these layers won't change unless dependencies change
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Download dependencies - this will be cached if dependencies don't change
RUN gradle dependencies --no-daemon

# Now copy the source code
COPY shared ./shared
COPY server ./server

# Build the application
RUN gradle :server:shadowJar --no-daemon && \
    # Clean up to reduce image size
    rm -rf ~/.gradle/caches

# ----------- Stage 2: Runtime Stage -----------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Add a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /home/gradle/src/server/build/libs/*.jar ./app.jar

# Set ownership to the non-root user
RUN chown -R appuser:appgroup /app

# Use the non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD wget -q --spider http://localhost:8080/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.awt.headless=true"

# Run the application
ENTRYPOINT exec java $JAVA_OPTS -jar /app/app.jar
