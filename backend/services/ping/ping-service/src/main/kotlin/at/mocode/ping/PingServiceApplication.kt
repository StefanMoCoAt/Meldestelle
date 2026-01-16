package at.mocode.ping

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy
// Scannt das eigene Service-Package UND das Security-Infrastruktur-Package
@ComponentScan(basePackages = ["at.mocode.ping", "at.mocode.infrastructure.security"])
class PingServiceApplication

fun main(args: Array<String>) {
    runApplication<PingServiceApplication>(*args)
}
