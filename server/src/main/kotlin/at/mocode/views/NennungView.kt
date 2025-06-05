package at.mocode.views

import at.mocode.model.Turnier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*

/**
 * View class for generating HTML for the tournament registration forms.
 */
class NennungView {
    private val layoutTemplate = LayoutTemplate()

    /**
     * Generates the HTML response for the tournament registration form.
     * @param call The ApplicationCall to respond to
     * @param turnier The tournament to display the registration form for
     */
    suspend fun renderNennungForm(call: ApplicationCall, turnier: Turnier) {
        call.respondHtml(HttpStatusCode.OK) {
            layoutTemplate.apply {
                applyLayout(
                    title = "Online-Nennen - ${turnier.name}",
                    showNavbar = false,
                    showAdminLink = false
                ) {
                    h1 { +"Online-Nennen" }

                    // Tournament description
                    div(classes = "tournament-info text-center mb-4") {
                        h2 { +turnier.name }
                        p { +turnier.datum }
                        p { +"Turnier-Nr.: ${turnier.number}" }
                    }

                    form(action = "/nennung/${turnier.number}/submit", method = FormMethod.post, classes = "registration-form") {
                        div(classes = "form-section mb-4") {
                            h3 { +"Teilnehmer-Informationen" }

                            // Participant information list
                            div(classes = "competitions-list") {
                                // Rider information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        label(classes = "required") { +"Reiter-Name" }
                                        input(type = InputType.text, name = "riderName") {
                                            attributes["required"] = "required"
                                            attributes["placeholder"] = "Vor- und Nachname"
                                        }
                                    }
                                }

                                // Horse information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        label(classes = "required") { +"Kopf-Nr./Pferd" }
                                        input(type = InputType.text, name = "horseName") {
                                            attributes["required"] = "required"
                                            attributes["placeholder"] = "Name des Pferdes"
                                        }
                                    }
                                }

                                // Contact information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        div(classes = "form-row") {
                                            div(classes = "form-group form-group-half") {
                                                label { +"E-Mail" }
                                                input(type = InputType.email, name = "email") {
                                                    attributes["placeholder"] = "ihre@email.com"
                                                }
                                            }
                                        }
                                    }
                                }

                                // Contact information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        div(classes = "form-row") {
                                            div(classes = "form-group form-group-half") {
                                                label { +"Telefon-Nr." }
                                                input(type = InputType.tel, name = "phone") {
                                                    attributes["placeholder"] = "Ihre Telefonnummer"
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            p(classes = "form-hint") { +"Bitte geben Sie mindestens eine Kontaktmöglichkeit an (E-Mail oder Telefon)." }
                        }

                        // Competitions list
                        div(classes = "form-section mb-4") {
                            h3 { +"Bewerbe" }

                            if (turnier.bewerbe.isEmpty()) {
                                p { +"Keine Bewerbe verfügbar." }
                            } else {
                                div(classes = "competitions-list") {
                                    turnier.bewerbe.forEach { bewerb ->
                                        div(classes = "competition-item") {
                                            label {
                                                input(type = InputType.checkBox, name = "selectedEvents") {
                                                    attributes["value"] = bewerb.nummer.toString()
                                                }
                                                span(classes = "competition-details") {
                                                    strong { +"${bewerb.nummer}. ${bewerb.titel}" }
                                                    +" - ${bewerb.klasse}"
                                                    if (bewerb.task != null) {
                                                        +" - ${bewerb.task}"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Comments
                        div(classes = "form-section mb-4") {
                            h3 { +"Zusätzliche Informationen" }

                            div(classes = "form-group") {
                                label { +"Wünsche/Bemerkungen" }
                                textArea {
                                    attributes["rows"] = "4"
                                    attributes["name"] = "comments"
                                    attributes["placeholder"] = "Ihre Wünsche oder Bemerkungen zur Nennung..."
                                }
                            }
                        }

                        // Submit button
                        div(classes = "form-actions text-center mt-4") {
                            button(type = ButtonType.submit, classes = "button") {
                                +"Jetzt Nennen"
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * Generates the HTML response for the confirmation page after successful registration.
     * @param call The ApplicationCall to respond to
     * @param turnier The tournament the registration was for
     * @param riderName The name of the rider
     * @param horseName The name of the horse
     */
    suspend fun renderConfirmationPage(call: ApplicationCall, turnier: Turnier, riderName: String, horseName: String) {
        call.respondHtml(HttpStatusCode.OK) {
            layoutTemplate.apply {
                applyLayout(
                    title = "Nennung bestätigt - ${turnier.name}",
                    showNavbar = false,
                    showAdminLink = false
                ) {
                    h1 { +"Nennung bestätigt" }

                    div(classes = "confirmation-box") {
                        div(classes = "confirmation-icon") {
                            /* Icon removed as per request */
                        }

                        h2 { +"Vielen Dank für Ihre Nennung!" }

                        div(classes = "confirmation-details") {
                            p {
                                +"Ihre Nennung für "
                                strong { +turnier.name }
                                +" wurde erfolgreich übermittelt."
                            }

                            div(classes = "detail-item") {
                                span(classes = "detail-label") { +"Reiter:" }
                                span(classes = "detail-value") { +riderName }
                            }

                            div(classes = "detail-item") {
                                span(classes = "detail-label") { +"Pferd:" }
                                span(classes = "detail-value") { +horseName }
                            }
                        }

                        p(classes = "confirmation-message") {
                            +"Start und Ergebnislisten auf"
                        }

                        p(classes = "confirmation-message") {
                            +"www.ihremeldestelle.at"
                        }

                        div(classes = "confirmation-actions") {
                            a(href = "/nennung/${turnier.number}", classes = "button") {
                                +"Weitere Nennung abgeben"
                            }
                        }
                    }

                }
            }
        }
    }
}
