# Architecture Tests

Dieses Modul ist der zentrale "Architektur-Wächter" des gesamten Projekts. Es verwendet [ArchUnit](https://www.archunit.org/) um sicherzustellen, dass die definierten Architektur-Regeln im Code nicht verletzt werden.

## Zweck

Der Hauptzweck dieses Moduls ist es, die strikte Trennung zwischen den verschiedenen Teilen des Systems technisch zu erzwingen und so die langfristige Wartbarkeit und Skalierbarkeit zu sichern. Dazu gehören Regeln wie:

*   **Keine Kreuz-Abhängigkeiten zwischen Backend-Services:** Ein Service darf nicht direkt auf den Code eines anderen Service zugreifen.
*   **Keine Kreuz-Abhängigkeiten zwischen Frontend-Features:** Ein Feature darf nicht direkt auf den Code eines anderen Features zugreifen.
*   **Einhaltung von Schichten-Architekturen:** z.B. Controller -> Service -> Repository.

Diese Tests laufen als Teil der CI-Pipeline. Ein Build schlägt fehl, wenn eine Regel verletzt wird.

## Wie es funktioniert

1.  **Abhängigkeiten:** Die `build.gradle.kts` dieses Moduls deklariert explizite `implementation`-Abhängigkeiten zu **allen** baubaren Modulen des Projekts, deren Architektur verifiziert werden soll.
2.  **Test-Klassen:** Die eigentlichen Regeln sind als JUnit 5-Tests in den `src/test/kotlin`-Verzeichnissen implementiert (z.B. `BackendArchitectureTest.kt`).
3.  **Klassen-Import:** Die Tests verwenden die `@AnalyzeClasses`-Annotation von ArchUnit, um die Klassen aus den deklarierten Abhängigkeiten zu scannen.
4.  **Regel-Definition:** Innerhalb der Tests werden ArchUnit-Regeln definiert (z.B. mit `slices().should().notDependOnEachOther()`), die auf die importierten Klassen angewendet werden.

## Wie man neue Regeln hinzufügt

1.  **Abhängigkeit sicherstellen:** Stellen Sie in der `build.gradle.kts` dieses Moduls sicher, dass eine Abhängigkeit zum neuen Modul, das Sie testen wollen, existiert.
2.  **Test-Klasse erweitern:** Fügen Sie eine neue `@ArchTest`-Methode zu einer der bestehenden Test-Klassen (`BackendArchitectureTest.kt`, `FrontendArchitectureTest.kt`) hinzu oder erstellen Sie eine neue Test-Klasse für eine neue Kategorie von Regeln.
3.  **Regel definieren:** Schreiben Sie Ihre Regel unter Verwendung der ArchUnit-API.
