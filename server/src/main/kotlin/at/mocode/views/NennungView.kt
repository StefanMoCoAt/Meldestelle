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

                    // Additional styles specific to the registration form
                    style {
                        +"""
                        .tournament-info {
                            margin-bottom: 2rem;
                        }

                        .tournament-info h2 {
                            color: var(--primary-color);
                        }

                        .form-section {
                            background-color: white;
                            border-radius: 8px;
                            padding: 2rem;
                            margin-bottom: 2rem;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.08);
                            border-left: 4px solid var(--primary-color);
                        }

                        .form-section h3 {
                            border-bottom: 1px solid var(--border-color);
                            padding-bottom: 0.8rem;
                            margin-bottom: 1.5rem;
                            text-align: left;
                            color: var(--primary-color);
                            font-size: 1.4rem;
                        }

                        .form-row {
                            display: flex;
                            flex-direction: column;
                            width: 100%;
                            margin: 0;
                        }

                        @media (min-width: 768px) {
                            .form-row {
                                flex-direction: row;
                                gap: 20px;
                            }

                            .form-group-half {
                                flex: 1;
                            }
                        }

                        .form-group {
                            width: 100%;
                            margin: 0 0 1.5rem 0;
                        }

                        .form-group-half {
                            flex: 0 0 100%;
                            margin: 0 0 1.5rem 0;
                            max-width: 100%;
                        }

                        .form-hint {
                            font-size: 0.9rem;
                            color: var(--light-text);
                            margin-top: 0.5rem;
                            text-align: center;
                        }

                        .competitions-list {
                            display: flex;
                            flex-direction: column;
                            align-items: flex-start;
                            gap: 15px;
                            width: 100%;
                            margin: 0;
                        }

                        .competition-item {
                            padding: 18px;
                            border-radius: 8px;
                            transition: all 0.3s;
                            width: 100%;
                            text-align: left;
                            border: 1px solid var(--border-color);
                            background-color: white;
                            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                        }

                        .competition-item:hover {
                            background-color: rgba(0,0,0,0.02);
                            box-shadow: 0 3px 8px rgba(0,0,0,0.12);
                        }

                        .competition-item label {
                            display: flex;
                            align-items: center;
                            justify-content: flex-start;
                            cursor: pointer;
                        }

                        .competition-item input[type="checkbox"] {
                            margin-right: 15px;
                            transform: scale(1.2);
                        }

                        .competition-details {
                            text-align: left;
                        }

                        /* Participant information styling */
                        .participant-details {
                            width: 100%;
                        }

                        .participant-details label {
                            display: block;
                            margin-bottom: 0.5rem;
                            font-weight: 500;
                        }

                        .participant-details input {
                            width: 100%;
                        }

                        @media (max-width: 768px) {
                            /* Mobile styles already handled by the responsive layout */
                        }
                        """
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
                            +"Sie erhalten in Kürze eine Bestätigung per E-Mail."
                        }

                        div(classes = "confirmation-actions") {
                            a(href = "/nennung/${turnier.number}", classes = "button") {
                                +"Weitere Nennung abgeben"
                            }
                        }
                    }

                    // Additional styles specific to the confirmation page
                    style {
                        +"""
                        .confirmation-box {
                            background-color: var(--light-bg);
                            border-radius: 8px;
                            padding: 2rem;
                            text-align: center;
                            max-width: 600px;
                            margin: 0 auto;
                            box-shadow: 0 4px 15px rgba(0,0,0,0.05);
                        }

                        .confirmation-icon {
                            font-size: 4rem;
                            color: var(--success-color);
                            margin-bottom: 1rem;
                        }

                        .confirmation-details {
                            margin: 1.5rem 0;
                            padding: 1rem;
                            background-color: var(--container-bg);
                            border-radius: 8px;
                            text-align: left;
                        }

                        .detail-item {
                            display: flex;
                            margin-bottom: 0.5rem;
                            padding: 0.5rem 0;
                            border-bottom: 1px solid var(--border-color);
                        }

                        .detail-item:last-child {
                            border-bottom: none;
                        }

                        .detail-label {
                            font-weight: bold;
                            width: 100px;
                        }

                        .detail-value {
                            flex: 1;
                        }

                        .confirmation-message {
                            margin: 1.5rem 0;
                            color: var(--light-text);
                        }

                        .confirmation-actions {
                            margin-top: 1.5rem;
                        }
                        """
                    }
                }
            }
        }
    }
}
