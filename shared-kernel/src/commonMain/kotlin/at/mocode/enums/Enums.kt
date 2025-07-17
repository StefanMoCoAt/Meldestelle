package at.mocode.enums

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
