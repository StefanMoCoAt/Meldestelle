package at.mocode.shared.database

/**
 * Konfiguration für die Datenbankverbindung.
 * Parameter werden aus Umgebungsvariablen gelesen oder Standardwerte verwendet.
 */
data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String = "org.postgresql.Driver",
    val maxPoolSize: Int = 10
) {
    companion object {
        /**
         * Erstellt eine Datenbank-Konfiguration aus Umgebungsvariablen.
         * Wenn keine Umgebungsvariablen gefunden werden, werden Standardwerte für die Entwicklung verwendet.
         */
        fun fromEnv(): DatabaseConfig {
            val host = System.getenv("DB_HOST") ?: "localhost"
            val port = System.getenv("DB_PORT") ?: "5432"
            val database = System.getenv("DB_NAME") ?: "meldestelle_db"
            val username = System.getenv("DB_USER") ?: "meldestelle_user"
            val password = System.getenv("DB_PASSWORD") ?: "secure_password_change_me"

            return DatabaseConfig(
                jdbcUrl = "jdbc:postgresql://$host:$port/$database",
                username = username,
                password = password
            )
        }
    }
}
