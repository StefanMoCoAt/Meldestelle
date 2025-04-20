package at.mocode

import at.mocode.model.Turnier
import at.mocode.tables.TurniereTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Test-spezifische Version der configureDatabase-Funktion, die eine In-Memory-Datenbank verwendet.
 */
fun configureTestDatabase() {
    val log = LoggerFactory.getLogger("TestDatabaseInitialization")
    log.info("Initializing in-memory H2 database for testing...")

    // Verbinde mit einer In-Memory-H2-Datenbank
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    // Initialisiere das Datenbankschema
    transaction {
        log.info("Creating test database schema...")
        SchemaUtils.create(TurniereTable)

        // Füge ein Test-Turnier hinzu
        log.info("Inserting test tournament data...")
        TurniereTable.insert {
            it[id] = "dummy-01"
            it[name] = "Erstes DB Turnier"
            it[datum] = "19.04.2025"
            it[logoUrl] = null
            it[ausschreibungUrl] = "/pdfs/ausschreibung_dummy.pdf"
        }

        log.info("Test database initialized successfully!")
    }
}

/**
 * Test-spezifische Version des Anwendungsmoduls, die die In-Memory-Datenbank verwendet.
 */
fun Application.testModule() {
    // Konfiguriere die Test-Datenbank
    configureTestDatabase()

    // Konfiguriere das Routing wie in der Original-Anwendung
    routing {
        get("/") {
            val log = LoggerFactory.getLogger("RootRoute")

            // Lese Daten aus der Test-Datenbank
            val turniereFromDb = transaction {
                TurniereTable.selectAll().map { row ->
                    Turnier(
                        id = row[TurniereTable.id],
                        name = row[TurniereTable.name],
                        datum = row[TurniereTable.datum],
                        logoUrl = row[TurniereTable.logoUrl],
                        ausschreibungUrl = row[TurniereTable.ausschreibungUrl]
                    )
                }
            }

            // HTML-Antwort generieren (wie in Application.kt)
            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title { +"Meldestelle Portal" }
                }
                body {
                    h1 { +"Willkommen beim Meldestelle Portal!" }
                    p { +"Datenbankverbindung erfolgreich!" }
                    hr()
                    h2 { +"Aktuelle Turniere (aus Datenbank):" }

                    ul {
                        if (turniereFromDb.isEmpty()) {
                            li { +"Keine Turniere in der Datenbank gefunden." }
                        } else {
                            turniereFromDb.forEach { turnier ->
                                li {
                                    strong { +turnier.name }
                                    +" (${turnier.datum})"
                                    +" "
                                    if (turnier.ausschreibungUrl != null) {
                                        a(href = turnier.ausschreibungUrl, target = "_blank") {
                                            button { +"Ausschreibung" }
                                        }
                                        +" "
                                    }
                                    a(href = "/nennung/${turnier.id}") {
                                        button { +"Online Nennen" }
                                    }
                                }
                            }
                        }
                    }
                    hr()
                    p { a(href = "/admin/tournaments") { +"Zur Turnierverwaltung (TODO)" } }
                }
            }
        }
    }
}
