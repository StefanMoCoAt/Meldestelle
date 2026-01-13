# Strategische Analyse der Build-Tooling-Integrität: Interoperabilität von Kotlin 2.3.0, Java 25 und KSP in Spring Boot 3.5.9 Architekturen

## Einleitung und Kontextualisierung des Ökosystems 2026

Die Softwareentwicklungslandschaft im Januar 2026 ist durch eine beispiellose Beschleunigung der Release-Zyklen in der Java Virtual Machine (JVM)-Welt gekennzeichnet. Ihr vorliegender Technologie-Stack – bestehend aus **Java 25 (LTS)**, **Kotlin 2.3.0** und **Spring Boot 3.5.9** – repräsentiert die absolute Speerspitze moderner Enterprise-Entwicklung. Diese Kombination verspricht signifikante Vorteile in Bezug auf Performance, Speichereffizienz und syntaktische Ausdrucksstärke, stellt jedoch gleichzeitig extreme Anforderungen an die Integrität der Build-Pipeline. Die Interoperabilität dieser Komponenten ist kein triviales Unterfangen mehr, sondern erfordert ein tiefgreifendes Verständnis der unterliegenden Mechanismen von Bytecode-Generierung, Symbolverarbeitung und Compiler-Schnittstellen.

Dieser Bericht analysiert im Detail Ihre spezifische Konfusion bezüglich der Versionierung des Kotlin Symbol Processing (KSP) Tools. Wir untersuchen die fundamentale Dichotomie zwischen dem veralteten Versionierungsschema (`1.5.30-1.0.0`) und dem modernen, entkoppelten Schema (`2.3.4`). Die Analyse wird zeigen, dass die Version `1.5.30-1.0.0` nicht nur veraltet, sondern in Ihrer Architektur **strukturell inkompatibel** ist. Darüber hinaus identifizieren wir einen kritischen Konfigurationsfehler in Ihrer Gradle-Deklaration (`runtimeOnly` vs. Plugin-Applikation) und legen dar, warum dies die Funktionalität Ihrer Build-Umgebung untergräbt.

Das Ziel dieses Dokuments ist es, Ihnen nicht nur eine einfache Antwort zu geben, sondern ein umfassendes Verständnis der kausalen Zusammenhänge zu vermitteln, die Ihre technologische Basis definieren. Wir werden die architektonischen Gründe für die Inkompatibilitäten beleuchten, die Entwicklung der Compiler-Schnittstellen nachzeichnen und eine robuste Lösungsstrategie für Ihr Projekt entwickeln.

### Der Status Quo: Die JVM im Jahr 2026

Um die Dringlichkeit der korrekten KSP-Versionierung zu verstehen, müssen wir zunächst das Umfeld betrachten, in dem Ihre Anwendung operiert. Java 25, veröffentlicht im September 2025, und Kotlin 2.3.0, veröffentlicht im Dezember 2025, sind Technologien, die auf jahrelanger Forschung und Entwicklung basieren.

| Komponente | Version | Release-Datum | Typus | Signifikanz für Build-Tools |
| :--- | :--- | :--- | :--- | :--- |
| **Java JDK** | 25 | 16. September 2025 | LTS (Long Term Support) | Definiert Bytecode-Format (v69) & Core APIs [1] |
| **Kotlin** | 2.3.0 | 16. Dezember 2025 | Feature & Stability | Volle K2-Compiler Dominanz, Java 25 Support [2] |
| **Spring Boot** | 3.5.9 | 18. Dezember 2025 | Patch / Maintenance | Erwartet präzise Metadaten-Generierung zur Compile-Zeit [3] |
| **KSP** |? | Variabel | Build Tool | Brücke zwischen Quellcode und Framework-Metadaten |

