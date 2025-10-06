package at.mocode.clients.pingfeature.model

/**
 * Konkrete Rollen-Definitionen für das Reitsport-Authentication-Testing
 * Basiert auf den aktuell verfügbaren BerechtigungE und wird mit der fachlichen Implementierung erweitert
 */
object ReitsportRoles {

    /**
     * System-Administrator - Vollzugriff auf alle Bounded Contexts
     */
    val ADMIN = ReitsportRole(
        roleType = RolleE.ADMIN,
        displayName = "System-Administrator",
        description = "Vollzugriff auf alle Microservices und System-Konfiguration",
        icon = "🔧",
        permissions = BerechtigungE.entries, // Alle verfügbaren Berechtigungen
        priority = 1,
        category = RoleCategory.SYSTEM
    )

    /**
     * Vereins-Administrator - Vereins-Bounded-Context
     */
    val VEREINS_ADMIN = ReitsportRole(
        roleType = RolleE.VEREINS_ADMIN,
        displayName = "Vereins-Administrator",
        description = "Vereinsverwaltung und Mitglieder-Management",
        icon = "🏛️",
        permissions = listOf(
            // Personen (Mitglieder)
            BerechtigungE.PERSON_READ,
            BerechtigungE.PERSON_CREATE,
            BerechtigungE.PERSON_UPDATE,
            BerechtigungE.PERSON_DELETE,
            // Verein
            BerechtigungE.VEREIN_READ,
            BerechtigungE.VEREIN_UPDATE,
            // Veranstaltungen organisieren
            BerechtigungE.VERANSTALTUNG_READ,
            BerechtigungE.VERANSTALTUNG_CREATE,
            BerechtigungE.VERANSTALTUNG_UPDATE,
            // Pferde (für Vereinsmitglieder)
            BerechtigungE.PFERD_READ
        ),
        priority = 2,
        category = RoleCategory.SYSTEM
    )

    /**
     * Funktionär - Event-Management-Bounded-Context
     */
    val FUNKTIONAER = ReitsportRole(
        roleType = RolleE.FUNKTIONAER,
        displayName = "Funktionär (Meldestelle)",
        description = "Turnierorganisation: Nennungen, Starterlisten, Meldestellen-Workflows",
        icon = "⚖️",
        permissions = listOf(
            // Lesen aller relevanten Daten
            BerechtigungE.PERSON_READ,
            BerechtigungE.PFERD_READ,
            BerechtigungE.VERANSTALTUNG_READ,
            BerechtigungE.VERANSTALTUNG_UPDATE, // Turnier-Management
            // Erweiterte Rechte in Veranstaltungs-Context
            // (Hier werden später Nennung-, Startlisten-Berechtigungen hinzugefügt)
        ),
        priority = 3,
        category = RoleCategory.OFFICIAL
    )

    /**
     * Richter - Spezialisierte Bewertungs-Rolle
     */
    val RICHTER = ReitsportRole(
        roleType = RolleE.RICHTER,
        displayName = "Richter",
        description = "Prüfungs-Bewertung und Ergebnis-Eingabe (ReadOnly-Zugriff auf Stammdaten)",
        icon = "⚖️",
        permissions = listOf(
            // Nur Lese-Zugriff auf relevante Daten
            BerechtigungE.PERSON_READ,         // Starter-Info
            BerechtigungE.PFERD_READ,          // Pferde-Info
            BerechtigungE.VERANSTALTUNG_READ   // Prüfungs-Details
            // Ergebnis-Eingabe wird später als eigener Bounded Context hinzugefügt
        ),
        priority = 4,
        category = RoleCategory.OFFICIAL
    )

    /**
     * Tierarzt - Veterinär-Bounded-Context
     */
    val TIERARZT = ReitsportRole(
        roleType = RolleE.TIERARZT,
        displayName = "Tierarzt",
        description = "Veterinärkontrollen und Pferde-Gesundheits-Management",
        icon = "🩺",
        permissions = listOf(
            BerechtigungE.PFERD_READ,
            BerechtigungE.PFERD_UPDATE,        // Gesundheitsdaten, Vet-Checks
            BerechtigungE.PERSON_READ,         // Besitzer-Kontakt
            BerechtigungE.VERANSTALTUNG_READ   // Turnier-Context für Kontrollen
        ),
        priority = 5,
        category = RoleCategory.OFFICIAL
    )

