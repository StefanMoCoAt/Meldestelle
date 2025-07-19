package at.mocode.members.domain.repository

import at.mocode.members.domain.model.DomBerechtigung
import at.mocode.members.domain.model.DomRolleBerechtigung
import com.benasher44.uuid.Uuid

/**
 * Repository-Interface für die Verwaltung von Rolle-Berechtigung-Zuordnungen.
 *
 * Definiert die Operationen für das Erstellen, Lesen, Aktualisieren und Löschen
 * von Rolle-Berechtigung-Beziehungen im System.
 */
interface RolleBerechtigungRepository {

    /**
     * Speichert eine Rolle-Berechtigung-Zuordnung (erstellen oder aktualisieren).
     *
     * @param rolleBerechtigung Die zu speichernde Rolle-Berechtigung-Zuordnung.
     * @return Die gespeicherte Rolle-Berechtigung-Zuordnung mit aktualisierten Zeitstempeln.
     */
    suspend fun save(rolleBerechtigung: DomRolleBerechtigung): DomRolleBerechtigung

    /**
     * Sucht eine Rolle-Berechtigung-Zuordnung anhand ihrer ID.
     *
     * @param rolleBerechtigungId Die eindeutige ID der Rolle-Berechtigung-Zuordnung.
     * @return Die gefundene Rolle-Berechtigung-Zuordnung oder null, falls nicht vorhanden.
     */
    suspend fun findById(rolleBerechtigungId: Uuid): DomRolleBerechtigung?

    /**
     * Sucht alle Berechtigungen einer bestimmten Rolle.
     *
     * @param rolleId Die eindeutige ID der Rolle.
     * @param nurAktive Wenn true, werden nur aktive Zuordnungen zurückgegeben.
     * @return Liste der Rolle-Berechtigung-Zuordnungen.
     */
    suspend fun findByRolleId(rolleId: Uuid, nurAktive: Boolean = true): List<DomRolleBerechtigung>

    /**
     * Sucht alle Rollen mit einer bestimmten Berechtigung.
     *
     * @param berechtigungId Die eindeutige ID der Berechtigung.
     * @param nurAktive Wenn true, werden nur aktive Zuordnungen zurückgegeben.
     * @return Liste der Rolle-Berechtigung-Zuordnungen.
     */
    suspend fun findByBerechtigungId(berechtigungId: Uuid, nurAktive: Boolean = true): List<DomRolleBerechtigung>

    /**
     * Sucht eine spezifische Rolle-Berechtigung-Zuordnung.
     *
     * @param rolleId Die eindeutige ID der Rolle.
     * @param berechtigungId Die eindeutige ID der Berechtigung.
     * @return Die gefundene Rolle-Berechtigung-Zuordnung oder null, falls nicht vorhanden.
     */
    suspend fun findByRolleAndBerechtigung(rolleId: Uuid, berechtigungId: Uuid): DomRolleBerechtigung?

    /**
     * Gibt alle aktiven Rolle-Berechtigung-Zuordnungen zurück.
     *
     * @return Liste aller aktiven Rolle-Berechtigung-Zuordnungen.
     */
    suspend fun findAllActive(): List<DomRolleBerechtigung>

    /**
     * Gibt alle Rolle-Berechtigung-Zuordnungen zurück (aktive und inaktive).
     *
     * @return Liste aller Rolle-Berechtigung-Zuordnungen.
     */
    suspend fun findAll(): List<DomRolleBerechtigung>

    /**
     * Deaktiviert eine Rolle-Berechtigung-Zuordnung.
     *
     * @param rolleBerechtigungId Die ID der zu deaktivierenden Rolle-Berechtigung-Zuordnung.
     * @return true, wenn die Deaktivierung erfolgreich war, false sonst.
     */
    suspend fun deactivateRolleBerechtigung(rolleBerechtigungId: Uuid): Boolean

    /**
     * Löscht eine Rolle-Berechtigung-Zuordnung permanent.
     *
     * @param rolleBerechtigungId Die ID der zu löschenden Rolle-Berechtigung-Zuordnung.
     * @return true, wenn das Löschen erfolgreich war, false sonst.
     */
    suspend fun deleteRolleBerechtigung(rolleBerechtigungId: Uuid): Boolean

    /**
     * Prüft, ob eine Rolle eine bestimmte Berechtigung hat.
     *
     * @param rolleId Die eindeutige ID der Rolle.
     * @param berechtigungId Die eindeutige ID der Berechtigung.
     * @return true, wenn die Rolle die Berechtigung hat, false sonst.
     */
    suspend fun hasRolleBerechtigung(rolleId: Uuid, berechtigungId: Uuid): Boolean

    /**
     * Weist einer Rolle eine Berechtigung zu.
     *
     * @param rolleId Die eindeutige ID der Rolle.
     * @param berechtigungId Die eindeutige ID der Berechtigung.
     * @param zugewiesenVon Die ID der Person, die die Zuweisung vornimmt (optional).
     * @return Die erstellte Rolle-Berechtigung-Zuordnung.
     */
    suspend fun assignBerechtigungToRolle(rolleId: Uuid, berechtigungId: Uuid, zugewiesenVon: Uuid? = null): DomRolleBerechtigung

    /**
     * Entzieht einer Rolle eine Berechtigung.
     *
     * @param rolleId Die eindeutige ID der Rolle.
     * @param berechtigungId Die eindeutige ID der Berechtigung.
     * @return true, wenn die Berechtigung erfolgreich entzogen wurde, false sonst.
     */
    suspend fun revokeBerechtigungFromRolle(rolleId: Uuid, berechtigungId: Uuid): Boolean
}
