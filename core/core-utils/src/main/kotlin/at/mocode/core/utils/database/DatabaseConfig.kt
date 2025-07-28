package at.mocode.core.utils.database

import java.util.Properties

/**
 * Konfiguration für die Datenbankverbindung.
 * Diese Klasse ist ein reiner Datenhalter (Value Object). Die Logik zum Laden
 * der Werte ist in der companion object Factory-Methode gekapselt.
 */
data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String = "org.postgresql.Driver",
    val maxPoolSize: Int = 10,
    val minPoolSize: Int = 5,
    val autoMigrate: Boolean = true // Flag to enable/disable Flyway migrations
) {
    companion object {
        /**
         * Erstellt eine Datenbank-Konfiguration aus Umgebungsvariablen und Properties.
         * Die Priorität ist: Umgebungsvariablen > Properties > Standardwerte.
         */
        fun fromProperties(props: Properties): DatabaseConfig {
            val host = System.getenv("DB_HOST") ?: props.getProperty("database.host", "localhost")
            val port = System.getenv("DB_PORT") ?: props.getProperty("database.port", "5432")
            val database = System.getenv("DB_NAME") ?: props.getProperty("database.name", "meldestelle_db")
            val username = System.getenv("DB_USER") ?: props.getProperty("database.username", "meldestelle_user")
            val password =
                System.getenv("DB_PASSWORD") ?: props.getProperty("database.password", "secure_password_change_me")

            val maxPoolSize = System.getenv("DB_MAX_POOL_SIZE")?.toIntOrNull()
                ?: props.getProperty("database.maxPoolSize")?.toIntOrNull()
                ?: 10

            val minPoolSize = System.getenv("DB_MIN_POOL_SIZE")?.toIntOrNull()
                ?: props.getProperty("database.minPoolSize")?.toIntOrNull()
                ?: 5

            val autoMigrate = System.getenv("DB_AUTO_MIGRATE")?.toBoolean()
                ?: props.getProperty("database.autoMigrate")?.toBoolean()
                ?: true

            return DatabaseConfig(
                jdbcUrl = "jdbc:postgresql://$host:$port/$database",
                username = username,
                password = password,
                driverClassName = "org.postgresql.Driver",
                maxPoolSize = maxPoolSize,
                minPoolSize = minPoolSize,
                autoMigrate = autoMigrate
            )
        }
    }
}