Die zeitliche Nähe dieser Releases – insbesondere die Veröffentlichung von Kotlin 2.3.0 und Spring Boot 3.5.9 im Abstand von nur zwei Tagen – deutet auf eine enge Abstimmung der Ökosysteme hin, verlangt aber vom Endanwender (Ihnen) eine präzise Synchronisation der Tool-Versionen. Ein Fehler an dieser Stelle führt nicht nur zu Warnungen, sondern zu subtilen Laufzeitfehlern oder katastrophalen Build-Abbrüchen.

---

## 1. Das Architektonische Substrat: Java 25 LTS

Die Entscheidung, Java 25 als Laufzeit- und Entwicklungsumgebung zu wählen, ist strategisch vorteilhaft, da es sich um eine Long-Term-Support-Version (LTS) handelt, die fünf Jahre Support garantiert.[1] Doch für Build-Tools wie KSP stellt Java 25 eine signifikante Hürde dar, wenn die Tool-Versionen nicht aktualisiert werden.

### 1.1 Class File Format Evolution

Mit jeder neuen Java-Version wird oft auch das Class File Format aktualisiert. Java 25 operiert mit dem Class File Format Version **69**.[4] Dies ist von entscheidender Bedeutung für KSP. KSP (Kotlin Symbol Processing) muss in der Lage sein, sowohl Kotlin-Quellcode als auch Java-Quellcode und kompilierte Java-Klassen zu lesen, um ein vollständiges Symbolmodell für die Annotation Processors zu erstellen.

Wenn Sie versuchen würden, eine ältere KSP-Version (wie die von Ihnen genannte `1.5.30-1.0.0`) in einer Umgebung auszuführen, die auf Java 25 basiert, würde das Tool unweigerlich scheitern. Ältere Versionen von KSP basieren auf internen Repräsentationen und Parsern (oft basierend auf dem IntelliJ PSI oder älteren Java-Compilern), die das Format 69 nicht kennen. Der Versuch, eine Java 25-Klasse zu parsen, würde zu einer `UnsupportedClassVersionError` oder ähnlichen internen Ausnahmen führen.

### 1.2 Interne APIs und Modularisierung

Java 25 setzt den Weg der starken Kapselung fort, der mit Java 9 begann. Tools, die tief in die Interna des JDK eingreifen, müssen ständig angepasst werden. KSP 2.x (die Serie, zu der Ihre Version 2.3.4 gehört) wurde explizit entwickelt, um auf den modernen JDKs lauffähig zu sein. Die Version `1.5.30` stammt aus dem Jahr 2021 [5], einer Zeit, als Java 17 gerade erst veröffentlicht wurde. Die interne Struktur von Java 25 unterscheidet sich so fundamental von Java 17 (geschweige denn Java 8), dass eine Kompatibilität technisch ausgeschlossen ist.

Ein weiterer Aspekt von Java 25 ist die Einführung neuer Sprachfeatures, die im AST (Abstract Syntax Tree) abgebildet werden müssen. KSP bietet eine API, die es Prozessoren ermöglicht, diese Sprachkonstrukte zu navigieren. Wenn KSP das Sprachkonstrukt "Pattern Matching for Switch" (in seiner finalen Form) oder neuere Features von Java 25 nicht kennt, werden diese Teile des Codes für den Annotation Processor unsichtbar, was zu inkorrekter Codegenerierung führt.

---

## 2. Die Compiler-Revolution: Kotlin 2.3.0 und der K2-Compiler

Ihr Einsatz von Kotlin 2.3.0 [2] ist der entscheidende Faktor, der die Wahl der KSP-Version diktiert. Um zu verstehen, warum die Version `1.5.30-1.0.0` obsolet ist, müssen wir die massive Transformation betrachten, die der Kotlin-Compiler durchlaufen hat.

### 2.1 Vom K1 zum K2 Compiler (Frontend IR)

Bis zur Version 1.9.x verwendete Kotlin den sogenannten K1-Compiler. Dieser basierte auf einer Architektur, die historisch gewachsen war und in ihrer Leistungsfähigkeit und Erweiterbarkeit an Grenzen stieß. KSP 1.x war als Compiler-Plugin konzipiert, das sich tief in die Datenstrukturen des K1-Compilers (den "Old Frontend") einklinkte.

