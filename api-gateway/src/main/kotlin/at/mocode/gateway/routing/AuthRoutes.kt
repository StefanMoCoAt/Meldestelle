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

                when (authResult) {
                    is at.mocode.members.domain.service.AuthenticationService.AuthResult.Success -> {
                        call.respond(
                            HttpStatusCode.OK,
                            LoginResponse(
                                success = true,
                                token = authResult.token,
                                message = "Login successful",
                                user = UserProfileResponse(
                                    userId = authResult.user.userId.toString(),
                                    username = authResult.user.username,
                                    email = authResult.user.email,
                                    isActive = authResult.user.istAktiv,
                                    isEmailVerified = authResult.user.istEmailVerifiziert,
                                    lastLogin = authResult.user.letzteAnmeldung?.toString()
                                )
                            )
                        )
                    }
                    is at.mocode.members.domain.service.AuthenticationService.AuthResult.Failure -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            LoginResponse(
                                success = false,
                                message = authResult.reason
                            )
                        )
                    }
                    is at.mocode.members.domain.service.AuthenticationService.AuthResult.Locked -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            LoginResponse(
                                success = false,
                                message = "Account ist gesperrt bis ${authResult.lockedUntil}"
                            )
                        )
                    }
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

                // Validate input
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
                if (registerRequest.personId.isEmpty()) {
                    errors.add(ValidationErrorResponse("personId", "Person ID is required"))
                }

                if (errors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        RegisterResponse(
                            success = false,
                            message = "Registration failed",
                            errors = errors
                        )
                    )
                    return@post
                }

                // Parse personId
                val personId = try {
                    com.benasher44.uuid.Uuid.fromString(registerRequest.personId)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        RegisterResponse(
                            success = false,
                            message = "Invalid person ID format",
                            errors = listOf(ValidationErrorResponse("personId", "Invalid UUID format"))
                        )
                    )
                    return@post
                }

                // Register user
                val registerResult = authenticationService.registerUser(
                    registerRequest.username,
                    registerRequest.email,
                    registerRequest.password,
                    personId
                )

                when (registerResult) {
                    is at.mocode.members.domain.service.AuthenticationService.RegisterResult.Success -> {
                        call.respond(
                            HttpStatusCode.Created,
                            RegisterResponse(
                                success = true,
                                message = "User registered successfully",
                                user = UserProfileResponse(
                                    userId = registerResult.user.userId.toString(),
                                    username = registerResult.user.username,
                                    email = registerResult.user.email,
                                    isActive = registerResult.user.istAktiv,
                                    isEmailVerified = registerResult.user.istEmailVerifiziert,
                                    lastLogin = registerResult.user.letzteAnmeldung?.toString()
                                )
                            )
                        )
                    }
                    is at.mocode.members.domain.service.AuthenticationService.RegisterResult.Failure -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            RegisterResponse(
                                success = false,
                                message = registerResult.reason
                            )
                        )
                    }
                    is at.mocode.members.domain.service.AuthenticationService.RegisterResult.WeakPassword -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            RegisterResponse(
                                success = false,
                                message = "Password is too weak",
                                errors = registerResult.issues.map {
                                    ValidationErrorResponse("password", it)
                                }
                            )
                        )
                    }
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
                    val userIdString = principal?.subject

                    if (userIdString != null) {
                        val userId = try {
                            com.benasher44.uuid.Uuid.fromString(userIdString)
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.Unauthorized, "Invalid token format")
                            return@get
                        }

                        // Fetch actual user data from database
                        val userRepository = at.mocode.members.infrastructure.repository.UserRepositoryImpl()
                        val user = userRepository.findById(userId)

                        if (user != null) {
                            call.respond(
                                HttpStatusCode.OK,
                                UserProfileResponse(
                                    userId = user.userId.toString(),
                                    username = user.username,
                                    email = user.email,
                                    isActive = user.istAktiv,
                                    isEmailVerified = user.istEmailVerifiziert,
                                    lastLogin = user.letzteAnmeldung?.toString()
                                )
                            )
                        } else {
                            call.respond(HttpStatusCode.NotFound, "User not found")
                        }
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
                    val userIdString = principal?.subject

                    if (userIdString != null) {
                        val userId = try {
                            com.benasher44.uuid.Uuid.fromString(userIdString)
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.Unauthorized, "Invalid token format")
                            return@post
                        }

                        val changePasswordRequest = call.receive<ChangePasswordRequest>()

                        // Validate input
                        if (changePasswordRequest.currentPassword.isEmpty()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ChangePasswordResponse(
                                    success = false,
                                    message = "Current password is required",
                                    errors = listOf(ValidationErrorResponse("currentPassword", "Current password is required"))
                                )
                            )
                            return@post
                        }

                        if (changePasswordRequest.newPassword.length < 8) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ChangePasswordResponse(
                                    success = false,
                                    message = "New password must be at least 8 characters",
                                    errors = listOf(ValidationErrorResponse("newPassword", "Password must be at least 8 characters"))
                                )
                            )
                            return@post
                        }

                        // Change password using AuthenticationService
                        val changeResult = authenticationService.changePassword(
                            userId,
                            changePasswordRequest.currentPassword,
                            changePasswordRequest.newPassword
                        )

                        when (changeResult) {
                            is at.mocode.members.domain.service.AuthenticationService.PasswordChangeResult.Success -> {
                                call.respond(
                                    HttpStatusCode.OK,
                                    ChangePasswordResponse(
                                        success = true,
                                        message = "Password changed successfully"
                                    )
                                )
                            }
                            is at.mocode.members.domain.service.AuthenticationService.PasswordChangeResult.Failure -> {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ChangePasswordResponse(
                                        success = false,
                                        message = changeResult.reason
                                    )
                                )
                            }
                            is at.mocode.members.domain.service.AuthenticationService.PasswordChangeResult.WeakPassword -> {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ChangePasswordResponse(
                                        success = false,
                                        message = "Password is too weak",
                                        errors = changeResult.issues.map {
                                            ValidationErrorResponse("newPassword", it)
                                        }
                                    )
                                )
                            }
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
                        // Validate the current token
                        val tokenInfo = jwtService.validateToken(token)
                        if (tokenInfo != null) {
                            // Get user from database to ensure they're still active
                            val userRepository = at.mocode.members.infrastructure.repository.UserRepositoryImpl()
                            val user = userRepository.findById(tokenInfo.userId)

                            if (user != null && user.canLogin()) {
                                // Create a new token
                                val newToken = jwtService.createToken(user)

                                call.respond(
                                    HttpStatusCode.OK,
                                    mapOf(
                                        "token" to newToken,
                                        "message" to "Token refreshed successfully"
                                    )
                                )
                            } else {
                                call.respond(
                                    HttpStatusCode.Unauthorized,
                                    mapOf("message" to "User is no longer active or account is locked")
                                )
                            }
                        } else {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                mapOf("message" to "Invalid or expired token")
                            )
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "No token provided"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Error refreshing token: ${e.message}"))
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
