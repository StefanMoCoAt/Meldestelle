package at.mocode.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Gemeinsame Enums, die domänenweit verwendet werden.
 * Teil des Shared Kernel zur Sicherung einer konsistenten Fachsprache.
 */

/**
 * Quelle eines Datensatzes. Querschnittsthema und daher Teil des Shared Kernel.
 */
@Serializable
enum class DatenQuelleE {
    MANUELL,
    IMPORT_ZNS,
    SYSTEM_GENERATED,
    IMPORT_API
}

/**
 * Allgemeiner Status von Entitäten in der Domäne.
 */
@Serializable
enum class StatusE {
    AKTIV,
    INAKTIV,
    ENTWURF,
    ARCHIVIERT,
    GELOESCHT
}

/**
 * Prioritätsstufen für unterschiedliche Domänen-Objekte.
 */
@Serializable
enum class PrioritaetE {
    NIEDRIG,
    NORMAL,
    HOCH,
    KRITISCH
}

/**
 * Häufige Benutzerrollen im System.
 */
@Serializable
enum class BenutzerRolleE {
    ADMIN,
    BENUTZER,
    MODERATOR,
    GAST,
    SYSTEM
}

/**
 * Verifikationsstatus für Datensätze.
 */
@Serializable
enum class VerifikationsStatusE {
    NICHT_VERIFIZIERT,
    IN_PRUEFUNG,
    VERIFIZIERT,
    ABGELEHNT,
    KORREKTUR_ERFORDERLICH
}

/**
 * Processing states for workflows and tasks.
 */
@Serializable
enum class BearbeitungsStatusE {
    OFFEN,
    IN_BEARBEITUNG,
    WARTEND,
    ABGESCHLOSSEN,
    ABGEBROCHEN,
    FEHLER
}
