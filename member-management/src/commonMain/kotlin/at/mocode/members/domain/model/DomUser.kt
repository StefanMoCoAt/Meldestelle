package at.mocode.members.domain.model

import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen Benutzer für die Authentifizierung im System.
 *
 * Diese Entität verwaltet die Anmeldedaten und ist mit einer Person verknüpft.
 * Ein Benutzer kann sich am System anmelden und erhält basierend auf seinen
 * zugewiesenen Rollen entsprechende Berechtigungen.
 *
 * @property userId Eindeutiger interner Identifikator für diesen Benutzer (UUID).
 * @property personId Fremdschlüssel zur verknüpften Person (DomPerson.personId).
 * @property username Eindeutiger Benutzername für die Anmeldung.
 * @property email E-Mail-Adresse des Benutzers (kann auch als Login verwendet werden).
 * @property passwordHash Gehashtes Passwort des Benutzers.
 * @property salt Salt für das Passwort-Hashing.
 * @property istAktiv Gibt an, ob dieser Benutzer aktuell aktiv ist und sich anmelden kann.
 * @property istEmailVerifiziert Gibt an, ob die E-Mail-Adresse verifiziert wurde.
 * @property letzteAnmeldung Zeitstempel der letzten erfolgreichen Anmeldung.
 * @property fehlgeschlageneAnmeldungen Anzahl der fehlgeschlagenen Anmeldeversuche.
 * @property gesperrtBis Optionaler Zeitstempel bis wann der Benutzer gesperrt ist.
 * @property passwortAendernErforderlich Gibt an, ob der Benutzer sein Passwort ändern muss.
 * @property createdAt Zeitstempel der Erstellung dieses Benutzers.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Benutzers.
 */
@Serializable
data class DomUser(
    @Serializable(with = UuidSerializer::class)
    val userId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val personId: Uuid,

    var username: String,
    var email: String,
    var passwordHash: String,
    var salt: String,

    var istAktiv: Boolean = true,
    var istEmailVerifiziert: Boolean = false,

    @Serializable(with = KotlinInstantSerializer::class)
    var letzteAnmeldung: Instant? = null,

    var fehlgeschlageneAnmeldungen: Int = 0,

    @Serializable(with = KotlinInstantSerializer::class)
    var gesperrtBis: Instant? = null,

    var passwortAendernErforderlich: Boolean = false,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
