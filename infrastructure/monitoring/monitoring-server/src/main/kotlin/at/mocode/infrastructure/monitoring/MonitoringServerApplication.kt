package at.mocode.infrastructure.monitoring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import zipkin2.server.internal.EnableZipkinServer

@EnableZipkinServer
@SpringBootApplication
class MonitoringServerApplication

fun main(args: Array<String>) {
    runApplication<MonitoringServerApplication>(*args)
}
