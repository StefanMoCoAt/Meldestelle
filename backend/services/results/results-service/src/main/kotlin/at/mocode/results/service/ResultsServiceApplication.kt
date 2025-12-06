package at.mocode.results.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class ResultsServiceApplication

fun main(args: Array<String>) {
    runApplication<ResultsServiceApplication>(*args)
}

@RestController
class ResultsController {
    @GetMapping("/")
    fun health(): String = "Results Service is running"
}
