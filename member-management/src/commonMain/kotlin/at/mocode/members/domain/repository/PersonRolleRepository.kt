package at.mocode.members.domain.repository

import at.mocode.members.domain.model.DomPersonRolle
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * Repository-Interface für die Verwaltung von Person-Rolle-Zuordnungen.
 *
 * Definiert die Operationen für das Erstellen, Lesen, Aktualisieren und Löschen
 * von Person-Rolle-Beziehungen im System.
 */
interface PersonRolleRepository {

    /**
     * Speichert eine Person-Rolle-Zuordnung (erstellen oder aktualisieren).
     *
     * @param personRolle Die zu speichernde Person-Rolle-Zuordnung.
     * @return Die gespeicherte Person-Rolle-Zuordnung mit aktualisierten Zeitstempeln.
     */
    suspend fun save(personRolle: DomPersonRolle): DomPersonRolle

    /**
     * Sucht eine Person-Rolle-Zuordnung anhand ihrer ID.
     *
     * @param personRolleId Die eindeutige ID der Person-Rolle-Zuordnung.
     * @return Die gefundene Person-Rolle-Zuordnung oder null, falls nicht vorhanden.
     */
    suspend fun findById(personRolleId: Uuid): DomPersonRolle?

    /**
     * Sucht alle Rollen einer bestimmten Person.
     *
     * @param personId Die eindeutige ID der Person.
     * @param nurAktive Wenn true, werden nur aktive Zuordnungen zurückgegeben.
     * @return Liste der Person-Rolle-Zuordnungen.
     */
    suspend fun findByPersonId(personId: Uuid, nurAktive: Boolean = true): List<DomPersonRolle>

    /**
     * Sucht alle Personen mit einer bestimmten Rolle.
     *
     * @param rolleId Die eindeutige ID der Rolle.
     * @param nurAktive Wenn true, werden nur aktive Zuordnungen zurückgegeben.
     * @return Liste der Person-Rolle-Zuordnungen.
     */
    suspend fun findByRolleId(rolleId: Uuid, nurAktive: Boolean = true): List<DomPersonRolle>

    /**
     * Sucht alle Person-Rolle-Zuordnungen für einen bestimmten Verein.
     *
     * @param vereinId Die eindeutige ID des Vereins.
     * @param nurAktive Wenn true, werden nur aktive Zuordnungen zurückgegeben.
     * @return Liste der Person-Rolle-Zuordnungen.
     */
    suspend fun findByVereinId(vereinId: Uuid, nurAktive: Boolean = true): List<DomPersonRolle>

    /**
     * Sucht eine spezifische Person-Rolle-Zuordnung.
     *
     * @param personId Die eindeutige ID der Person.
     * @param rolleId Die eindeutige ID der Rolle.
     * @param vereinId Die eindeutige ID des Vereins (optional).
     * @return Die gefundene Person-Rolle-Zuordnung oder null, falls nicht vorhanden.
     */
    suspend fun findByPersonAndRolle(personId: Uuid, rolleId: Uuid, vereinId: Uuid? = null): DomPersonRolle?

    /**
     * Sucht alle Person-Rolle-Zuordnungen, die zu einem bestimmten Datum gültig sind.
     *
     * @param stichtag Das Datum, für das die Gültigkeit geprüft werden soll.
     * @param nurAktive Wenn true, werden nur aktive Zuordnungen zurückgegeben.
     * @return Liste der gültigen Person-Rolle-Zuordnungen.
     */
    suspend fun findValidAt(stichtag: LocalDate, nurAktive: Boolean = true): List<DomPersonRolle>

    /**
     * Sucht alle Person-Rolle-Zuordnungen einer Person, die zu einem bestimmten Datum gültig sind.
     *
     * @param personId Die eindeutige ID der Person.
     * @param stichtag Das Datum, für das die Gültigkeit geprüft werden soll.
     * @param nurAktive Wenn true, werden nur aktive Zuordnungen zurückgegeben.
     * @return Liste der gültigen Person-Rolle-Zuordnungen.
     */
    suspend fun findByPersonValidAt(personId: Uuid, stichtag: LocalDate, nurAktive: Boolean = true): List<DomPersonRolle>

    /**
     * Deaktiviert eine Person-Rolle-Zuordnung.
     *
     * @param personRolleId Die ID der zu deaktivierenden Person-Rolle-Zuordnung.
     * @return true, wenn die Deaktivierung erfolgreich war, false sonst.
     */
    suspend fun deactivatePersonRolle(personRolleId: Uuid): Boolean

    /**
     * Löscht eine Person-Rolle-Zuordnung permanent.
     *
     * @param personRolleId Die ID der zu löschenden Person-Rolle-Zuordnung.
     * @return true, wenn das Löschen erfolgreich war, false sonst.
     */
    suspend fun deletePersonRolle(personRolleId: Uuid): Boolean

    /**
     * Prüft, ob eine Person eine bestimmte Rolle hat.
     *
     * @param personId Die eindeutige ID der Person.
     * @param rolleId Die eindeutige ID der Rolle.
     * @param vereinId Die eindeutige ID des Vereins (optional).
     * @param stichtag Das Datum, für das die Gültigkeit geprüft werden soll (optional, default: heute).
     * @return true, wenn die Person die Rolle hat, false sonst.
     */
    suspend fun hasPersonRolle(personId: Uuid, rolleId: Uuid, vereinId: Uuid? = null, stichtag: LocalDate? = null): Boolean
}
