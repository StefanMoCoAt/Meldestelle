package at.mocode.temp.pingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy
class PingServiceApplication

fun main(args: Array<String>) {
    runApplication<PingServiceApplication>(*args)
}
