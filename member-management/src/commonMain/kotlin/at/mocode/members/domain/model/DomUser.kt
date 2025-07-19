package at.mocode.members.domain.model

import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen Benutzer im System.
 *
 * Ein Benutzer ist mit einer Person verknüpft und hat Anmeldedaten für den Zugriff auf das System.
 *
 * @property userId Eindeutiger interner Identifikator für diesen Benutzer (UUID).
 * @property personId ID der zugehörigen Person.
 * @property username Benutzername für die Anmeldung.
 * @property email E-Mail-Adresse des Benutzers.
 * @property passwordHash Hash des Passworts.
 * @property salt Salt für das Password-Hashing.
 * @property istAktiv Gibt an, ob dieser Benutzer aktiv ist.
 * @property istEmailVerifiziert Gibt an, ob die E-Mail-Adresse verifiziert wurde.
 * @property fehlgeschlageneAnmeldungen Anzahl fehlgeschlagener Anmeldeversuche.
 * @property gesperrtBis Zeitpunkt, bis zu dem der Account gesperrt ist (null, wenn nicht gesperrt).
 * @property letzteAnmeldung Zeitpunkt der letzten erfolgreichen Anmeldung.
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
    var fehlgeschlageneAnmeldungen: Int = 0,

    @Serializable(with = KotlinInstantSerializer::class)
    var gesperrtBis: Instant? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    var letzteAnmeldung: Instant? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),

    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
) {
    /**
     * Prüft, ob der Benutzeraccount gesperrt ist.
     *
     * @return true, wenn der Account gesperrt ist, false sonst.
     */
    fun isLocked(): Boolean {
        val now = Clock.System.now()
        return gesperrtBis != null && now < gesperrtBis!!
    }

    /**
     * Prüft, ob der Benutzer anmelden kann (aktiv und nicht gesperrt).
     *
     * @return true, wenn der Benutzer sich anmelden kann, false sonst.
     */
    fun canLogin(): Boolean {
        return istAktiv && !isLocked()
    }
}
