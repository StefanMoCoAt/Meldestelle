# Use Eclipse Temurin for better security, smaller image size, and active support
FROM eclipse-temurin:21-jre-alpine

# Add metadata labels
LABEL maintainer="Meldestelle Team"
LABEL description="API Gateway for Meldestelle System"
LABEL version="1.0"

# Install curl for health checks and create non-root user
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S gateway && \
    adduser -u 1001 -S gateway -G gateway

# Set working directory
WORKDIR /app

# Copy the gateway JAR file and set ownership
COPY infrastructure/gateway/build/libs/*.jar app.jar
RUN chown gateway:gateway app.jar

# Switch to non-root user
USER gateway

# Expose port
EXPOSE 8080

# Add optimized health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Configure JVM for containerized Spring Boot reactive application
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application with optimized JVM settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