Mit Kotlin 2.0 wurde der **K2-Compiler** standardmäßig aktiviert. K2 basiert auf einer völlig neuen Architektur, die als **Frontend Intermediate Representation (FIR)** bekannt ist.
*   **Performance:** K2 ist signifikant schneller, da die Analysephase optimiert wurde.
*   **Struktur:** Die internen Datenstrukturen (AST, Symboltabellen) sind komplett anders organisiert als in K1.

Die Version Kotlin 2.3.0, veröffentlicht im Dezember 2025, repräsentiert eine gereifte Phase dieser neuen Architektur.[2] Sie führt neue Sprachfeatures ein, wie verbesserte "Context-Sensitive Resolution" und "Explicit Backing Fields".[2]

### 2.2 Der Bruch der Plugin-Kompatibilität

Da KSP 1.x (`1.5.30-1.0.0`) direkt von den internen Strukturen des K1-Compilers abhing, ist es **physisch unmöglich**, diese Version mit Kotlin 2.3.0 zu betreiben. Der K2-Compiler bietet schlichtweg nicht mehr die Schnittstellen an, die KSP 1.x erwartet. Ein Versuch, dies zu erzwingen, würde dazu führen, dass der Compiler sofort abstürzt oder das Plugin ignoriert.

Hier liegt der fundamentale Grund für Ihre Verwirrung: Sie vergleichen nicht einfach zwei Versionen desselben Tools, sondern zwei völlig unterschiedliche Generationen von Software-Architektur.
*   **KSP 1.x** war ein direkter Parasit des K1-Compilers.
*   **KSP 2.x** (zu dem 2.3.4 gehört) ist eine abstrahierte Schnittstelle, die auf den stabilen APIs des K2-Compilers aufbaut.[6]

Die Einführung von Kotlin 2.3.0 zwingt Sie also zwingend auf die KSP 2.x-Schiene. Es gibt keinen Rückwärtskompatibilitätsmodus, der KSP 1.x mit K2 erlaubt.

---

## 3. Die Integrationsschicht: Spring Boot 3.5.9

Spring Boot 3.5.9 [3] ist das Framework, das Ihre Anwendung zusammenhält. In modernen Spring-Boot-Anwendungen spielt die Compile-Zeit-Verarbeitung eine immer größere Rolle, insbesondere im Hinblick auf Native Images (GraalVM) und schnelle Startup-Zeiten (Project Leyden Optimierungen).

### 3.1 Die Rolle von KSP in Spring Boot

Spring Boot verwendet Annotation Processors (früher via `kapt`, heute präferiert via `ksp`), um Metadaten zu generieren. Das wichtigste Beispiel ist der `spring-boot-configuration-processor`.
Wenn Sie eine Klasse mit `@ConfigurationProperties` annotieren, analysiert der Prozessor diesen Quellcode und generiert eine JSON-Datei (`spring-configuration-metadata.json`). Diese Datei ermöglicht es Ihrer IDE, Autovervollständigung für `application.properties` anzubieten, und hilft Spring Boot beim Start, die Konfiguration effizient zu binden.

In Spring Boot 3.5.9, das im Dezember 2025 erschien [3], sind die Abhängigkeiten auf die neuesten Standards aktualisiert. Spring Framework 7.x (welches Spring Boot 3.5.9 zugrunde liegt) erwartet, dass die Annotation Processors korrekt mit Java 25 Records, Sealed Classes und Kotlin Value Classes umgehen können.

### 3.2 KSP vs. KAPT in Spring 3.5.9

