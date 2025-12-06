package at.mocode.entries.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@SpringBootApplication
class EntriesServiceApplication

fun main(args: Array<String>) {
    runApplication<EntriesServiceApplication>(*args)
}

@RestController
class EntriesController {
    @GetMapping("/")
    fun health(): String = "Entries Service is running"

    @PostMapping("/entries/conflict-demo")
    fun conflictDemo(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict detected (Demo)")
    }
}
