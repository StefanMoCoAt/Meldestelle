package at.mocode.infrastructure.monitoring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MonitoringServerApplication

fun main(args: Array<String>) {
    runApplication<MonitoringServerApplication>(*args)
}
