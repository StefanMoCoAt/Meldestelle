FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the gateway JAR file
COPY infrastructure/gateway/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
