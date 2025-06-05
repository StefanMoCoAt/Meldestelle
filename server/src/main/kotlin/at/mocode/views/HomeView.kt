package at.mocode.views

import at.mocode.model.Turnier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*

/**
 * View class for generating HTML for the home page.
 */
class HomeView {
    private val layoutTemplate = LayoutTemplate()

    /**
     * Generates the HTML response for the home page.
     * @param call The ApplicationCall to respond to
     * @param turniere List of tournaments to display
     */
    suspend fun renderHomePage(call: ApplicationCall, turniere: List<Turnier>) {
        call.respondHtml(HttpStatusCode.OK) {
            layoutTemplate.apply {
                applyLayout("Meldestelle Portal - Startseite") {
                    h1 { +"Willkommen beim Meldestelle Portal!" }
                    p(classes = "text-center mb-3") { +"Hier finden Sie alle aktuellen Turniere und können sich online anmelden." }

                    div(classes = "mt-4") {
                        h2 { +"Aktuelle Turniere" }

                        if (turniere.isEmpty()) {
                            div(classes = "text-center mt-3 mb-3") {
                                p { +"Keine Turniere in der Datenbank gefunden." }
                            }
                        } else {
                            div(classes = "tournament-list") {
                                turniere.forEach { turnier ->
                                    div(classes = "tournament-item mb-3") {
                                        div(classes = "tournament-header") {
                                            h3 { +turnier.name }
                                            p {
                                                i("far fa-calendar-alt") {}
                                                +" ${turnier.datum}"
                                            }
                                            p {
                                                i("fas fa-hashtag") {}
                                                +" Turnier-Nr.: ${turnier.number}"
                                            }
                                        }

                                        div(classes = "tournament-competitions mt-2") {
                                            h4 { +"Bewerbe:" }
                                            if (turnier.bewerbe.isEmpty()) {
                                                p { +"Keine Bewerbe verfügbar" }
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

                                        div(classes = "tournament-actions mt-2") {
                                            a(href = "/nennung/${turnier.number}", classes = "button") {
                                                i("fas fa-edit") {}
                                                +" Online Nennen"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
