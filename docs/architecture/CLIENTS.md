# Client Architecture (Kotlin Multiplatform)

This document summarizes the post-migration client setup and how it integrates with the overall architecture.

## Overview
- Single Kotlin Multiplatform module `:client`
- Targets:
  - Desktop (JVM) using Compose Desktop
  - Web (Browser) using Compose for Web (WASM)
- Shared UI and logic in `commonMain`; thin platform entry points in `jvmMain` and `wasmJsMain`

## Interaction with Backend
- Gateway exposes unified API under `/api/...`
- Client calls go through the gateway:
  - Desktop (JVM): Base URL from env `API_BASE_URL` (defaults to `http://localhost:8081`)
  - Web (WASM): Same-origin requests (e.g. `/api/ping`) – serve WASM bundle from the same host as the gateway or configure a reverse proxy

## Build & Run
- Desktop (JVM): `./gradlew :client:run`
- Web (WASM):
  - Dev server with live reload: `./gradlew :client:wasmJsBrowserDevelopmentRun`
  - Production build: `./gradlew :client:wasmJsBrowserProductionWebpack`

Artifacts:
- Desktop distributions: `client/build/compose/binaries`
- WASM production build: `client/build/dist/wasmJs/productionExecutable`

## WASM Bundle Analysis & Optimization
- Enable bundle analysis: `ANALYZE_BUNDLE=true ./gradlew :client:wasmJsBrowserProductionWebpack`
- Webpack augmentations in `client/webpack.config.d/`:
  - `bundle-analyzer.js`: logs asset sizes and optimization hints
  - `wasm-optimization.js`: enables tree-shaking, chunk splitting, and production optimizations
- Client-side Ktor setup is minimized to reduce bundle size (no extra plugins, lean JSON config)

## Testing Notes
- Browser-based JS tests (Karma/ChromeHeadless) are disabled to avoid local sandbox/headless issues
- JS tests run under Node/Mocha
- Integration tests for backend modules are available in their respective modules; run all tests with `./gradlew test`

## Current Limitations / TODOs
- Domain UIs (masterdata, members, horses, events) to be implemented in the client
- Authentication/session handling (Keycloak) to be integrated in the client
- Optional: add lightweight E2E (smoke) tests that traverse the full flow via the gateway

## Relation to C4 Diagrams
- See `docs/architecture/c4/` for Context and Container diagrams
- The `:client` module represents the User Interface container (Desktop/Web) communicating with the API Gateway container
