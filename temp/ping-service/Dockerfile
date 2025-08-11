FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the ping-service JAR file
COPY temp/ping-service/build/libs/*.jar app.jar

# Expose port (will be assigned dynamically by Spring Boot)
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8080/ping || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
