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
                    showAdminLink = false,
                    showFooter = false
                ) {
                    h1 { +"Online-Nennen" }

                    // Tournament description

                    div(classes = "tournament-info mb-3") {
                        h2 { +turnier.name }
                        p { +turnier.datum }
                        p { +"Turnier-Nr.: ${turnier.number}" }
                    }


                    form(
                        action = "/nennung/${turnier.number}/submit",
                        method = FormMethod.post,
                        classes = "registration-form needs-validation"
                    ) {
                        attributes["onsubmit"] = "return validateForm(this)"
                        style {
                            unsafe {
                                +"""
                                .validation-error {
                                    border: 2px solid red !important;
                                    background-color: rgba(255, 0, 0, 0.05) !important;
                                    transition: all 0.3s ease;
                                }
                                """
                            }
                        }
                        script {
                            unsafe {
                                +"""
                                function validateForm(form) {

                                    // Check if rider name is provided
                                    var riderName = form.querySelector('input[name="riderName"]').value.trim();
                                    if (riderName === '') {
                                        // Focus on the rider name field
                                        var riderNameField = form.querySelector('input[name="riderName"]');
                                        riderNameField.focus();
                                        // Highlight the field
                                        riderNameField.classList.add('validation-error');
                                        setTimeout(function() {
                                            riderNameField.classList.remove('validation-error');
                                        }, 2000);
                                        return false;
                                    }

                                    // Check if horse name is provided
                                    var horseName = form.querySelector('input[name="horseName"]').value.trim();
                                    if (horseName === '') {
                                        // Focus on the horse name field
                                        var horseNameField = form.querySelector('input[name="horseName"]');
                                        horseNameField.focus();
                                        // Highlight the field
                                        horseNameField.classList.add('validation-error');
                                        setTimeout(function() {
                                            horseNameField.classList.remove('validation-error');
                                        }, 2000);
                                        return false;
                                    }

                                    // Check if at least one contact method is provided
                                    var email = form.querySelector('input[name="email"]').value.trim();
                                    var phone = form.querySelector('input[name="phone"]').value.trim();
                                    if (email === '' && phone === '') {
                                        // Focus on the email field
                                        var emailField = form.querySelector('input[name="email"]');
                                        emailField.focus();
                                        // Highlight both email and phone fields
                                        emailField.classList.add('validation-error');
                                        var phoneField = form.querySelector('input[name="phone"]');
                                        phoneField.classList.add('validation-error');
                                        setTimeout(function() {
                                            emailField.classList.remove('validation-error');
                                            phoneField.classList.remove('validation-error');
                                        }, 2000);
                                        return false;
                                    }

                                    // Check if at least one competition is selected
                                    var checkboxes = form.querySelectorAll('input[name="selectedEvents"]');
                                    var checked = false;
                                    for (var i = 0; i < checkboxes.length; i++) {
                                        if (checkboxes[i].checked) {
                                            checked = true;
                                            break;
                                        }
                                    }
                                    if (!checked) {
                                        // Focus on the competitions section
                                        var competitionsSections = form.querySelectorAll('div.form-section h3');
                                        var competitionsSection = null;
                                        for (var i = 0; i < competitionsSections.length; i++) {
                                            if (competitionsSections[i].textContent === 'Bewerbe') {
                                                competitionsSection = competitionsSections[i].parentNode;
                                                break;
                                            }
                                        }
                                        if (competitionsSection) {
                                            competitionsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                                            // Highlight the section
                                            competitionsSection.classList.add('validation-error');
                                            setTimeout(function() {
                                                competitionsSection.classList.remove('validation-error');
                                            }, 2000);
                                        }
                                        return false;
                                    }

                                    return true;
                                }
                                """
                            }
                        }
                        div(classes = "form-section mb-4") {
                            h3 { +"Teilnehmer-Informationen" }

                            // Participant information list
                            div(classes = "competitions-list") {
                                // Rider information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        label(classes = "required") { +"Reiter-Name" }
                                        input(type = InputType.text, name = "riderName", classes = "form-control") {
                                            attributes["required"] = "required"
                                            attributes["placeholder"] = "Vor- und Nachname"
                                            attributes["class"] = "form-control"
                                            attributes["oninvalid"] =
                                                "this.setCustomValidity('Fügen Sie den Namen des Reiters ein!')"
                                            attributes["oninput"] = "this.setCustomValidity('')"
                                        }
                                    }
                                }

                                // Horse information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        label(classes = "required") { +"Kopf-Nr./Pferd" }
                                        input(type = InputType.text, name = "horseName", classes = "form-control") {
                                            attributes["required"] = "required"
                                            attributes["placeholder"] = "Kopf-Nr. / Name des Pferdes"
                                            attributes["class"] = "form-control"
                                            attributes["oninvalid"] =
                                                "this.setCustomValidity('Fügen Sie die Kopf-Nr. und oder den Pferdenamen ein!')"
                                            attributes["oninput"] = "this.setCustomValidity('')"
                                        }
                                    }
                                }

                                // Contact information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        div(classes = "form-group") {
                                            label(classes = "required") { +"E-Mail" }
                                            input(type = InputType.email, name = "email") {
                                                attributes["placeholder"] = "ihre@email.com"
                                                attributes["class"] = "form-control"
                                                attributes["oninvalid"] =
                                                    "this.setCustomValidity('Fügen Sie Ihre E-Mail Adresse oder Telefon-Nr. ein!')"
                                                attributes["oninput"] = "this.setCustomValidity('')"
                                            }
                                        }
                                    }
                                }

                                // Contact information
                                div(classes = "competition-item") {
                                    div(classes = "participant-details") {
                                        div(classes = "form-group") {
                                            label(classes = "required") { +"Telefon-Nr." }
                                            input(type = InputType.tel, name = "phone") {
                                                attributes["placeholder"] = "Ihre Telefonnummer"
                                                attributes["class"] = "form-control"
                                                attributes["oninvalid"] =
                                                    "this.setCustomValidity('Fügen Sie Ihre E-Mail Adresse oder Telefon-Nr. ein!')"
                                                attributes["oninput"] = "this.setCustomValidity('')"
                                            }
                                        }
                                    }
                                }

                            }
                            p(classes = "form-hint") { +"Bitte geben Sie mindestens eine Kontaktmöglichkeit an (E-Mail oder Telefon). Sie müssen nicht beide Felder ausfüllen." }
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
                                            label(classes = "form-check") {
                                                input(
                                                    type = InputType.checkBox,
                                                    name = "selectedEvents",
                                                    classes = "form-check-input"
                                                ) {
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
                                textArea(classes = "form-control") {
                                    attributes["rows"] = "4"
                                    attributes["name"] = "comments"
                                    attributes["placeholder"] = "Ihre Wünsche oder Bemerkungen zur Nennung..."
                                }
                            }
                        }

                        // Submit button
                        div(classes = "form-actions mt-4") {
                            attributes["style"] = "justify-content: center;"
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
     * @param selectedEvents The list of selected competition IDs
     */
    suspend fun renderConfirmationPage(
        call: ApplicationCall,
        turnier: Turnier,
        riderName: String,
        horseName: String,
        selectedEvents: List<String>
    ) {
        call.respondHtml(HttpStatusCode.OK) {
            layoutTemplate.apply {
                applyLayout(
                    title = "Nennung bestätigt - ${turnier.name}",
                    showNavbar = false,
                    showAdminLink = false,
                    showFooter = false
                ) {

                    div(classes = "confirmation-box") {
                        div(classes = "confirmation-icon") {
                            /* Icon removed as per request */
                        }

                        h2 { +"Vielen Dank für Ihre Nennung!" }

                        div(classes = "confirmation-details") {
                            p(classes = "confirmation-message") { +"Ihre Nennung für " }

                            p(classes = "confirmation-message") { strong { +turnier.name } }

                            p(classes = "confirmation-message") { +" wurde erfolgreich übermittelt." }

                            div(classes = "detail-item") {
                                span(classes = "detail-label") { +"Reiter:" }
                                span(classes = "detail-value") { +riderName }
                            }

                            div(classes = "detail-item") {
                                span(classes = "detail-label") { +"Pferd:" }
                                span(classes = "detail-value") { +horseName }
                            }

                            // Display selected competitions
                            if (selectedEvents.isNotEmpty()) {
                                div(classes = "detail-item mt-3") {
                                    span(classes = "detail-label") { +"Ausgewählte Bewerbe:" }
                                    div(classes = "selected-competitions") {
                                        ul(classes = "competition-list") {
                                            selectedEvents.forEach { eventId ->
                                                val bewerb = turnier.bewerbe.find { it.nummer.toString() == eventId }
                                                if (bewerb != null) {
                                                    span(classes = "detail-value") {
                                                        +"${bewerb.nummer}  .  ${bewerb.titel} - ${bewerb.klasse}"
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
