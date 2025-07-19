package at.mocode.events.ui.components

import at.mocode.events.domain.model.Veranstaltung
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import emotion.react.css

/**
 * Props for the VeranstaltungsListe component
 */
external interface VeranstaltungsListeProps : Props

// Create Ktor client for API calls
private val apiClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

/**
 * React component that displays a list of events (Veranstaltungen).
 *
 * This component loads event data from the API and renders it as HTML.
 * Uses useState for state management and useEffectOnce for data loading.
 */
val VeranstaltungsListe = FC<VeranstaltungsListeProps> { _ ->
    // State management with useState
    var events by useState<List<Veranstaltung>>(emptyList())
    var loading by useState(true)
    var error by useState<String?>(null)

    // Data loading with useEffectOnce hook
    useEffectOnce {
        val scope = MainScope()
        scope.launch {
            try {
                loading = true
                error = null
                // Load data with Ktor client
                val response = apiClient.get("http://localhost:8080/api/events")
                val loadedEvents: List<Veranstaltung> = response.body()
                events = loadedEvents
            } catch (e: Exception) {
                error = "Fehler beim Laden der Veranstaltungen: ${e.message}"
                console.error("Error loading events:", e)
            } finally {
                loading = false
            }
        }
    }

    // Render HTML with React DOM elements
    div {
        css {
            // Basic styling for the main container
        }

        h1 {
            +"Veranstaltungen"
        }

        when {
            loading -> {
                div {
                    +"Lade Veranstaltungen..."
                }
            }
            error != null -> {
                div {
                    +error!!
                }
            }
            events.isEmpty() -> {
                div {
                    +"Keine Veranstaltungen verfügbar"
                }
            }
            else -> {
                div {
                    events.forEach { event ->
                        div {
                            h3 {
                                +event.name
                            }

                            p {
                                span {
                                    +"📍"
                                }
                                +" ${event.ort}"
                            }

                            p {
                                span {
                                    +"📅"
                                }
                                if (event.isMultiDay()) {
                                    +" ${event.startDatum} - ${event.endDatum} (${event.getDurationInDays()} Tage)"
                                } else {
                                    +" ${event.startDatum} (Eintägige Veranstaltung)"
                                }
                            }

                            // Status indicators
                            val statusList = mutableListOf<String>()
                            if (event.istAktiv) statusList.add("Aktiv")
                            if (event.istOeffentlich) statusList.add("Öffentlich")
                            if (event.isRegistrationOpen()) statusList.add("Anmeldung offen")
                            if (statusList.isNotEmpty()) {
                                p {
                                    span {
                                        +"ℹ️"
                                    }
                                    +" Status: ${statusList.joinToString(", ")}"
                                }
                            }

                            // Description
                            if (!event.beschreibung.isNullOrBlank()) {
                                p {
                                    span {
                                        +"📝"
                                    }
                                    +" ${event.beschreibung}"
                                }
                            }

                            // Sports/Sparten
                            if (event.sparten.isNotEmpty()) {
                                p {
                                    span {
                                        +"🏆"
                                    }
                                    +" Sparten: ${event.sparten.joinToString(", ") { it.name }}"
                                }
                            }

                            // Additional info
                            event.maxTeilnehmer?.let { max ->
                                p {
                                    span {
                                        +"👥"
                                    }
                                    +" Max. Teilnehmer: $max"
                                }
                            }

                            event.anmeldeschluss?.let { deadline ->
                                p {
                                    span {
                                        +"⏰"
                                    }
                                    +" Anmeldeschluss: $deadline"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
