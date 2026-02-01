package at.mocode.ping.infrastructure.persistence

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

/**
 * Minimale Konfiguration für DataJpaTests.
 * Verhindert, dass die echte PingServiceApplication geladen wird,
 * welche einen weiten ComponentScan auslöst und damit Controller/Services lädt,
 * die wir im Repository-Test nicht brauchen (und die Fehler verursachen).
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = ["at.mocode.ping.infrastructure.persistence"])
class TestPersistenceConfig
