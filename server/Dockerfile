# ---------- Stage 1: Build ----------
FROM gradle:8.14-jdk-21-and-24 AS build

# Set working directory inside container
WORKDIR /app

# Copy root-level Gradle config, including settings and version catalogs
COPY settings.gradle.kts /app/settings.gradle.kts
COPY build.gradle.kts /app/build.gradle.kts
COPY gradle.properties /app/gradle.properties
COPY gradle /app/gradle

# Copy the shared module (used by server)
COPY shared /app/shared

# Copy the server module source and build script
COPY server /app/server

# Build the Ktor server (this assumes the 'server' module builds its own jar)
WORKDIR /app
RUN gradle :server:build -x test

# ---------- Stage 2: Runtime ----------
FROM openjdk:21-ea-18-jdk AS runtime

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/server/build/libs/*.jar app.jar

# Create a directory for data persistence
RUN mkdir -p /app/data
VOLUME /app/data

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables for JVM tuning
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.awt.headless=true"

# Run the application
ENTRYPOINT java $JAVA_OPTS -jar app.jar
