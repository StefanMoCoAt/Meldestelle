package at.mocode.clients.pingfeature.model

import kotlinx.serialization.Serializable

/**
 * Local copy of RolleE enum for multiplatform compatibility
 * Mirrors the original from infrastructure:auth:auth-client
 */
@Serializable
enum class RolleE {
    ADMIN,              // System administrator
    VEREINS_ADMIN,      // Club administrator
    FUNKTIONAER,        // Official/functionary
    REITER,             // Rider
    TRAINER,            // Trainer
    RICHTER,            // Judge
    TIERARZT,           // Veterinarian
    ZUSCHAUER,          // Spectator
    GAST                // Guest
}

/**
 * Local copy of BerechtigungE enum for multiplatform compatibility
 * Mirrors the original from infrastructure:auth:auth-client
 */
@Serializable
enum class BerechtigungE {
    // Person management
    PERSON_READ,
    PERSON_CREATE,
    PERSON_UPDATE,
    PERSON_DELETE,

    // Club management
    VEREIN_READ,
    VEREIN_CREATE,
    VEREIN_UPDATE,
    VEREIN_DELETE,

    // Event management
    VERANSTALTUNG_READ,
    VERANSTALTUNG_CREATE,
    VERANSTALTUNG_UPDATE,
    VERANSTALTUNG_DELETE,

    // Horse management
    PFERD_READ,
    PFERD_CREATE,
    PFERD_UPDATE,
    PFERD_DELETE
}
