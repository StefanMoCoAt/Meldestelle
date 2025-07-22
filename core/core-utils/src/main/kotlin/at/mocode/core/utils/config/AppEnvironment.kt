package at.mocode.core.utils.config

/**
 * Aufzählung der verschiedenen Anwendungsumgebungen.
 */
enum class AppEnvironment {
    DEVELOPMENT, // Lokale Entwicklungsumgebung
    TEST,        // Testumgebung (CI/CD, Integrationstests)
    STAGING,     // Vorabproduktionsumgebung
    PRODUCTION;  // Produktionsumgebung

    companion object {
        /**
         * Ermittelt die aktuelle Umgebung basierend auf der APP_ENV Umgebungsvariable.
         *
         * @return Die aktuelle Umgebung (Standardmäßig DEVELOPMENT wenn nicht definiert)
         */
        fun current(): AppEnvironment {
            val envName = System.getenv("APP_ENV")?.uppercase() ?: "DEVELOPMENT"
            return try {
                valueOf(envName)
            } catch (_: IllegalArgumentException) {
                println("Warnung: Unbekannte Umgebung '$envName', verwende DEVELOPMENT")
                DEVELOPMENT
            }
        }

        /**
         * Prüft, ob die aktuelle Umgebung die Entwicklungsumgebung ist.
         */
        fun isDevelopment() = current() == DEVELOPMENT

        /**
         * Prüft, ob die aktuelle Umgebung die Testumgebung ist.
         */
        fun isTest() = current() == TEST

        /**
         * Prüft, ob die aktuelle Umgebung die Staging-Umgebung ist.
         */
        fun isStaging() = current() == STAGING

        /**
         * Prüft, ob die aktuelle Umgebung die Produktionsumgebung ist.
         */
        fun isProduction() = current() == PRODUCTION
    }
}
