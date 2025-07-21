package at.mocode.members.ui.components

import at.mocode.validation.ApiValidationUtils
import at.mocode.validation.ValidationError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import emotion.react.css

/**
 * Props for the LoginForm component
 */
external interface LoginFormProps : Props {
    var onLoginSuccess: (String) -> Unit
}

/**
 * Request body for login API
 */
@Serializable
private data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Response from login API
 */
@Serializable
private data class LoginResponse(
    val token: String,
    val username: String
)

/**
 * Error response from API
 */
@Serializable
private data class ErrorResponse(
    val message: String,
    val status: String
)

// Create Ktor client for API calls
private val apiClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

/**
 * React component that displays a login form with client-side validation.
 *
 * This component demonstrates how to use the existing validation utilities
 * for client-side validation before submitting the form to the server.
 */
val LoginForm = FC<LoginFormProps> { props ->
    // State management with useState
    var username by useState("")
    var password by useState("")
    var validationErrors by useState<List<ValidationError>>(emptyList())
    var serverError by useState<String?>(null)
    var isLoading by useState(false)

    // Function to handle login
    val handleLogin = {
        // Clear previous errors
        validationErrors = emptyList()
        serverError = null

        // Perform client-side validation
        val errors = ApiValidationUtils.validateLoginRequest(username, password)

        if (errors.isNotEmpty()) {
            // If validation fails, update the validationErrors state
            validationErrors = errors
        } else {
            // If validation passes, submit the form
            isLoading = true

            val scope = MainScope()
            scope.launch {
                try {
                    val response = apiClient.post("http://localhost:8080/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequest(username, password))
                    }

                    if (response.status.isSuccess()) {
                        val loginResponse: LoginResponse = response.body()
                        props.onLoginSuccess(loginResponse.token)
                    } else {
                        val errorResponse: ErrorResponse = response.body()
                        serverError = errorResponse.message
                    }
                } catch (e: Exception) {
                    serverError = "Login failed: ${e.message}"
                    console.error("Login error:", e)
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Helper function to get validation error for a field
    val getFieldError = { fieldName: String ->
        validationErrors.find { it.field == fieldName }?.message
    }

    // Render the form
    div {
        css {
            "maxWidth" to "400px"
            "margin" to "0 auto"
            "padding" to "20px"
            "backgroundColor" to "#f9f9f9"
            "borderRadius" to "8px"
            "boxShadow" to "0 2px 4px rgba(0,0,0,0.1)"
        }

        h2 {
            css {
                "textAlign" to "center"
                "color" to "#2c3e50"
                "marginBottom" to "20px"
            }
            +"Login"
        }

        // Display server error if any
        serverError?.let {
            div {
                css {
                    "backgroundColor" to "#fdeaea"
                    "color" to "#e74c3c"
                    "padding" to "10px"
                    "borderRadius" to "4px"
                    "marginBottom" to "15px"
                    "textAlign" to "center"
                }
                +it
            }
        }

        form {
            // No onSubmit handler, using button click instead

            // Username field
            div {
                css {
                    "marginBottom" to "15px"
                }

                label {
                    css {
                        "display" to "block"
                        "marginBottom" to "5px"
                        "fontWeight" to "bold"
                    }
                    htmlFor = "username"
                    +"Username or Email"
                }

                input {
                    css {
                        "width" to "100%"
                        "padding" to "8px"
                        "borderRadius" to "4px"
                        "border" to if (getFieldError("username") != null) "1px solid #e74c3c" else "1px solid #ddd"
                    }
                    type = InputType.text
                    id = "username"
                    value = username
                    onChange = { event -> username = event.target.value }
                    disabled = isLoading
                    required = true
                }

                // Display validation error for username if any
                getFieldError("username")?.let {
                    p {
                        css {
                            "color" to "#e74c3c"
                            "fontSize" to "12px"
                            "margin" to "5px 0 0 0"
                        }
                        +it
                    }
                }
            }

            // Password field
            div {
                css {
                    "marginBottom" to "20px"
                }

                label {
                    css {
                        "display" to "block"
                        "marginBottom" to "5px"
                        "fontWeight" to "bold"
                    }
                    htmlFor = "password"
                    +"Password"
                }

                input {
                    css {
                        "width" to "100%"
                        "padding" to "8px"
                        "borderRadius" to "4px"
                        "border" to if (getFieldError("password") != null) "1px solid #e74c3c" else "1px solid #ddd"
                    }
                    type = InputType.password
                    id = "password"
                    value = password
                    onChange = { event -> password = event.target.value }
                    disabled = isLoading
                    required = true
                }

                // Display validation error for password if any
                getFieldError("password")?.let {
                    p {
                        css {
                            "color" to "#e74c3c"
                            "fontSize" to "12px"
                            "margin" to "5px 0 0 0"
                        }
                        +it
                    }
                }
            }

            // Submit button
            button {
                css {
                    "width" to "100%"
                    "padding" to "10px"
                    "backgroundColor" to "#3498db"
                    "color" to "white"
                    "border" to "none"
                    "borderRadius" to "4px"
                    "cursor" to if (isLoading) "not-allowed" else "pointer"
                    "opacity" to if (isLoading) "0.7" else "1"
                    "transition" to "background-color 0.3s"
                    "hover" to {
                        "backgroundColor" to if (!isLoading) "#2980b9" else "#3498db"
                    }
                }
                type = react.dom.html.ButtonType.button
                disabled = isLoading
                onClick = { handleLogin() }

                if (isLoading) {
                    +"Logging in..."
                } else {
                    +"Login"
                }
            }
        }
    }
}
