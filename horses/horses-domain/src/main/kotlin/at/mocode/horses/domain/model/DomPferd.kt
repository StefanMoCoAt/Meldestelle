package at.mocode.horses.domain.model

import at.mocode.core.domain.model.PferdeGeschlechtE
import at.mocode.core.domain.model.DatenQuelleE
import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

/**
 * Domain model representing a horse in the registry system.
 *
 * This entity contains all essential information about a horse including
 * identification, ownership, breeding information, and administrative data.
 * It serves as the core aggregate root for the horse-registry bounded context.
 *
 * @property pferdId Unique internal identifier for this horse (UUID).
 * @property pferdeName Name of the horse.
 * @property geschlecht Gender of the horse (Hengst, Stute, Wallach).
 * @property geburtsdatum Birth date of the horse.
 * @property rasse Breed of the horse.
 * @property farbe Color/coat of the horse.
 * @property besitzerId ID of the current owner (Person from member-management context).
 * @property verantwortlichePersonId ID of the responsible person (trainer, rider, etc.).
 * @property zuechterName Name of the breeder.
 * @property zuchtbuchNummer Studbook number if registered.
 * @property lebensnummer Life number (unique identification number).
 * @property chipNummer Microchip number for identification.
 * @property passNummer Passport number.
 * @property oepsNummer OEPS (Austrian Equestrian Federation) number.
 * @property feiNummer FEI (International Equestrian Federation) number.
 * @property vaterName Name of the sire (father).
 * @property mutterName Name of the dam (mother).
 * @property mutterVaterName Name of the maternal grandsire.
 * @property stockmass Height of the horse in cm.
 * @property istAktiv Whether the horse is currently active in the system.
 * @property bemerkungen Additional notes or comments.
 * @property datenQuelle Source of the data (manual entry, import, etc.).
 * @property createdAt Timestamp when this record was created.
 * @property updatedAt Timestamp when this record was last updated.
 */
@Serializable
data class DomPferd(
    @Serializable(with = UuidSerializer::class)
    val pferdId: Uuid = uuid4(),

    // Basic Information
    var pferdeName: String,
    var geschlecht: PferdeGeschlechtE,
    var geburtsdatum: LocalDate? = null,
    var rasse: String? = null,
    var farbe: String? = null,

    // Ownership and Responsibility
    @Serializable(with = UuidSerializer::class)
    var besitzerId: Uuid? = null,
    @Serializable(with = UuidSerializer::class)
    var verantwortlichePersonId: Uuid? = null,

    // Breeding Information
    var zuechterName: String? = null,
    var zuchtbuchNummer: String? = null,

    // Identification Numbers
    var lebensnummer: String? = null,
    var chipNummer: String? = null,
    var passNummer: String? = null,
    var oepsNummer: String? = null,
    var feiNummer: String? = null,

    // Pedigree Information
    var vaterName: String? = null,
    var mutterName: String? = null,
    var mutterVaterName: String? = null,

    // Physical Characteristics
    var stockmass: Int? = null, // Height in cm

    // Status and Administrative
    var istAktiv: Boolean = true,
    var bemerkungen: String? = null,
    var datenQuelle: DatenQuelleE = DatenQuelleE.MANUELL,

    // Audit Fields
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
) {
    /**
     * Returns the display name for the horse, combining name and birth year if available.
     */
    fun getDisplayName(): String {
        return geburtsdatum?.let { birthDate ->
            "$pferdeName (${birthDate.year})"
        } ?: pferdeName
    }

    /**
     * Checks if the horse has complete identification information.
     */
    fun hasCompleteIdentification(): Boolean {
        return !lebensnummer.isNullOrBlank() ||
               !chipNummer.isNullOrBlank() ||
               !passNummer.isNullOrBlank()
    }

    /**
     * Checks if the horse is registered with OEPS.
     */
    fun isOepsRegistered(): Boolean {
        return !oepsNummer.isNullOrBlank()
    }

    /**
     * Checks if the horse is registered with FEI.
     */
    fun isFeiRegistered(): Boolean {
        return !feiNummer.isNullOrBlank()
    }

    /**
     * Returns the age of the horse in years, or null if birth date is unknown.
     */
    fun getAge(): Int? {
        return geburtsdatum?.let { birthDate ->
            val today = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
            var age = today.year - birthDate.year

            // Check if birthday has occurred this year
            if (today.monthNumber < birthDate.monthNumber ||
                (today.monthNumber == birthDate.monthNumber && today.dayOfMonth < birthDate.dayOfMonth)) {
                age--
            }

            age
        }
    }

    /**
     * Validates that required fields are present for horse registration.
     */
    fun validateForRegistration(): List<String> {
        val errors = mutableListOf<String>()

        if (pferdeName.isBlank()) {
            errors.add("Horse name is required")
        }

        if (!hasCompleteIdentification()) {
            errors.add("At least one identification number (life number, chip number, or passport number) is required")
        }

        if (besitzerId == null) {
            errors.add("Owner is required")
        }

        return errors
    }

    /**
     * Creates a copy of this horse with updated timestamp.
     */
    fun withUpdatedTimestamp(): DomPferd {
        return this.copy(updatedAt = Clock.System.now())
    }
}
