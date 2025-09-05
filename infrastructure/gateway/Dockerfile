# Dockerfile für das Meldestelle API Gateway
# Multi-Stage Build für optimierte Containerisierung

FROM eclipse-temurin:21-jdk-alpine AS build

# Arbeitsverzeichnis setzen
WORKDIR /workspace

# Gradle Wrapper und Build-Dateien kopieren
COPY gradle gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY build.gradle.kts ./

# Platform und Core Module kopieren (Dependencies)
COPY platform platform/
COPY core core/

# Infrastructure Module kopieren (für Dependencies)
COPY infrastructure infrastructure/

# Client Module kopieren (für Dependencies)
COPY client client/

# Documentation Module kopieren (für Dependencies)
COPY docs docs/

# Temporary Module kopieren (für Dependencies)
COPY temp temp/

# Gateway Module bauen
RUN ./gradlew :infrastructure:gateway:bootJar -x test --no-daemon

# JAR-Datei für Layer-Extraktion extrahieren
RUN mkdir -p build/dependency && \
    (cd build/dependency; java -Djarmode=layertools -jar /workspace/infrastructure/gateway/build/libs/*.jar extract)

# Runtime Stage - optimiert für Produktion
FROM eclipse-temurin:21-jre-alpine

# Metadaten für Container
LABEL maintainer="Meldestelle Development Team" \
      org.opencontainers.image.title="Meldestelle API Gateway" \
      org.opencontainers.image.description="Spring Cloud Gateway für die Meldestelle Microservices" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.vendor="Österreichischer Pferdesportverband"

# Non-root User für Security
RUN addgroup -g 1001 gateway && \
    adduser -D -u 1001 -G gateway gateway

# Arbeitsverzeichnis und Berechtigungen
WORKDIR /app
RUN chown gateway:gateway /app

# System-Updates für Security
RUN apk update && \
    apk add --no-cache tzdata curl && \
    rm -rf /var/cache/apk/*

# Zeitzone setzen
ENV TZ=Europe/Vienna

USER gateway

# Spring Boot Layer für besseres Caching
COPY --from=build --chown=gateway:gateway /workspace/build/dependency/dependencies/ ./
COPY --from=build --chown=gateway:gateway /workspace/build/dependency/spring-boot-loader/ ./
COPY --from=build --chown=gateway:gateway /workspace/build/dependency/snapshot-dependencies/ ./
COPY --from=build --chown=gateway:gateway /workspace/build/dependency/application/ ./

# Logs-Verzeichnis erstellen
RUN mkdir -p logs && chown gateway:gateway logs

# JVM-Parameter für Container-Umgebung
ENV JAVA_OPTS="-server -Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Spring Profile und Port
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Health Check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Gateway Port exposieren
EXPOSE 8080

# Anwendung starten
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
