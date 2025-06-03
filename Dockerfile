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
WORKDIR /app/server
RUN gradle clean build -x test

# ---------- Stage 2: Runtime ----------
FROM openjdk:21-ea-18-jdk AS runtime

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/server/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
