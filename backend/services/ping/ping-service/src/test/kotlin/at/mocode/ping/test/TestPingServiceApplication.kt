package at.mocode.ping.test

import at.mocode.ping.infrastructure.web.PingController
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import

/**
 * Eine spezielle Application-Klasse für Tests.
 * Sie liegt in einem separaten Package, damit sie nicht automatisch von @WebMvcTest gefunden wird,
 * sondern explizit importiert werden muss.
 *
 * WICHTIG: Wir scannen HIER NICHT das 'at.mocode.ping' Package!
 * Das verhindert, dass echte Services und Repositories geladen werden.
 * Wir scannen nur die Security-Infrastruktur.
 *
 * Den Controller importieren wir explizit, damit er verfügbar ist.
 */
@SpringBootApplication
@ComponentScan(
    basePackages = ["at.mocode.infrastructure.security"]
)
@Import(PingController::class)
@EnableAspectJAutoProxy(proxyTargetClass = true) // Erzwingt CGLIB Proxies für Controller
class TestPingServiceApplication
