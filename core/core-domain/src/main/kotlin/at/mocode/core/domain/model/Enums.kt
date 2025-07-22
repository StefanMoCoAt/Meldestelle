package at.mocode.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Data source enumeration - indicates where data originated from
 */
@Serializable
enum class DatenQuelleE { OEPS_ZNS, MANUELL }

/**
 * Horse gender enumeration
 */
@Serializable
enum class PferdeGeschlechtE {
    HENGST, STUTE, WALLACH, UNBEKANNT
}

/**
 * Person gender enumeration
 */
@Serializable
enum class GeschlechtE { M, W, D, UNBEKANNT }

/**
 * Sport discipline enumeration
 */
@Serializable
enum class SparteE { DRESSUR, SPRINGEN, VIELSEITIGKEIT, FAHREN, VOLTIGIEREN, WESTERN, DISTANZ, ISLAND, PFERDESPORT_SPIEL, BASIS, KOMBINIERT, SONSTIGES }

/**
 * Venue/place type enumeration
 */
@Serializable
enum class PlatzTypE { AUSTRAGUNG, VORBEREITUNG, LONGIEREN, SONSTIGES }

/**
 * User role enumeration for member management
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
 * Permission enumeration for access control
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
    PFERD_DELETE,

    // Master data management
    STAMMDATEN_READ,
    STAMMDATEN_UPDATE,

    // System administration
    SYSTEM_ADMIN,
    BENUTZER_VERWALTEN,
    ROLLEN_VERWALTEN
}