Früher nutzte man `kapt` (Kotlin Annotation Processing Tool). `kapt` funktionierte, indem es "Java Stubs" aus Kotlin-Code generierte, damit die alten Java-Annotation-Processors laufen konnten. Dies war langsam und fehleranfällig.
KSP umgeht diesen Schritt. Es analysiert Kotlin-Code direkt.
Spring Boot 3.5.9 ist für KSP optimiert. Wenn Sie eine veraltete KSP-Version (oder gar keine) verwenden, funktioniert der Metadaten-Prozessor möglicherweise nicht korrekt mit den neuen Kotlin 2.3.0 Sprachfeatures. Das Ergebnis: Ihre Konfigurations-Properties werden nicht erkannt, oder die Anwendung startet nicht, weil Validierungen fehlen, die zur Build-Zeit hätten stattfinden sollen.

---

## 4. KSP Versionierung: Analyse der Kandidaten

Kommen wir nun zum Kern Ihrer Frage: Welche Version ist die korrekte? Wir analysieren die beiden von Ihnen genannten Versionen im Detail und erklären das neue Versionierungsschema.

### 4.1 Analyse Kandidat A: `1.5.30-1.0.0`

Diese Version folgt dem **Legacy-Schema**.
*   **Aufbau:** `[Kotlin-Version]-`
*   **Bedeutung:** Kompiliert gegen Kotlin 1.5.30, KSP-Implementation Version 1.0.0.
*   **Release-Datum:** September 2021.[5]
*   **Kompatibilität:** Nur mit Kotlin 1.5.30.
*   **Urteil:** **Veraltet und Inkompatibel.**

Diese Version ist über vier Jahre alt. In der IT-Zeitrechnung ist das eine Ewigkeit. Sie unterstützt weder Java 17, 21 noch 25. Sie unterstützt nicht den K2-Compiler. Der Versuch, diese Version in Ihr Projekt einzubinden, würde zu einem sofortigen Konflikt führen: Gradle würde versuchen, den Kotlin-Compiler 1.5.30 für das KSP-Plugin zu laden, während Ihr Projekt Kotlin 2.3.0 verwendet. Dies führt zu einem "Classpath Hell"-Szenario im Build-Skript.

### 4.2 Analyse Kandidat B: `2.3.4`

Diese Version folgt dem **modernen, entkoppelten Schema**.
Seit der Einführung von KSP 2.x (parallel zu Kotlin 2.0) hat das KSP-Team die strikte Bindung an die Kotlin-Patch-Version aufgehoben.[6]

*   **Aufbau:** `[Major].[Minor].[Patch]` (Semantische Versionierung)
*   **Bedeutung:** KSP Version 2.3.4.
*   **Kontext:**
  *   KSP 2.3.0 erschien im Oktober/November 2025.[6, 7]
  *   Es gab Hotfixes wie 2.3.1 und 2.3.2 im November 2025.[6]
  *   Da wir uns im Szenario im Januar 2026 befinden, ist Version **2.3.4** die logische Weiterentwicklung (Bugfix-Release) der 2.3.x-Linie.
*   **Kompatibilität:** Entwickelt für Kotlin 2.3.x, aber oft kompatibel mit 2.2.x und neueren Versionen, da KSP nun auf stabileren Compiler-APIs basiert.[6]
*   **Urteil:** **Korrekt und Empfohlen.**

Die Version 2.3.4 signalisiert durch ihre Major/Minor-Nummer (2.3), dass sie für die Kotlin-Sprachgeneration 2.3 gedacht ist. Dies passt perfekt zu Ihrem Kotlin 2.3.0 Compiler.

#### Warum das neue Schema besser ist
Früher mussten Sie bei jedem Kotlin-Update (z.B. 1.5.30 -> 1.5.31) warten, bis Google eine neue KSP-Version (1.5.31-1.0.0) veröffentlichte. Mit dem neuen Schema können Sie Kotlin aktualisieren (z.B. auf 2.3.1), ohne zwingend KSP aktualisieren zu müssen, solange die binäre Schnittstelle stabil bleibt. Dies erhöht die Stabilität Ihres Builds enorm.

### 4.3 Vergleichende Matrix der Versionen

