package at.mocode.client.common.repository

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Simplified Person data class for client-side use.
 * This is a client-side representation of the DomPerson entity from the domain model.
 */
@Serializable
data class Person(
    val id: String = "",
    val nachname: String,
    val vorname: String,
    val titel: String? = null,
    val oepsSatzNr: String? = null,
    val geburtsdatum: LocalDate? = null,
    val geschlecht: String? = null,
    val telefon: String? = null,
    val email: String? = null,
    val strasse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    val adresszusatz: String? = null,
    val feiId: String? = null,
    val mitgliedsNummer: String? = null,
    val istGesperrt: Boolean = false,
    val sperrGrund: String? = null,
    val notizen: String? = null,
    val datenQuelle: String = "MANUELL",
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Returns the full name of the person, including title if available.
     */
    fun getFullName(): String {
        return buildString {
            titel?.let { append("$it ") }
            append("$vorname $nachname")
        }
    }

    /**
     * Returns a display-friendly representation of the address.
     */
    fun getFormattedAddress(): String? {
        if (strasse == null || plz == null || ort == null) return null

        return buildString {
            append(strasse)
            adresszusatz?.let { append(", $it") }
            append(", $plz $ort")
        }
    }
}
