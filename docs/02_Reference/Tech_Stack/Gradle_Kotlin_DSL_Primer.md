# Gradle Kotlin DSL Primer

**Quelle:** [Original Gradle Documentation](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
**Kontext:** Dieses Dokument dient als Referenz für die im Projekt verwendete Gradle Kotlin DSL. Es fasst die wichtigsten Konzepte und Syntax-Elemente zusammen.

---

Gradle’s Kotlin DSL offers an alternative to the traditional Groovy DSL, delivering an enhanced editing experience in supported IDEs.

## Key Concepts

### Script File Names
*   Groovy DSL: `.gradle`
*   Kotlin DSL: `.gradle.kts`

To activate the Kotlin DSL, use the `.gradle.kts` extension for your build scripts, settings file (`settings.gradle.kts`), and initialization scripts (`init.gradle.kts`).

### Type-safe Model Accessors
The Kotlin DSL replaces Groovy's dynamic resolution with type-safe model accessors for elements contributed by plugins (configurations, tasks, extensions). This provides better IDE support (code completion, refactoring).

**Example:**
```kotlin
plugins {
    `java-library`
}

dependencies {
    // 'api', 'implementation' are type-safe accessors
    api("junit:junit:4.13")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks {
    // 'test' is a type-safe accessor for the Test task
    test {
        useJUnitPlatform()
    }
}
```

Accessors are available for elements contributed by plugins applied in the `plugins {}` block. For elements created dynamically later in the script, you must fall back to string-based lookups:
```kotlin
configurations.create("custom")

dependencies {
    "custom"("com.google.guava:guava:32.1.2-jre")
}
```

### Lazy Property Assignment
The Kotlin DSL supports lazy property assignment using the `=` operator for types like `Property` and `ConfigurableFileCollection`. This is the preferred way over the `set()` method.

```kotlin
// Instead of:
// javaVersion.set(JavaLanguageVersion.of(17))

// Use:
javaVersion = JavaLanguageVersion.of(17)
```

### Working with Containers
You can interact with containers like `tasks` or `configurations` in several ways:

1.  **Container API (using `named` and `register`):**
    ```kotlin
    tasks.named<Test>("test") {
        testLogging.showExceptions = true
    }
    tasks.register<Copy>("myCopy") {
        from("source")
        into("destination")
    }
    ```

2.  **Delegated Properties (using `by existing` and `by registering`):**
    ```kotlin
    val test by tasks.existing(Test::class) {
        testLogging.showStackTraces = true
    }
    val myCopy by tasks.registering(Copy::class) {
        from("source")
        into("destination")
    }
    ```

### Extra Properties
Access project or task-level extra properties via delegated properties:
```kotlin
// Define an extra property
val myNewProperty by extra("initial value")

// Read an existing extra property
val myExtraProperty: String by extra
```

### Kotlin DSL Plugin (`kotlin-dsl`)
This plugin is essential for developing build logic in Kotlin (e.g., in `buildSrc` or for convention plugins). It automatically applies the Kotlin plugin and adds necessary dependencies like `kotlin-stdlib` and `gradleKotlinDsl()`.

```kotlin
// buildSrc/build.gradle.kts
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}
```
