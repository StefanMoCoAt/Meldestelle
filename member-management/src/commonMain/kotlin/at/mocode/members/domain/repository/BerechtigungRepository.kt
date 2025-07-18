package at.mocode.members.domain.repository

import at.mocode.enums.BerechtigungE
import at.mocode.members.domain.model.DomBerechtigung
import com.benasher44.uuid.Uuid

/**
 * Repository-Interface für die Verwaltung von Berechtigungen.
 *
 * Definiert die Operationen für das Erstellen, Lesen, Aktualisieren und Löschen
 * von Berechtigungen im System.
 */
interface BerechtigungRepository {

    /**
     * Speichert eine Berechtigung (erstellen oder aktualisieren).
     *
     * @param berechtigung Die zu speichernde Berechtigung.
     * @return Die gespeicherte Berechtigung mit aktualisierten Zeitstempeln.
     */
    suspend fun save(berechtigung: DomBerechtigung): DomBerechtigung

    /**
     * Sucht eine Berechtigung anhand ihrer ID.
     *
     * @param berechtigungId Die eindeutige ID der Berechtigung.
     * @return Die gefundene Berechtigung oder null, falls nicht vorhanden.
     */
    suspend fun findById(berechtigungId: Uuid): DomBerechtigung?

    /**
     * Sucht eine Berechtigung anhand ihres Typs.
     *
     * @param berechtigungTyp Der Typ der Berechtigung.
     * @return Die gefundene Berechtigung oder null, falls nicht vorhanden.
     */
    suspend fun findByTyp(berechtigungTyp: BerechtigungE): DomBerechtigung?

    /**
     * Sucht Berechtigungen anhand ihres Namens (Teilstring-Suche).
     *
     * @param name Der Name oder Teilname der Berechtigung.
     * @return Liste der gefundenen Berechtigungen.
     */
    suspend fun findByName(name: String): List<DomBerechtigung>

    /**
     * Sucht Berechtigungen anhand der Ressource.
     *
     * @param ressource Die Ressource (z.B. "Person", "Verein").
     * @return Liste der gefundenen Berechtigungen.
     */
    suspend fun findByRessource(ressource: String): List<DomBerechtigung>

    /**
     * Sucht Berechtigungen anhand der Aktion.
     *
     * @param aktion Die Aktion (z.B. "lesen", "erstellen").
     * @return Liste der gefundenen Berechtigungen.
     */
    suspend fun findByAktion(aktion: String): List<DomBerechtigung>

    /**
     * Gibt alle aktiven Berechtigungen zurück.
     *
     * @return Liste aller aktiven Berechtigungen.
     */
    suspend fun findAllActive(): List<DomBerechtigung>

    /**
     * Gibt alle Berechtigungen zurück (aktive und inaktive).
     *
     * @return Liste aller Berechtigungen.
     */
    suspend fun findAll(): List<DomBerechtigung>

    /**
     * Deaktiviert eine Berechtigung (soft delete).
     *
     * @param berechtigungId Die ID der zu deaktivierenden Berechtigung.
     * @return true, wenn die Deaktivierung erfolgreich war, false sonst.
     */
    suspend fun deactivateBerechtigung(berechtigungId: Uuid): Boolean

    /**
     * Löscht eine Berechtigung permanent (nur für nicht-System-Berechtigungen).
     *
     * @param berechtigungId Die ID der zu löschenden Berechtigung.
     * @return true, wenn das Löschen erfolgreich war, false sonst.
     */
    suspend fun deleteBerechtigung(berechtigungId: Uuid): Boolean

    /**
     * Prüft, ob eine Berechtigung mit dem gegebenen Typ bereits existiert.
     *
     * @param berechtigungTyp Der zu prüfende Berechtigungstyp.
     * @return true, wenn eine Berechtigung mit diesem Typ existiert, false sonst.
     */
    suspend fun existsByTyp(berechtigungTyp: BerechtigungE): Boolean
}
