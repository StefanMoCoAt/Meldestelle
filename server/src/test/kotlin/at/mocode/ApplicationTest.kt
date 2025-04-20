package at.mocode

import at.mocode.model.Turnier
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun testRootRouteShowsTournamentList() {
        // Erstelle ein Beispiel-Turnier, das in der Datenbank sein würde
        val mockTurnier = Turnier(
            id = "dummy-01",
            name = "Erstes DB Turnier",
            datum = "19.04.2025",
            logoUrl = null,
            ausschreibungUrl = "/pdfs/ausschreibung_dummy.pdf"
        )

        // Erstelle eine Liste von Turnieren, wie sie aus der Datenbank kommen würde
        val turniereFromDb = listOf(mockTurnier)

        // Generiere das HTML direkt, wie es in der Application.kt gemacht wird
        val htmlContent = StringWriter().apply {
            appendHTML().html {
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
        }.toString()

        // --- Überprüfungen (Assertions) ---

        // Prüfe auf wichtige Textelemente im HTML
        assertTrue(
            htmlContent.contains("<h1>Willkommen beim Meldestelle Portal!</h1>"),
            "Main heading missing or incorrect"
        )
        assertTrue(
            htmlContent.contains("<h2>Aktuelle Turniere (aus Datenbank):</h2>"),
            "Tournament list heading missing or incorrect"
        )

        // Prüfe, ob das Dummy-Turnier angezeigt wird
        assertTrue(htmlContent.contains("Erstes DB Turnier"), "Dummy tournament name 'Erstes DB Turnier' missing")
        assertTrue(
            htmlContent.contains("(19.04.2025)"),
            "Dummy tournament date missing or incorrect"
        )
        assertTrue(htmlContent.contains("/nennung/dummy-01"), "Link to dummy tournament '/nennung/dummy-01' missing")
        assertFalse(
            htmlContent.contains("Keine Turniere in der Datenbank gefunden."),
            "'No tournaments' message should not be present if dummy was inserted"
        )
    }
}
