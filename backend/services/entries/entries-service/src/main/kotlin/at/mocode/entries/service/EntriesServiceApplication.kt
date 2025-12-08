package at.mocode.entries.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

fun main(args: Array<String>) {
    runApplication<EntriesServiceApplication>(*args)
}

@SpringBootApplication
@EnableAspectJAutoProxy
class EntriesServiceApplication {

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
