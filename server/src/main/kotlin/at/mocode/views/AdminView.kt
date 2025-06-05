package at.mocode.views

import at.mocode.model.Turnier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*

/**
 * View class for generating HTML for the admin pages.
 */
class AdminView {
    private val layoutTemplate = LayoutTemplate()

    /**
     * Generates the HTML response for the tournament management page.
     * @param call The ApplicationCall to respond to
     * @param turniere List of tournaments to display
     */
    suspend fun renderTournamentManagementPage(call: ApplicationCall, turniere: List<Turnier>) {
        call.respondHtml(HttpStatusCode.OK) {
            layoutTemplate.apply {
                applyLayout("Turnierverwaltung") {
                    h1 { +"Turnierverwaltung" }

                    // Form to add a new tournament
                    div(classes = "admin-section mb-4") {
                        h2 {
                            i("fas fa-plus-circle") {}
                            +" Neues Turnier hinzufügen"
                        }
                        form(action = "/admin/tournaments/add", method = FormMethod.post, classes = "admin-form") {
                            div(classes = "form-row") {
                                div(classes = "form-group form-group-third") {
                                    label {
                                        htmlFor = "turnier-number"
                                        +"Turniernummer:"
                                    }
                                    input(type = InputType.number) {
                                        id = "turnier-number"
                                        name = "number"
                                        required = true
                                        placeholder = "z.B. 12345"
                                    }
                                }
                                div(classes = "form-group form-group-third") {
                                    label {
                                        htmlFor = "turnier-name"
                                        +"Turniername:"
                                    }
                                    input(type = InputType.text) {
                                        id = "turnier-name"
                                        name = "name"
                                        required = true
                                        placeholder = "Name des Turniers"
                                    }
                                }
                                div(classes = "form-group form-group-third") {
                                    label {
                                        htmlFor = "turnier-datum"
                                        +"Datum:"
                                    }
                                    input(type = InputType.text) {
                                        id = "turnier-datum"
                                        name = "datum"
                                        required = true
                                        placeholder = "z.B. 01.01.2023"
                                    }
                                }
                            }

                            div(classes = "bewerbe-section mt-3") {
                                h3 {
                                    i("fas fa-trophy") {}
                                    +" Bewerbe"
                                }
                                div {
                                    id = "bewerbe-container"
                                }
                                button(type = ButtonType.button, classes = "button button-secondary mt-2") {
                                    onClick = "addBewerbField()"
                                    i("fas fa-plus") {}
                                    +" Bewerb hinzufügen"
                                }
                            }

                            div(classes = "form-actions mt-4") {
                                button(type = ButtonType.submit, classes = "button") {
                                    i("fas fa-save") {}
                                    +" Turnier speichern"
                                }
                            }
                        }
                    }

                    // Table of existing tournaments
                    div(classes = "admin-section") {
                        h2 {
                            i("fas fa-list") {}
                            +" Vorhandene Turniere"
                        }

                        if (turniere.isEmpty()) {
                            div(classes = "empty-state") {
                                i("fas fa-info-circle") {}
                                p { +"Keine Turniere vorhanden" }
                            }
                        } else {
                            div(classes = "table-responsive") {
                                table {
                                    thead {
                                        tr {
                                            th { +"Nummer" }
                                            th { +"Name" }
                                            th { +"Datum" }
                                            th { +"Bewerbe" }
                                            th { +"Aktionen" }
                                        }
                                    }
                                    tbody {
                                        turniere.forEach { turnier ->
                                            tr {
                                                td { +turnier.number.toString() }
                                                td { +turnier.name }
                                                td { +turnier.datum }
                                                td {
                                                    if (turnier.bewerbe.isEmpty()) {
                                                        +"Keine Bewerbe"
                                                    } else {
                                                        ul(classes = "competition-list") {
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
                                                td {
                                                    button(type = ButtonType.button, classes = "button button-secondary") {
                                                        onClick = "loadTurnierForEdit(${turnier.number})"
                                                        i("fas fa-edit") {}
                                                        +" Bearbeiten"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    // JavaScript for dynamic form handling
                    script(type = "text/javascript") {
                        unsafe {
                            +"""
                            let bewerbCounter = 0;

                            function addBewerbField() {
                                bewerbCounter++;
                                const container = document.getElementById('bewerbe-container');
                                const bewerbDiv = document.createElement('div');
                                bewerbDiv.className = 'bewerb-container';
                                bewerbDiv.id = 'bewerb-' + bewerbCounter;

                                bewerbDiv.innerHTML = '<h4><i class="fas fa-trophy"></i> Bewerb ' + bewerbCounter + '</h4>' +
                                    '<div class="form-row">' +
                                    '<div class="form-group form-group-third">' +
                                    '<label for="bewerb-nummer-' + bewerbCounter + '">Nummer:</label>' +
                                    '<input type="number" id="bewerb-nummer-' + bewerbCounter + '" name="bewerb-nummer[]" placeholder="Bewerbnummer" required>' +
                                    '</div>' +
                                    '<div class="form-group form-group-third">' +
                                    '<label for="bewerb-titel-' + bewerbCounter + '">Titel:</label>' +
                                    '<input type="text" id="bewerb-titel-' + bewerbCounter + '" name="bewerb-titel[]" placeholder="Titel des Bewerbs" required>' +
                                    '</div>' +
                                    '<div class="form-group form-group-third">' +
                                    '<label for="bewerb-klasse-' + bewerbCounter + '">Klasse:</label>' +
                                    '<input type="text" id="bewerb-klasse-' + bewerbCounter + '" name="bewerb-klasse[]" placeholder="Klasse" required>' +
                                    '</div>' +
                                    '</div>' +
                                    '<div class="form-group">' +
                                    '<label for="bewerb-task-' + bewerbCounter + '">Task (optional):</label>' +
                                    '<input type="text" id="bewerb-task-' + bewerbCounter + '" name="bewerb-task[]" placeholder="Optionale Task-Beschreibung">' +
                                    '</div>' +
                                    '<button type="button" onclick="removeBewerbField(' + bewerbCounter + ')" class="button button-secondary"><i class="fas fa-trash"></i> Bewerb entfernen</button>';

                                container.appendChild(bewerbDiv);
                            }

                            function removeBewerbField(id) {
                                const bewerbDiv = document.getElementById('bewerb-' + id);
                                bewerbDiv.remove();
                            }

                            function loadTurnierForEdit(number) {
                                window.location.href = '/admin/tournaments/edit/' + number;
                            }
                            """
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates the HTML response for the tournament edit page.
     * @param call The ApplicationCall to respond to
     * @param turnier The tournament to edit
     */
    suspend fun renderTournamentEditPage(call: ApplicationCall, turnier: Turnier) {
        call.respondHtml(HttpStatusCode.OK) {
            layoutTemplate.apply {
                applyLayout("Turnier bearbeiten") {
                    h1 { +"Turnier bearbeiten" }

                    div(classes = "admin-section") {
                        form(action = "/admin/tournaments/update/${turnier.number}", method = FormMethod.post, classes = "admin-form") {
                            div(classes = "form-row") {
                                div(classes = "form-group form-group-third") {
                                    label {
                                        htmlFor = "turnier-number"
                                        +"Turniernummer:"
                                    }
                                    input(type = InputType.number) {
                                        id = "turnier-number"
                                        name = "number"
                                        value = turnier.number.toString()
                                        readonly = true
                                        classes = setOf("readonly-field")
                                    }
                                }
                                div(classes = "form-group form-group-third") {
                                    label {
                                        htmlFor = "turnier-name"
                                        +"Turniername:"
                                    }
                                    input(type = InputType.text) {
                                        id = "turnier-name"
                                        name = "name"
                                        value = turnier.name
                                        required = true
                                    }
                                }
                                div(classes = "form-group form-group-third") {
                                    label {
                                        htmlFor = "turnier-datum"
                                        +"Datum:"
                                    }
                                    input(type = InputType.text) {
                                        id = "turnier-datum"
                                        name = "datum"
                                        value = turnier.datum
                                        required = true
                                    }
                                }
                            }

                            div(classes = "bewerbe-section mt-3") {
                                h3 {
                                    i("fas fa-trophy") {}
                                    +" Bewerbe"
                                }
                                div {
                                    id = "bewerbe-container"

                                    // Render existing competitions
                                    turnier.bewerbe.forEachIndexed { index, bewerb ->
                                        val bewerbIndex = index + 1
                                        div(classes = "bewerb-container") {
                                            id = "bewerb-$bewerbIndex"
                                            h4 {
                                                i("fas fa-trophy") {}
                                                +" Bewerb $bewerbIndex"
                                            }

                                            div(classes = "form-row") {
                                                div(classes = "form-group form-group-third") {
                                                    label {
                                                        htmlFor = "bewerb-nummer-$bewerbIndex"
                                                        +"Nummer:"
                                                    }
                                                    input(type = InputType.number) {
                                                        id = "bewerb-nummer-$bewerbIndex"
                                                        name = "bewerb-nummer[]"
                                                        value = bewerb.nummer.toString()
                                                        required = true
                                                    }
                                                }
                                                div(classes = "form-group form-group-third") {
                                                    label {
                                                        htmlFor = "bewerb-titel-$bewerbIndex"
                                                        +"Titel:"
                                                    }
                                                    input(type = InputType.text) {
                                                        id = "bewerb-titel-$bewerbIndex"
                                                        name = "bewerb-titel[]"
                                                        value = bewerb.titel
                                                        required = true
                                                    }
                                                }
                                                div(classes = "form-group form-group-third") {
                                                    label {
                                                        htmlFor = "bewerb-klasse-$bewerbIndex"
                                                        +"Klasse:"
                                                    }
                                                    input(type = InputType.text) {
                                                        id = "bewerb-klasse-$bewerbIndex"
                                                        name = "bewerb-klasse[]"
                                                        value = bewerb.klasse
                                                        required = true
                                                    }
                                                }
                                            }
                                            div(classes = "form-group") {
                                                label {
                                                    htmlFor = "bewerb-task-$bewerbIndex"
                                                    +"Task (optional):"
                                                }
                                                input(type = InputType.text) {
                                                    id = "bewerb-task-$bewerbIndex"
                                                    name = "bewerb-task[]"
                                                    value = bewerb.task ?: ""
                                                }
                                            }
                                            button(type = ButtonType.button, classes = "button button-secondary") {
                                                onClick = "removeBewerbField($bewerbIndex)"
                                                i("fas fa-trash") {}
                                                +" Bewerb entfernen"
                                            }
                                        }
                                    }
                                }
                                button(type = ButtonType.button, classes = "button button-secondary mt-2") {
                                    onClick = "addBewerbField()"
                                    i("fas fa-plus") {}
                                    +" Bewerb hinzufügen"
                                }
                            }

                            div(classes = "form-actions mt-4") {
                                button(type = ButtonType.submit, classes = "button") {
                                    i("fas fa-save") {}
                                    +" Turnier aktualisieren"
                                }
                                a(href = "/admin/tournaments", classes = "button button-secondary ml-2") {
                                    i("fas fa-arrow-left") {}
                                    +" Zurück zur Turnierverwaltung"
                                }
                            }
                        }
                    }


                    // JavaScript for dynamic form handling
                    script(type = "text/javascript") {
                        unsafe {
                            raw("let bewerbCounter = ${turnier.bewerbe.size};")
                            +"""
                            function addBewerbField() {
                                bewerbCounter++;
                                const container = document.getElementById('bewerbe-container');
                                const bewerbDiv = document.createElement('div');
                                bewerbDiv.className = 'bewerb-container';
                                bewerbDiv.id = 'bewerb-' + bewerbCounter;

                                bewerbDiv.innerHTML = '<h4><i class="fas fa-trophy"></i> Neuer Bewerb</h4>' +
                                    '<div class="form-row">' +
                                    '<div class="form-group form-group-third">' +
                                    '<label for="bewerb-nummer-' + bewerbCounter + '">Nummer:</label>' +
                                    '<input type="number" id="bewerb-nummer-' + bewerbCounter + '" name="bewerb-nummer[]" placeholder="Bewerbnummer" required>' +
                                    '</div>' +
                                    '<div class="form-group form-group-third">' +
                                    '<label for="bewerb-titel-' + bewerbCounter + '">Titel:</label>' +
                                    '<input type="text" id="bewerb-titel-' + bewerbCounter + '" name="bewerb-titel[]" placeholder="Titel des Bewerbs" required>' +
                                    '</div>' +
                                    '<div class="form-group form-group-third">' +
                                    '<label for="bewerb-klasse-' + bewerbCounter + '">Klasse:</label>' +
                                    '<input type="text" id="bewerb-klasse-' + bewerbCounter + '" name="bewerb-klasse[]" placeholder="Klasse" required>' +
                                    '</div>' +
                                    '</div>' +
                                    '<div class="form-group">' +
                                    '<label for="bewerb-task-' + bewerbCounter + '">Task (optional):</label>' +
                                    '<input type="text" id="bewerb-task-' + bewerbCounter + '" name="bewerb-task[]" placeholder="Optionale Task-Beschreibung">' +
                                    '</div>' +
                                    '<button type="button" onclick="removeBewerbField(' + bewerbCounter + ')" class="button button-secondary"><i class="fas fa-trash"></i> Bewerb entfernen</button>';

                                container.appendChild(bewerbDiv);
                            }

                            function removeBewerbField(id) {
                                const bewerbDiv = document.getElementById('bewerb-' + id);
                                bewerbDiv.remove();
                            }
                            """
                        }
                    }
                }
            }
        }
    }
}