    /**
     * Trainer - Training-Bounded-Context (zukünftig)
     */
    val TRAINER = ReitsportRole(
        roleType = RolleE.TRAINER,
        displayName = "Trainer",
        description = "Schützlings-Betreuung und Training-Management",
        icon = "🏃‍♂️",
        permissions = listOf(
            BerechtigungE.PERSON_READ,         // Schützlinge
            BerechtigungE.PFERD_READ,          // Trainingspferde
            BerechtigungE.VERANSTALTUNG_READ   // Turnier-Planung für Schützlinge
            // Training-spezifische Berechtigungen kommen später
        ),
        priority = 6,
        category = RoleCategory.ACTIVE
    )

    /**
     * Reiter - Persönlicher Bounded Context
     */
    val REITER = ReitsportRole(
        roleType = RolleE.REITER,
        displayName = "Reiter",
        description = "Persönliche Daten, eigene Pferde und Turnier-Teilnahme",
        icon = "🐎",
        permissions = listOf(
            BerechtigungE.PERSON_READ,         // Nur eigene Daten
            BerechtigungE.PFERD_READ,          // Nur eigene Pferde
            BerechtigungE.VERANSTALTUNG_READ   // Öffentliche Turnier-Infos
            // Eigene Daten ändern: Später als PERSON_UPDATE_OWN, PFERD_UPDATE_OWN
        ),
        priority = 7,
        category = RoleCategory.ACTIVE
    )

    /**
     * Zuschauer - Public-Read-Only Bounded Context
     */
    val ZUSCHAUER = ReitsportRole(
        roleType = RolleE.ZUSCHAUER,
        displayName = "Zuschauer",
        description = "Öffentliche Informationen: Starterlisten, Ergebnisse, Zeitpläne",
        icon = "👁️",
        permissions = listOf(
            BerechtigungE.VERANSTALTUNG_READ   // Nur öffentliche Turnier-Daten
            // Später: STARTERLISTE_READ_PUBLIC, ERGEBNIS_READ_PUBLIC
        ),
        priority = 8,
        category = RoleCategory.PASSIVE
    )

    /**
     * Gast - Keine Authentifizierung erforderlich
     */
    val GAST = ReitsportRole(
        roleType = RolleE.GAST,
        displayName = "Gast",
        description = "Öffentliche Basis-Informationen ohne Registrierung",
        icon = "🔓",
        permissions = emptyList(), // Nur völlig öffentliche Endpunkte
        priority = 9,
        category = RoleCategory.PASSIVE
    )

    /**
     * Alle definierten Rollen in organisatorischer Reihenfolge
     */
    val ALL_ROLES = listOf(
        ADMIN,
        VEREINS_ADMIN,
        FUNKTIONAER,
        RICHTER,
        TIERARZT,
        TRAINER,
        REITER,
        ZUSCHAUER,
        GAST
    )

    /**
     * Rollen nach Bounded Context / Microservice gruppiert
     */
    val ROLES_BY_BOUNDED_CONTEXT = mapOf(
        "System Management" to listOf(ADMIN),
        "Vereins-Service" to listOf(VEREINS_ADMIN),
        "Event-Service" to listOf(FUNKTIONAER),
        "Bewertungs-Service" to listOf(RICHTER),
        "Vet-Service" to listOf(TIERARZT),
        "Training-Service" to listOf(TRAINER),
        "Member-Service" to listOf(REITER),
        "Public-Service" to listOf(ZUSCHAUER, GAST)
    )

    /**
     * Rollen nach UI-Kategorie (für Ping-Dashboard)
     */
    val ROLES_BY_CATEGORY = ALL_ROLES.groupBy { it.category }

    /**
     * Hilfsfunktion: Rolle nach RolleE-Typ finden
     */
    fun getRoleByType(roleType: RolleE): ReitsportRole? {
        return ALL_ROLES.find { it.roleType == roleType }
    }

    /**
     * Hilfsfunktion: Alle Rollen mit einer bestimmten Berechtigung
     */
    fun getRolesWithPermission(permission: BerechtigungE): List<ReitsportRole> {
        return ALL_ROLES.filter { it.hasPermission(permission) }
    }
}
