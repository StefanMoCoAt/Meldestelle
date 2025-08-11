package at.mocode.temp.pingservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {

    @GetMapping("/ping")
    fun ping(): Map<String, String> {
        return mapOf("status" to "pong")
    }
}
