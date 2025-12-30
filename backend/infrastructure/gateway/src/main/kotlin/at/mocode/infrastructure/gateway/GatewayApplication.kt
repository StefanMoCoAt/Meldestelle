package at.mocode.infrastructure.gateway

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment

@SpringBootApplication
class GatewayApplication

fun main(args: Array<String>) {
    val context = runApplication<GatewayApplication>(*args)
    val logger = LoggerFactory.getLogger(GatewayApplication::class.java)
    val env = context.getBean(Environment::class.java)
    val port = env.getProperty("server.port") ?: "8081"

    logger.info("""
        ----------------------------------------------------------
        Application 'Gateway' is running!
        Port:       $port
        Profiles:   ${env.activeProfiles.joinToString(", ").ifEmpty { "default" }}
        ----------------------------------------------------------
    """.trimIndent())
}
