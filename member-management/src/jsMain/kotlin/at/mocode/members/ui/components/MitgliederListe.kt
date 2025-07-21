package at.mocode.members.ui.components

import at.mocode.members.domain.model.DomUser
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
 * Props for the MitgliederListe component
 */
external interface MitgliederListeProps : Props

// Create Ktor client for API calls
private val apiClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

/**
 * React component that displays a list of members (Mitglieder).
 *
 * This component loads member data from the API and renders it as HTML.
 * Uses useState for state management and useEffectOnce for data loading.
 */
val MitgliederListe = FC<MitgliederListeProps> { _ ->
    // State management with useState
    var members by useState<List<DomUser>>(emptyList())
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
                val response = apiClient.get("http://localhost:8080/api/members")
                val loadedMembers: List<DomUser> = response.body()
                members = loadedMembers
            } catch (e: Exception) {
                error = "Fehler beim Laden der Mitglieder: ${e.message}"
                console.error("Error loading members:", e)
            } finally {
                loading = false
            }
        }
    }

    // Render HTML with React DOM elements
    div {
        css {
            // Basic styling for the main container
            "padding" to "20px"
            "fontFamily" to "Arial, sans-serif"
            "maxWidth" to "1200px"
            "margin" to "0 auto"
        }

        h1 {
            css {
                "color" to "#2c3e50"
                "borderBottom" to "2px solid #3498db"
                "paddingBottom" to "10px"
                "marginBottom" to "20px"
            }
            +"Mitglieder"
        }

        when {
            loading -> {
                div {
                    css {
                        "padding" to "20px"
                        "textAlign" to "center"
                        "color" to "#666"
                        "fontSize" to "18px"
                    }
                    +"Lade Mitglieder..."
                }
            }
            error != null -> {
                div {
                    css {
                        "padding" to "20px"
                        "textAlign" to "center"
                        "color" to "#e74c3c"
                        "backgroundColor" to "#fdeaea"
                        "border" to "1px solid #e74c3c"
                        "borderRadius" to "8px"
                        "margin" to "20px 0"
                    }
                    +error!!
                }
            }
            members.isEmpty() -> {
                div {
                    css {
                        "padding" to "20px"
                        "textAlign" to "center"
                        "color" to "#666"
                        "backgroundColor" to "#f8f9fa"
                        "border" to "1px solid #e0e0e0"
                        "borderRadius" to "8px"
                        "margin" to "20px 0"
                    }
                    +"Keine Mitglieder verfügbar"
                }
            }
            else -> {
                div {
                    css {
                        "display" to "grid"
                        "gridTemplateColumns" to "repeat(auto-fill, minmax(300px, 1fr))"
                        "gap" to "20px"
                    }
                    members.forEach { member ->
                        div {
                            css {
                                "border" to "1px solid #e0e0e0"
                                "borderRadius" to "8px"
                                "padding" to "15px"
                                "backgroundColor" to "#f9f9f9"
                                "boxShadow" to "0 2px 4px rgba(0,0,0,0.1)"
                                "transition" to "transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out"
                                "hover" to {
                                    "transform" to "translateY(-5px)"
                                    "boxShadow" to "0 5px 15px rgba(0,0,0,0.1)"
                                }
                            }
                            h3 {
                                css {
                                    "color" to "#3498db"
                                    "marginTop" to "0"
                                    "marginBottom" to "10px"
                                    "borderBottom" to "1px solid #e0e0e0"
                                    "paddingBottom" to "5px"
                                }
                                +member.username
                            }

                            p {
                                span {
                                    +"📧"
                                }
                                +" E-Mail: ${member.email}"
                            }

                            p {
                                span {
                                    +"🆔"
                                }
                                +" Person-ID: ${member.personId}"
                            }

                            // Status indicators
                            val statusList = mutableListOf<String>()
                            if (member.istAktiv) statusList.add("Aktiv") else statusList.add("Inaktiv")
                            if (member.istEmailVerifiziert) statusList.add("E-Mail verifiziert")
                            if (member.isLocked()) statusList.add("Gesperrt")
                            if (member.canLogin()) statusList.add("Kann sich anmelden")

                            p {
                                span {
                                    +"ℹ️"
                                }
                                +" Status: ${statusList.joinToString(", ")}"
                            }

                            // Failed login attempts
                            if (member.fehlgeschlageneAnmeldungen > 0) {
                                p {
                                    span {
                                        +"⚠️"
                                    }
                                    +" Fehlgeschlagene Anmeldungen: ${member.fehlgeschlageneAnmeldungen}"
                                }
                            }

                            // Last login
                            member.letzteAnmeldung?.let { lastLogin ->
                                p {
                                    span {
                                        +"🔐"
                                    }
                                    +" Letzte Anmeldung: $lastLogin"
                                }
                            }

                            // Creation date
                            p {
                                span {
                                    +"📅"
                                }
                                +" Erstellt am: ${member.createdAt}"
                            }

                            // Last update
                            p {
                                span {
                                    +"🔄"
                                }
                                +" Zuletzt geändert: ${member.updatedAt}"
                            }
                        }
                    }
                }
            }
        }
    }
}
