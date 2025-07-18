package at.mocode.members.domain.repository

import at.mocode.enums.RolleE
import at.mocode.members.domain.model.DomRolle
import com.benasher44.uuid.Uuid

/**
 * Repository-Interface für die Verwaltung von Rollen.
 *
 * Definiert die Operationen für das Erstellen, Lesen, Aktualisieren und Löschen
 * von Rollen im System.
 */
interface RolleRepository {

    /**
     * Erstellt eine neue Rolle im System.
     *
     * @param rolle Die zu erstellende Rolle.
     * @return Die erstellte Rolle mit aktualisierten Zeitstempeln.
     */
    suspend fun save(rolle: DomRolle): DomRolle

    /**
     * Sucht eine Rolle anhand ihrer ID.
     *
     * @param rolleId Die eindeutige ID der Rolle.
     * @return Die gefundene Rolle oder null, falls nicht vorhanden.
     */
    suspend fun findById(rolleId: Uuid): DomRolle?

    /**
     * Sucht eine Rolle anhand ihres Typs.
     *
     * @param rolleTyp Der Typ der Rolle.
     * @return Die gefundene Rolle oder null, falls nicht vorhanden.
     */
    suspend fun findByTyp(rolleTyp: RolleE): DomRolle?

    /**
     * Sucht Rollen anhand ihres Namens (Teilstring-Suche).
     *
     * @param name Der Name oder Teilname der Rolle.
     * @return Liste der gefundenen Rollen.
     */
    suspend fun findByName(name: String): List<DomRolle>

    /**
     * Gibt alle aktiven Rollen zurück.
     *
     * @return Liste aller aktiven Rollen.
     */
    suspend fun findAllActive(): List<DomRolle>

    /**
     * Gibt alle Rollen zurück (aktive und inaktive).
     *
     * @return Liste aller Rollen.
     */
    suspend fun findAll(): List<DomRolle>

    /**
     * Aktualisiert eine bestehende Rolle.
     * Note: This is handled by the save method which works for both create and update.
     *
     * @param rolle Die zu aktualisierende Rolle.
     * @return Die aktualisierte Rolle mit aktualisierten Zeitstempeln.
     */
    // suspend fun updateRolle(rolle: DomRolle): DomRolle // Handled by save method

    /**
     * Deaktiviert eine Rolle (soft delete).
     *
     * @param rolleId Die ID der zu deaktivierenden Rolle.
     * @return true, wenn die Deaktivierung erfolgreich war, false sonst.
     */
    suspend fun deactivateRolle(rolleId: Uuid): Boolean

    /**
     * Löscht eine Rolle permanent (nur für nicht-System-Rollen).
     *
     * @param rolleId Die ID der zu löschenden Rolle.
     * @return true, wenn das Löschen erfolgreich war, false sonst.
     */
    suspend fun deleteRolle(rolleId: Uuid): Boolean

    /**
     * Prüft, ob eine Rolle mit dem gegebenen Typ bereits existiert.
     *
     * @param rolleTyp Der zu prüfende Rollentyp.
     * @return true, wenn eine Rolle mit diesem Typ existiert, false sonst.
     */
    suspend fun existsByTyp(rolleTyp: RolleE): Boolean
}