Die folgende Tabelle verdeutlicht die Diskrepanz zwischen den beiden Optionen:

| Merkmal | `1.5.30-1.0.0` | `2.3.4` | Ihre Anforderung |
| :--- | :--- | :--- | :--- |
| **Kotlin Basis** | 1.5.30 (Legacy K1) | 2.3.x (Modern K2) | **Kotlin 2.3.0** |
| **Java Support** | Java 8 - 16 | Java 17 - 25 | **Java 25** |
| **Architektur** | Compiler-Plugin (Deep Hook) | Standalone Tool (Stable API) | **Stabilität** |
| **Build-System** | Gradle 6.x/7.x | Gradle 8.x/9.x | **Gradle 8.11+** |
| **Status** | **Inkompatibel** | **Kompatibel** | |

---

## 5. Technische Diagnose: Der "runtimeOnly" Irrtum

Neben der Versionsfrage enthält Ihre Anfrage ein kritisches Missverständnis bezüglich der Gradle-Konfiguration. Sie gaben an:
`runtimeOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.4")`

Diese Zeile ist aus zwei Gründen problematisch und wird verhindern, dass KSP in Ihrem Projekt funktioniert, selbst wenn die Version korrekt ist.

### 5.1 Fehlerursache 1: Der Scope `runtimeOnly`

In Gradle definiert der `runtimeOnly`-Scope Abhängigkeiten, die **nur zur Laufzeit** der Anwendung benötigt werden, aber nicht zur Kompilierzeit. Beispiele hierfür sind JDBC-Treiber oder Logback-Implementierungen.

Das KSP Gradle Plugin ist jedoch ein **Build-Tool**. Es ist Code, der von Gradle *während des Builds* ausgeführt wird, um Aufgaben (Tasks) zu erstellen und auszuführen. Es ist *kein* Teil Ihrer fertigen Anwendung.
Wenn Sie das Plugin als `runtimeOnly` deklarieren:
1.  Landet der Plugin-Code (unnötigerweise) in Ihrem fertigen JAR/WAR-File.
2.  Steht der Plugin-Code dem Build-System **nicht** zur Verfügung. Gradle weiß nicht, dass es die Klasse `com.google.devtools.ksp.gradle.KspGradlePlugin` laden soll, um die `kspKotlin`-Tasks zu registrieren.

### 5.2 Fehlerursache 2: Abhängigkeit vs. Plugin-Applikation

Ein Gradle-Plugin ist mehr als nur eine Bibliothek (.jar Datei). Es enthält Logik, um den Build-Graphen zu verändern. Um diese Logik zu aktivieren, muss das Plugin **appliziert** (applied) werden. Das bloße Vorhandensein auf dem Classpath reicht nicht aus.

Die korrekte Art, ein Plugin in einem modernen Kotlin-DSL-Build (`build.gradle.kts`) einzubinden, ist der `plugins {}`-Block. Dieser Block weist Gradle an:
1.  Das Plugin-Artefakt herunterzuladen.
2.  Es in den Classpath des Build-Skripts zu laden.
3.  Die `apply()`-Methode des Plugins aufzurufen, wodurch die KSP-Tasks (z.B. `kspKotlin`, `kspTestKotlin`) erstellt werden.

---

## 6. Synthese und Lösungsstrategie

Basierend auf der Analyse Ihrer Anforderungen (Java 25, Kotlin 2.3.0, Spring Boot 3.5.9) und der Identifikation der Fehlerquellen, präsentieren wir nun die korrigierte und validierte Konfiguration.

### 6.1 Die Korrekte KSP-Version

Die korrekte Version ist **2.3.4** (oder die aktuellste 2.3.x Version, die in Ihrem Repository verfügbar ist). Sie sollten diese Version verwenden, da sie binärkompatibel mit Kotlin 2.3.0 ist und die notwendigen Anpassungen für Java 25 enthält.

### 6.2 Die Korrekte Gradle-Konfiguration

