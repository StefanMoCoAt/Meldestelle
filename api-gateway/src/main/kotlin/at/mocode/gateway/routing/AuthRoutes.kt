package at.mocode.gateway.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Authentication routes for the API Gateway.
 *
 * Provides endpoints for user login, logout, registration, and profile management.
 * This is a simplified implementation that will be connected to the actual
 * authentication services once the database layer is implemented.
 */

/**
 * Data classes for API requests and responses
 */
@Serializable
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null,
    val user: UserProfileResponse? = null
)

@Serializable
data class RegisterRequest(
    val personId: String, // UUID as string
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserProfileResponse? = null,
    val errors: List<ValidationErrorResponse>? = null
)

@Serializable
data class ValidationErrorResponse(
    val field: String,
    val message: String
)

@Serializable
data class UserProfileResponse(
    val userId: String,
    val username: String,
    val email: String,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val lastLogin: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ChangePasswordResponse(
    val success: Boolean,
    val message: String? = null,
    val errors: List<ValidationErrorResponse>? = null
)

/**
 * Configures authentication routes
 */
fun Route.authRoutes(
    authenticationService: at.mocode.members.domain.service.AuthenticationService,
    jwtService: at.mocode.members.domain.service.JwtService
) {
    route("/auth") {

        // Login endpoint
        post("/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()

                // Validate input
                if (loginRequest.usernameOrEmail.isEmpty() || loginRequest.password.isEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        LoginResponse(
                            success = false,
                            message = "Username/email and password are required"
                        )
                    )
                    return@post
                }

                // Authenticate user
                val authResult = authenticationService.authenticate(
                    loginRequest.usernameOrEmail,
                    loginRequest.password
                )

                if (authResult.isSuccess) {
                    val user = authResult.user!!
                    val tokenInfo = authResult.tokenInfo!!

                    call.respond(
                        HttpStatusCode.OK,
                        LoginResponse(
                            success = true,
                            token = tokenInfo.token,
                            message = "Login successful",
                            user = UserProfileResponse(
                                userId = user.userId.toString(),
                                username = user.username,
                                email = user.email,
                                isActive = user.istAktiv,
                                isEmailVerified = user.istEmailVerifiziert,
                                lastLogin = user.letzteAnmeldung?.toString()
                            )
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        LoginResponse(
                            success = false,
                            message = authResult.errorMessage ?: "Invalid credentials"
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    LoginResponse(
                        success = false,
                        message = "Invalid request: ${e.message}"
                    )
                )
            }
        }

        // Register endpoint
        post("/register") {
            try {
                val registerRequest = call.receive<RegisterRequest>()

                // TODO: Implement actual registration logic
                // For now, return a mock response
                if (registerRequest.username.isNotEmpty() &&
                    registerRequest.email.isNotEmpty() &&
                    registerRequest.password.length >= 8) {

                    call.respond(
                        HttpStatusCode.Created,
                        RegisterResponse(
                            success = true,
                            message = "User registered successfully",
                            user = UserProfileResponse(
                                userId = "mock-user-id-${System.currentTimeMillis()}",
                                username = registerRequest.username,
                                email = registerRequest.email,
                                isActive = true,
                                isEmailVerified = false,
                                lastLogin = null
                            )
                        )
                    )
                } else {
                    val errors = mutableListOf<ValidationErrorResponse>()
                    if (registerRequest.username.isEmpty()) {
                        errors.add(ValidationErrorResponse("username", "Username is required"))
                    }
                    if (registerRequest.email.isEmpty()) {
                        errors.add(ValidationErrorResponse("email", "Email is required"))
                    }
                    if (registerRequest.password.length < 8) {
                        errors.add(ValidationErrorResponse("password", "Password must be at least 8 characters"))
                    }

                    call.respond(
                        HttpStatusCode.BadRequest,
                        RegisterResponse(
                            success = false,
                            message = "Registration failed",
                            errors = errors
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    RegisterResponse(
                        success = false,
                        message = "Invalid request: ${e.message}"
                    )
                )
            }
        }

        // Protected routes (require authentication)
        authenticate("auth-jwt") {

            // Get user profile
            get("/profile") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim("userId", String::class)

                    if (userId != null) {
                        // TODO: Fetch actual user data from database
                        call.respond(
                            HttpStatusCode.OK,
                            UserProfileResponse(
                                userId = userId,
                                username = "mock_user",
                                email = "mock@example.com",
                                isActive = true,
                                isEmailVerified = true,
                                lastLogin = null
                            )
                        )
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error retrieving profile: ${e.message}")
                }
            }

            // Change password
            post("/change-password") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim("userId", String::class)

                    if (userId != null) {
                        val changePasswordRequest = call.receive<ChangePasswordRequest>()

                        // TODO: Implement actual password change logic
                        if (changePasswordRequest.newPassword.length >= 8) {
                            call.respond(
                                HttpStatusCode.OK,
                                ChangePasswordResponse(
                                    success = true,
                                    message = "Password changed successfully"
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ChangePasswordResponse(
                                    success = false,
                                    message = "Password change failed",
                                    errors = listOf(
                                        ValidationErrorResponse("newPassword", "Password must be at least 8 characters")
                                    )
                                )
                            )
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ChangePasswordResponse(
                            success = false,
                            message = "Invalid request: ${e.message}"
                        )
                    )
                }
            }

            // Refresh token
            post("/refresh") {
                try {
                    val token = call.request.header("Authorization")?.removePrefix("Bearer ")
                    if (token != null) {
                        // TODO: Implement actual token refresh logic
                        call.respond(
                            HttpStatusCode.OK,
                            mapOf(
                                "token" to "refreshed_mock_jwt_token_${System.currentTimeMillis()}",
                                "message" to "Token refreshed successfully"
                            )
                        )
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "No token provided")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error refreshing token: ${e.message}")
                }
            }

            // Logout (client-side token invalidation)
            post("/logout") {
                // In a stateless JWT system, logout is typically handled client-side
                // by removing the token. For server-side logout, you would need a token blacklist.
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Logged out successfully. Please remove the token from client storage.")
                )
            }
        }
    }
}
