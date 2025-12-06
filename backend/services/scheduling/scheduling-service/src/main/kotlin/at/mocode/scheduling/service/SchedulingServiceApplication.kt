package at.mocode.scheduling.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SchedulingServiceApplication

fun main(args: Array<String>) {
    runApplication<SchedulingServiceApplication>(*args)
}

@RestController
class SchedulingController {
    @GetMapping("/")
    fun health(): String = "Scheduling Service is running"
}
