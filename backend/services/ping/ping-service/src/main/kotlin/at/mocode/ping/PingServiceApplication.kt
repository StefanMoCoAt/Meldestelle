package at.mocode.ping

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.web.reactive.config.CorsRegistry

@SpringBootApplication
// Scannt explizit alle Sub-Packages (infrastructure, application, domain)
@EnableAspectJAutoProxy
class PingServiceApplication {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOriginPatterns("http://localhost:*")
                    .allowedOrigins("http://localhost:8080",
                        "http://localhost:8083",
                        "http://localhost:4000"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600)
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<PingServiceApplication>(*args)
}
