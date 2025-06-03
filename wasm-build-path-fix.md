# WebAssembly Build Path Fix

## Problem

When running `docker compose up --build -d`, the frontend build process failed with the following error:

```
failed to solve: failed to compute cache key: failed to calculate checksum of ref b71a5148-153c-456d-b4d7-02b1e2868ad6::mu4vterod1zcx0eoae33ye8hd: "/app/composeApp/build/dist/wasmJs/browserDistribution": not found
```

This error occurred because the Dockerfile was trying to copy files from a directory that doesn't exist. The COPY command was looking for files in `/app/composeApp/build/dist/wasmJs/browserDistribution`, but the Kotlin/JS build process was generating the files in different locations.

## Solution

The solution was to update the COPY commands in the Dockerfile to use the correct paths for the build output:

```dockerfile
# Before
COPY --from=build /app/composeApp/build/dist/wasmJs/browserDistribution /usr/share/nginx/html

# After
COPY --from=build /app/composeApp/build/compileSync/wasmJs/main/developmentExecutable/kotlin/* /usr/share/nginx/html/
COPY --from=build /app/composeApp/build/processedResources/wasmJs/main/* /usr/share/nginx/html/
```

This change ensures that all the necessary files are copied to the nginx html directory:
1. The WebAssembly and JavaScript files from `/app/composeApp/build/compileSync/wasmJs/main/developmentExecutable/kotlin/`
2. The HTML, CSS, and other resource files from `/app/composeApp/build/processedResources/wasmJs/main/`

## Why This Works

The Kotlin/JS build process with the wasmJsBrowserDistribution task generates the output files in multiple directories:
- The compiled WebAssembly (.wasm) and JavaScript (.js, .mjs) files are in the `compileSync/wasmJs/main/developmentExecutable/kotlin` directory
- The HTML, CSS, and other resource files are in the `processedResources/wasmJs/main` directory

By copying files from both directories to the nginx html directory, we ensure that all the necessary files are available for the web application to run correctly.

## Additional Information

The original path (`/app/composeApp/build/dist/wasmJs/browserDistribution`) might have been based on an older version of the Kotlin/JS plugin or a different configuration. The current directory structure is typical for Kotlin/JS projects using the WebAssembly target with the Compose Multiplatform framework.

When working with Kotlin/JS and WebAssembly in Docker containers, it's important to understand the build output structure and ensure that the correct files are copied to the web server's document root.
