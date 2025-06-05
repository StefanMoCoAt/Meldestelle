package at.mocode

import at.mocode.model.Bewerb
import at.mocode.model.Turnier
import at.mocode.tables.BewerbeTable
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
    log.info("Initializing in-memory SQLite database for testing...")

    // Verbinde mit einer In-Memory-SQLite-Datenbank
    Database.connect("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")

    // Initialisiere das Datenbankschema
    transaction {
        log.info("Creating test database schema...")
        SchemaUtils.create(TurniereTable, BewerbeTable)

        // Füge ein Test-Turnier hinzu
        log.info("Inserting test tournament data...")
        val turnierNumber = 1
        TurniereTable.insert {
            it[TurniereTable.number] = turnierNumber
            it[TurniereTable.name] = "CSN-C Edelhof April 2025"
            it[TurniereTable.datum] = "14.04.2025 - 15.04.2025"
        }

        // Füge Test-Bewerbe hinzu
        BewerbeTable.insert {
            it[BewerbeTable.nummer] = 1
            it[BewerbeTable.titel] = "Stilspringprüfung"
            it[BewerbeTable.klasse] = "60 cm"
            it[BewerbeTable.task] = null
            it[BewerbeTable.turnierNumber] = turnierNumber
        }

        BewerbeTable.insert {
            it[BewerbeTable.nummer] = 2
            it[BewerbeTable.titel] = "Dressurprüfung"
            it[BewerbeTable.klasse] = "Kl. A"
            it[BewerbeTable.task] = "DRA 1"
            it[BewerbeTable.turnierNumber] = turnierNumber
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
                // Get all tournaments
                val turniere = TurniereTable.selectAll().map { row ->
                    Turnier(
                        name = row[TurniereTable.name],
                        datum = row[TurniereTable.datum],
                        number = row[TurniereTable.number]
                    )
                }

                // For each tournament, get its competitions
                turniere.forEach { turnier ->
                    val bewerbeList = BewerbeTable.selectAll().where { BewerbeTable.turnierNumber eq turnier.number }
                        .map { row ->
                        Bewerb(
                            nummer = row[BewerbeTable.nummer],
                            titel = row[BewerbeTable.titel],
                            klasse = row[BewerbeTable.klasse],
                            task = row[BewerbeTable.task]
                        )
                    }
                    turnier.bewerbe = bewerbeList
                }

                turniere
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
                                    div {
                                        +"Bewerbe: "
                                        if (turnier.bewerbe.isEmpty()) {
                                            +"Keine"
                                        } else {
                                            ul {
                                                turnier.bewerbe.forEach { bewerb ->
                                                    li {
                                                        +"${bewerb.nummer}. ${bewerb.titel} - ${bewerb.klasse}"
                                                        if (bewerb.task != null) {
                                                            +" (${bewerb.task})"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    +" "
                                    a(href = "/nennung/${turnier.number}") {
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
