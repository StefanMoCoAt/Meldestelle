package at.mocode.infrastructure.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes {
            route(id = "ping-service") {
                path("/api/ping/**")
                filters {
                    stripPrefix(1)
                    circuitBreaker {
                        it.name = "pingServiceCB"
                        it.fallbackUri = java.net.URI.create("forward:/fallback/ping")
                    }
                }
                uri("http://ping-service:8082")
            }
        }
    }
}
