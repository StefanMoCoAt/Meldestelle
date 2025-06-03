# ---------- Stage 1: Build ----------
FROM gradle:8.14-jdk-21-and-24 AS build

# Set working directory inside container
WORKDIR /app

# Copy root-level Gradle config, including settings and version catalogs
COPY settings.gradle.kts /app/settings.gradle.kts
COPY build.gradle.kts /app/build.gradle.kts
COPY gradle.properties /app/gradle.properties
COPY gradle /app/gradle

# Copy the shared module (used by composeApp)
COPY shared /app/shared

# Copy the composeApp module source and build script
COPY composeApp /app/composeApp

# Build the WebAssembly JavaScript application
WORKDIR /app
RUN gradle :composeApp:wasmJsBrowserDistribution -x test

# ---------- Stage 2: Runtime ----------
FROM nginx:alpine AS runtime

# Copy the Nginx configuration
COPY composeApp/nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built WebAssembly JavaScript application
COPY --from=build /app/composeApp/build/dist/wasmJs/browserDistribution /usr/share/nginx/html

# Expose the port the app runs on
EXPOSE 80

# Run Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]
