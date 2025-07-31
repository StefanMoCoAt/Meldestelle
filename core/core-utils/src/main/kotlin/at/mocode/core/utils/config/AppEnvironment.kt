package at.mocode.core.utils.config

import org.slf4j.LoggerFactory

enum class AppEnvironment {
    DEVELOPMENT,
    TEST,
    STAGING,
    PRODUCTION;

    fun isProduction() = this == PRODUCTION

    companion object {
        private val logger = LoggerFactory.getLogger(AppEnvironment::class.java)

        fun current(): AppEnvironment {
            val envName = System.getenv("APP_ENV")?.uppercase() ?: "DEVELOPMENT"
            return try {
                valueOf(envName)
            } catch (_: IllegalArgumentException) {
                logger.warn("Unknown environment '{}', falling back to DEVELOPMENT.", envName)
                DEVELOPMENT
            }
        }
    }
}
