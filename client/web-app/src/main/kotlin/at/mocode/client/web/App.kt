package at.mocode.client.web

import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PingResponse(val status: String)

@Composable
fun App() {
    var responseStatus by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val httpClient = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    Div({
        style {
            fontFamily("Arial, sans-serif")
            padding(20.px)
            maxWidth(800.px)
            margin("0 auto")
        }
    }) {
        H1({
            style {
                color(Color.darkblue)
                textAlign("center")
                marginBottom(30.px)
            }
        }) {
            Text("Meldestelle - Reitersport Management")
        }

        Div({
            style {
                textAlign("center")
                marginBottom(20.px)
            }
        }) {
            P { Text("Welcome to the Meldestelle Web Application") }
            P { Text("Click the button below to test the backend connection") }
        }

        Div({
            style {
                textAlign("center")
                marginBottom(20.px)
            }
        }) {
            Button({
                style {
                    backgroundColor(Color.lightblue)
                    color(Color.white)
                    border(0.px)
                    padding(10.px, 20.px)
                    fontSize(16.px)
                    cursor("pointer")
                    borderRadius(5.px)
                }
                onClick {
                    scope.launch {
                        try {
                            isLoading = true
                            errorMessage = null
                            responseStatus = null

                            // Try different potential gateway URLs with correct routing
                            val gatewayUrls = listOf(
                                "http://localhost:8080/api/ping/ping",  // Correct gateway path
                                "http://localhost:8080/ping",            // Direct service call (fallback)
                                "http://localhost:8081/api/ping/ping"    // Alternative gateway port
                            )

                            var success = false
                            for (url in gatewayUrls) {
                                try {
                                    val response: HttpResponse = httpClient.get(url)
                                    val responseText = response.bodyAsText()

                                    // Try to parse as JSON first
                                    try {
                                        val pingResponse = Json.decodeFromString<PingResponse>(responseText)
                                        responseStatus = pingResponse.status
                                        success = true
                                        break
                                    } catch (e: Exception) {
                                        // If JSON parsing fails, use the raw response
                                        responseStatus = responseText
                                        success = true
                                        break
                                    }
                                } catch (e: Exception) {
                                    // Continue to next URL
                                    continue
                                }
                            }

                            if (!success) {
                                errorMessage = "Could not reach any backend service. Please ensure the backend is running."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
                disabled(isLoading)
            }) {
                Text(if (isLoading) "Loading..." else "Ping Backend")
            }
        }

        // Response display area
        Div({
            style {
                textAlign("center")
                marginTop(20.px)
                minHeight(100.px)
                border(1.px, LineStyle.Solid, Color.lightgray)
                borderRadius(5.px)
                padding(20.px)
                backgroundColor(Color.lightyellow)
            }
        }) {
            when {
                isLoading -> {
                    P { Text("Sending request to backend...") }
                }
                errorMessage != null -> {
                    P({
                        style {
                            color(Color.red)
                            fontWeight("bold")
                        }
                    }) {
                        Text(errorMessage!!)
                    }
                }
                responseStatus != null -> {
                    P({
                        style {
                            color(Color.green)
                            fontWeight("bold")
                            fontSize(18.px)
                        }
                    }) {
                        Text("Backend Response: $responseStatus")
                    }
                }
                else -> {
                    P { Text("Click the button above to test backend connection") }
                }
            }
        }
    }
}
