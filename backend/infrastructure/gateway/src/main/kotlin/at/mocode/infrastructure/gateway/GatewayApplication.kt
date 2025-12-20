package at.mocode.infrastructure.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@org.springframework.boot.context.properties.ConfigurationPropertiesScan(basePackages = ["at.mocode.infrastructure.gateway"])
class GatewayApplication

fun main(args: Array<String>) {
  // Der Web-Anwendungstyp wird über application.yml gesteuert (spring.main.web-application-type=reactive)
  runApplication<GatewayApplication>(*args)
}
