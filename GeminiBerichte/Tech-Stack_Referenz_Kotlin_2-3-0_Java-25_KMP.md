
# Tech-Stack Referenz: Kotlin 2.3.0 & Java 25 (KMP)

### 1. Kern-Spezifikationen

| Komponente | Version | Status |
| --- | --- | --- |
| **Kotlin** | `2.3.0` | Stabil (K2 Compiler standardmäßig aktiv) |
| **Java (JDK)** | `25` | LTS (Long-Term Support) |
| **Gradle** | `9.2+` | Erforderlich für JDK 25 Support |
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
./gradlew wrapper --gradle-version 9.2 --distribution-type all

```

---

### 4. Wichtige Kompatibilitätshinweise für das Plugin

* **IDE-Version:** Stelle sicher, dass **IntelliJ IDEA 2025.3** (oder neuer) installiert ist, da erst diese Version die volle Unterstützung für JDK 25 Sprachfeatures und das Kotlin 2.3.0 Plugin bietet.
* **K2 Compiler:** Kotlin 2.3.0 nutzt den K2-Compiler. Falls das Google AI Pro Plugin Code-Analysen durchführt, sollte es auf dem K2-Modus basieren.
* **Bytecode:** Java 25 Bytecode wird nur generiert, wenn das `jvmTarget` explizit auf `25` gesetzt ist. Andernfalls verbleibt Kotlin standardmäßig bei einer niedrigeren Version (meist 1.8 oder 11), was die neuen JDK-Features einschränken könnte.

---

### 5. Bekannte Features in diesem Setup

* **Java 25 Features:** Unterstützung für die finalen Versionen von *Scoped Values* und *Structured Concurrency*.
* **Kotlin 2.3.0 Features:** Nutzung von `explicit backing fields` und dem verbesserten `unused return value` Checker.
