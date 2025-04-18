# ----------- Stage 1: Build Stage -----------
FROM gradle:8.13-jdk21 AS build
WORKDIR /home/gradle/src
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY shared ./shared
COPY server ./server
RUN gradle :server:shadowJar --no-configure-on-demand

# ----------- Stage 2: Runtime Stage -----------
FROM openjdk:21-slim-bookworm AS runtime
WORKDIR /app
COPY --from=build /home/gradle/src/server/build/libs/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]