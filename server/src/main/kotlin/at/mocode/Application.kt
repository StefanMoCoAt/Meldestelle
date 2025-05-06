package at.mocode

import at.mocode.plugins.configureDatabase
import io.ktor.server.application.*
import io.ktor.server.netty.*


fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    // Als Erstes die Datenbank konfigurieren:
    configureDatabase()

    // Danach deine anderen Konfigurationen (Routing etc.):
//    routing {
//        get("/") {
//            // Logger holen (optional, aber nützlich)
//            val log = LoggerFactory.getLogger("RootRoute")
//            // --- Datenbankoperationen ---
//            // alle DB-Zugriffe mit Exposed sollten in einer Transaktion stattfinden
//            val turniereFromDb = transaction {
//                // Optional: Füge ein Test-Turnier hinzu, WENN die Tabelle leer ist.
//                // Das ist nur für den ersten Test praktisch.
//                if (TurniereTable.selectAll().count() == 0L) {
//                    log.info("Turnier table is empty, inserting dummy tournament...")
//                    TurniereTable.insert {
//                        it[id] = "dummy-01" // Eindeutige ID
//
//                    }
//                }
//
//                // Lese ALLE Einträge aus der TurniereTable
//                log.info("Fetching all tournaments from database...")
//                TurniereTable.selectAll().map { row ->
//                    // Wandle jede Datenbank-Zeile (row) wieder in ein Turnier-Objekt um
//                    Turnier(
//                        id = row[TurniereTable.id],
//
//                    )
//                } // Das Ergebnis ist eine List<Turnier>
//            } // Ende der Transaktion
//
//            // --- HTML-Antwort generieren ---
//            call.respondHtml(HttpStatusCode.OK) {
//                head {
//                    title { +"Meldestelle Portal" }
//                }
//                body {
//                    h1 { +"Willkommen beim Meldestelle Portal!" }
//                    p { +"Datenbankverbindung erfolgreich!" } // Kleine Bestätigung
//                    hr()
//                    h2 { +"Aktuelle Turniere (aus Datenbank):" } // Geänderte Überschrift
//
//                    // Gib die Turnierliste aus der Datenbank aus
//                    ul {
//                        if (turniereFromDb.isEmpty()) {
//                            li { +"Keine Turniere in der Datenbank gefunden." }
//                        } else {
//                            // Schleife über die Liste aus der DB
//                            turniereFromDb.forEach { turnier ->
//                                li {
//                                    strong { +turnier.name }
//                                    +" (${turnier.datum})"
//                                    // Füge die Buttons wieder hinzu
//                                    +" "
//                                    if (turnier.ausschreibungUrl != null) {
//                                        a(href = turnier.ausschreibungUrl, target = "_blank") {
//                                            button { +"Ausschreibung" }
//                                        }
//                                        +" "
//                                    }
//                                    a(href = "/nennung/${turnier.id}") {
//                                        button { +"Online Nennen" }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    // Link zum (noch nicht funktionierenden) Admin-Bereich
//                    hr()
//                    p { a(href = "/admin/tournaments") { +"Zur Turnierverwaltung (TODO)" } }
//                }
//            } // <--- HIER endet der respondHtml-Block
//        } // Ende get("/")
//    }
}
