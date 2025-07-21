# Cleanup Implementation Plan

This document outlines the plan for cleaning up the codebase, improving its structure, and enhancing maintainability as requested.

## 1. Directory Structure Standardization

### 1.1 Fix api-gateway Module Inconsistency

The api-gateway module currently has an inconsistent directory structure with both `src/main` and `src/jvmMain` directories, which causes confusion and potential maintenance issues.

**Actions:**
1. Compare files in both directories to identify duplicates and differences
2. Consolidate all code into the `src/jvmMain` directory to be consistent with other modules
3. Update any references to the old directory structure
4. Remove the `src/main` directory after ensuring all functionality is preserved

### 1.2 Standardize Module Structure

Ensure all modules follow the same directory structure pattern:
- `src/jvmMain/kotlin` for backend code
- `src/jsMain/kotlin` for frontend code
- `src/commonMain/kotlin` for shared code
- `src/jvmTest/kotlin` for backend tests
- `src/jsTest/kotlin` for frontend tests
- `src/commonTest/kotlin` for shared tests

## 2. Test Files Organization

### 2.1 Move Standalone Test Scripts

Move the following standalone test scripts from the root directory to appropriate test directories:
- `test_authentication.kt` → `member-management/src/jvmTest/kotlin/at/mocode/members/test/`
- `test_authentication_authorization.kt` → `api-gateway/src/jvmTest/kotlin/at/mocode/gateway/test/`
- `test_validation.kt` → `shared-kernel/src/jvmTest/kotlin/at/mocode/validation/test/`
- `database-integration-test.kt` → `shared-kernel/src/jvmTest/kotlin/at/mocode/shared/database/test/`

### 2.2 Convert to Proper Unit Tests

Convert the standalone test scripts to proper unit tests using the Kotlin test framework:
1. Add appropriate test annotations
2. Organize tests into test classes
3. Use proper assertions
4. Set up test dependencies

## 3. Documentation Cleanup

### 3.1 Update Outdated Documentation

Update the following documentation files to reflect the current project structure:
- `README_CODE_ORGANIZATION.md` - Update to reflect actual code organization
- `README_API_Implementation.md` - Verify and update if needed
- Other README files - Review and update as needed

### 3.2 Consolidate Documentation

Consolidate fragmented documentation into a more organized structure:
1. Create a `docs` directory with subdirectories for different topics
2. Move documentation files from the root directory to appropriate subdirectories
3. Create an index document that links to all documentation
4. Update the main README.md to point to the new documentation structure

Suggested structure:
```
docs/
├── architecture/
│   ├── code-organization.md
│   └── system-overview.md
├── api/
│   ├── api-implementation.md
│   └── validation.md
├── database/
│   ├── setup.md
│   └── migrations.md
├── security/
│   ├── authentication.md
│   └── authorization.md
└── development/
    ├── getting-started.md
    └── testing.md
```

### 3.3 Remove Redundant Documentation

Identify and remove any redundant or obsolete documentation files after consolidation.

## 4. Code Cleanup

### 4.1 Remove Duplicate Code

Identify and remove duplicate code, particularly in the api-gateway module where files exist in both src/main and src/jvmMain.

### 4.2 Standardize Naming Conventions

Ensure consistent naming conventions across the codebase:
- Repository interfaces: `EntityRepository`
- Repository implementations: `EntityRepositoryImpl`
- Service interfaces: `EntityService`
- Service implementations: `EntityServiceImpl`
- Controllers/Routes: `EntityRoutes`
- Data classes: `EntityDto` for DTOs, `Entity` for domain models

### 4.3 Improve Separation of Concerns

Ensure proper separation of concerns:
- Domain logic in domain layer
- Infrastructure concerns in infrastructure layer
- API endpoints in presentation layer
- Shared utilities in appropriate utility classes

## 5. Implementation Approach

### 5.1 Phase 1: Directory Structure and Documentation

1. Fix api-gateway module directory structure
2. Move standalone test scripts
3. Update and consolidate documentation

### 5.2 Phase 2: Code Cleanup

1. Remove duplicate code
2. Standardize naming conventions
3. Improve separation of concerns

### 5.3 Phase 3: Verification

1. Ensure the project builds correctly
2. Run tests to verify functionality
3. Manual testing of key features

## 6. Success Criteria

The cleanup will be considered successful when:
1. All modules follow a consistent directory structure
2. All test files are properly organized in test directories
3. Documentation is accurate, up-to-date, and well-organized
4. No duplicate code or files exist
5. Naming conventions are consistent
6. The project builds and all tests pass

## Last Updated

2025-07-21
