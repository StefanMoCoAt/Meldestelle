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

# JVM-Parameter für Container-Umgebung (optimized for Java 21)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Vienna"

# Spring Profile und Port (configurable)
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
ENV SERVER_PORT=${GATEWAY_PORT:-8081}

# Health Check
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:${GATEWAY_PORT:-8081}/actuator/health || exit 1

# Gateway Port exposieren
EXPOSE ${GATEWAY_PORT:-8081}

# Anwendung starten
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
