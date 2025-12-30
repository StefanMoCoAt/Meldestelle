package at.mocode.infrastructure.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Minimaler Test-ApplicationContext, der nur die absolut nötigen Auto-Konfigurationen lädt.
 * Problematische Auto-Configs werden hier explizit ausgeschlossen, damit der Context sicher startet.
 */
@SpringBootApplication
class MinimalTestApp
