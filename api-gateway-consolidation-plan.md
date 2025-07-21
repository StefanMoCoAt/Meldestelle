# API Gateway Consolidation Plan

This document outlines the plan for consolidating the duplicate directory structure in the api-gateway module, specifically merging the `src/main` and `src/jvmMain` directories.

## 1. File Analysis

### 1.1 Duplicate Files

The following files exist in both directories:

| File | Action | Reasoning |
|------|--------|-----------|
| Application.kt | Merge into src/jvmMain | src/jvmMain version has better configuration handling, but src/main has more complete module setup |
| config/AuthorizationConfig.kt | Keep src/jvmMain version | Assuming identical content |
| config/DatabaseConfig.kt | Keep src/jvmMain version | Assuming identical content |
| config/MonitoringConfig.kt | Keep src/jvmMain version | Confirmed identical content |
| config/OpenApiConfig.kt | Keep src/jvmMain version | Assuming identical content |
| config/SecurityConfig.kt | Keep src/jvmMain version | Assuming identical content |
| config/SerializationConfig.kt | Keep src/jvmMain version | Assuming identical content |
| routing/AuthRoutes.kt | Keep src/jvmMain version | Assuming identical content |

### 1.2 Files Unique to src/main

The following files exist only in src/main:

| File | Action | Reasoning |
|------|--------|-----------|
| auth/AuthorizationHelper.kt | Move to src/jvmMain | Contains important authorization functionality not present in src/jvmMain |
| routing/RoutingConfig.kt | Move to src/jvmMain | Contains critical routing configuration not present in src/jvmMain |
| config/configureSwagger.kt | Check if needed | Not referenced in src/jvmMain, but referenced in src/main Application.kt |

### 1.3 Files Unique to src/jvmMain

The following files exist only in src/jvmMain:

| File | Action | Reasoning |
|------|--------|-----------|
| auth/ApiKeyAuth.kt | Keep | Provides API key authentication |
| auth/JwtAuth.kt | Keep and enhance | Provides JWT authentication, but should be enhanced with functionality from AuthorizationHelper.kt |
| config/MigrationSetup.kt | Keep | Handles database migrations |
| migrations/* (4 files) | Keep | Handle migrations for different contexts |
| module.kt | Merge with Application.kt | Contains module definition but needs to be enhanced with functionality from src/main |
| validation/RequestValidator.kt | Keep | Provides request validation |

## 2. Implementation Steps

### 2.1 Merge Application.kt and module.kt

1. Start with the src/jvmMain Application.kt
2. Incorporate the module configuration from src/main Application.kt
3. Ensure all necessary components are configured:
   - Database
   - Serialization
   - Monitoring
   - Security
   - OpenAPI/Swagger
   - Routing

### 2.2 Move Unique Files from src/main to src/jvmMain

1. Move AuthorizationHelper.kt to src/jvmMain/kotlin/at/mocode/gateway/auth/
2. Move RoutingConfig.kt to src/jvmMain/kotlin/at/mocode/gateway/routing/
3. Check if configureSwagger.kt is needed and move if necessary

### 2.3 Update References

1. Update imports in all files to reflect the new structure
2. Ensure all configuration functions are called in the module function

### 2.4 Remove src/main Directory

After all files have been consolidated and the application has been verified to work correctly, remove the src/main directory.

## 3. Testing

After consolidation, the following tests should be performed:

1. Build the project to ensure there are no compilation errors
2. Run the application to ensure it starts correctly
3. Test key functionality to ensure it works as expected:
   - Authentication
   - Authorization
   - API endpoints
   - Database operations

## 4. Documentation Update

Update the project documentation to reflect the new structure:

1. Update README.md if it references the old structure
2. Update any other documentation that mentions the directory structure