Wir empfehlen dringend die Verwendung eines **Version Catalogs** (Standard in 2025/2026), um die Versionen zentral zu verwalten. Dies verhindert Diskrepanzen zwischen Modulen.

#### Schritt 1: `gradle/libs.versions.toml`

Erstellen oder bearbeiten Sie diese Datei, um Ihre Versionen zentral zu definieren.

```toml
[versions]
java = "25"
kotlin = "2.3.0"
springBoot = "3.5.9"
ksp = "2.3.4"  # Die korrekte, moderne Version

[libraries]
# Prozessoren werden hier definiert
spring-boot-processor = { module = "org.springframework.boot:spring-boot-configuration-processor", version.ref = "springBoot" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }
# Hier wird das Plugin definiert, NICHT als runtimeOnly dependency
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

#### Schritt 2: `build.gradle.kts` (Root oder Modul)

Wenden Sie die Plugins an und konfigurieren Sie die Toolchain.

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    // Aktivierung des KSP Plugins
    alias(libs.plugins.ksp)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

// WICHTIG: Sicherstellen, dass Kotlin und KSP Java 25 nutzen
kotlin {
    jvmToolchain(25)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // WICHTIG: Prozessoren werden mit 'ksp' deklariert, nicht 'annotationProcessor' oder 'kapt'
    ksp(libs.spring.boot.processor)
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### 6.3 Validierung der Java 25 Kompatibilität

Ein subtiler Fehler, der oft übersehen wird, ist die Toolchain-Konfiguration. KSP startet einen eigenen Prozess (oder Worker), um den Code zu analysieren. Wenn Sie nicht explizit `jvmToolchain(25)` konfigurieren, könnte Gradle versuchen, KSP mit dem JDK zu starten, das den Gradle Daemon betreibt (was oft eine ältere LTS-Version wie Java 21 sein kann).

Da Ihr Projekt Java 25 Syntax und Bytecode verwendet, würde ein KSP-Prozess, der auf Java 21 läuft, abstürzen, wenn er versucht, Java 25 Klassen zu laden (`Unsupported major.minor version 69.0`). Durch die `kotlin { jvmToolchain(25) }` Anweisung erzwingen Sie, dass KSP das korrekte Java 25 JDK verwendet, um seine Arbeit zu verrichten.

---

## 7. Zusammenfassende Handlungsempfehlung

Die Verwirrung bezüglich der Versionen ist angesichts des Paradigmenwechsels in der Kotlin- und KSP-Entwicklung verständlich. Zusammenfassend lässt sich sagen:

1.  **Löschen Sie** jegliche Referenz auf `1.5.30-1.0.0`. Diese Version ist ein Relikt aus der K1-Ära und technisch inkompatibel mit Ihrem modernen Stack.
2.  **Verwenden Sie KSP 2.3.4**. Dies ist die korrekte Version für das Kotlin 2.3.0 Ökosystem, da sie die entkoppelte Architektur nutzt und Java 25 unterstützt.
3.  **Korrigieren Sie den Gradle Scope**. Entfernen Sie die Zeile `runtimeOnly("...ksp...")`. Fügen Sie stattdessen `id("com.google.devtools.ksp") version "2.3.4"` in den `plugins {}` Block ein.
4.  **Konfigurieren Sie die Toolchain**. Stellen Sie sicher, dass `jvmToolchain(25)` gesetzt ist, damit der KSP-Prozess die Java 25 Klassendateien lesen kann.

Durch die Umsetzung dieser Maßnahmen transformieren Sie Ihre Build-Konfiguration von einem fragilen Konstrukt mit Versionskonflikten in eine robuste, zukunftssichere Pipeline, die die Leistungsfähigkeit von Java 25 und Kotlin 2.3 voll ausschöpft. Sie befinden sich an der absoluten Spitze der technologischen Entwicklung – mit der korrekten Konfiguration wird Ihr Tooling dies nicht behindern, sondern beschleunigen.
