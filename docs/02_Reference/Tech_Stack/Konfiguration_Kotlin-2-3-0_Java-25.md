# Tech-Stack Referenz: Kotlin 2.3.0 & Java 25 (KMP)

**Kontext:** Dieses Dokument beschreibt die notwendigen Konfigurationen, um Kotlin 2.3.0 mit Java 25 in einem Kotlin Multiplatform (KMP) Projekt mit Gradle 9.x zu verwenden.

---

### 1. Kern-Spezifikationen

| Komponente | Version  | Status |
| --- |----------| --- |
| **Kotlin** | `2.3.0`  | Stabil (K2 Compiler standardmäßig aktiv) |
| **Java (JDK)** | `25`     | LTS (Long-Term Support) |
| **Gradle** | `9.2.1`  | Erforderlich für JDK 25 Support |
| **Android Plugin (AGP)** | `8.8.0+` | Empfohlen für Gradle 9.x Kompatibilität |

---

### 2. Gradle Konfiguration (`build.gradle.kts`)

Für ein **Kotlin Multiplatform (KMP)** Projekt ist die Java Toolchain-Konfiguration entscheidend, um sicherzustellen, dass der Kotlin-Compiler und die JVM-Targets Java 25 korrekt ansprechen.

```kotlin
plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.android.library") version "8.8.0" // Falls Android Target genutzt wird
}

kotlin {
    // Globale Toolchain-Definition für alle JVM/Android Targets
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    jvm {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
            }
        }
    }
    
    // Weitere Targets (Beispiel iOS)
    iosArm64()
    iosSimulatorArm64()
}
```

---

### 3. Gradle Wrapper Update

Damit das Projekt Java 25 erkennt, muss der Wrapper auf dem neuesten Stand sein:

**Terminal-Befehl:**
```bash
./gradlew wrapper --gradle-version 9.2.1 --distribution-type all
```

---

### 4. Wichtige Kompatibilitätshinweise

* **IDE-Version:** IntelliJ IDEA 2025.3 (oder neuer) wird für die volle Unterstützung von JDK 25 und dem Kotlin 2.3.0 Plugin empfohlen.
* **K2 Compiler:** Kotlin 2.3.0 nutzt den K2-Compiler.
* **Bytecode:** Java 25 Bytecode wird nur generiert, wenn das `jvmTarget` explizit auf `25` gesetzt ist.

---

### 5. Bekannte Features in diesem Setup

* **Java 25 Features:** Unterstützung für die finalen Versionen von *Scoped Values* und *Structured Concurrency*.
* **Kotlin 2.3.0 Features:** Nutzung von `explicit backing fields` und dem verbesserten `unused return value` Checker.
