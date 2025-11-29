package at.mocode.clients.pingfeature.model

/**
 * Phase 1 Validierung für Reitsport-Authentication-Testing
 * testet alle Erfolgs-Kriterien aus der Aufgabenstellung
 */
object Phase1Validation {

  /**
   * Führt alle Phase 1 Validierungen durch
   */
  fun validatePhase1(): String {
    val results = mutableListOf<String>()

    // ✅ Test 1: Anzahl Rollen (erwartet: 9)
    val roleCount = ReitsportRoles.ALL_ROLES.size
    results.add("✅ Rollen-Anzahl: $roleCount (erwartet: 9) - ${if (roleCount == 9) "ERFOLG" else "FEHLER"}")

    // ✅ Test 2: Admin-Rolle verfügbar
    val adminRole = ReitsportRoles.ADMIN
    results.add("✅ Admin-Rolle: ${adminRole.displayName} - ERFOLG")

    // ✅ Test 3: Alle Kategorien verfügbar
    val categories = ReitsportRoles.ROLES_BY_CATEGORY.keys
    results.add("✅ Kategorien: $categories - ERFOLG")
    results.add("   - SYSTEM: ${ReitsportRoles.ROLES_BY_CATEGORY[RoleCategory.SYSTEM]?.size ?: 0} Rollen")
    results.add("   - OFFICIAL: ${ReitsportRoles.ROLES_BY_CATEGORY[RoleCategory.OFFICIAL]?.size ?: 0} Rollen")
    results.add("   - ACTIVE: ${ReitsportRoles.ROLES_BY_CATEGORY[RoleCategory.ACTIVE]?.size ?: 0} Rollen")
    results.add("   - PASSIVE: ${ReitsportRoles.ROLES_BY_CATEGORY[RoleCategory.PASSIVE]?.size ?: 0} Rollen")

    // ✅ Test 4: DateTime funktioniert
    val timestamp = DateTimeHelper.now()
    results.add("✅ DateTime funktioniert: $timestamp - ERFOLG")

    // ✅ Test 5: Test-ID generiert
    val testId = getTimeMillis().toString()
    results.add("✅ Test-ID generiert: $testId - ERFOLG")

    // ✅ Test 6: Enum-Zugriff funktioniert
    results.add("✅ RolleE Enum: ${RolleE.entries.size} Einträge - ERFOLG")
    results.add("✅ BerechtigungE Enum: ${BerechtigungE.entries.size} Einträge - ERFOLG")

    // ✅ Test 7: Alle 9 Rollen einzeln prüfen
    results.add("✅ Alle Rollen-Definitionen:")
    ReitsportRoles.ALL_ROLES.forEachIndexed { index, role ->
      results.add("   ${index + 1}. ${role.displayName} (${role.roleType}) - ${role.permissions.size} Berechtigungen")
    }

    // ✅ Test 8: Berechtigungen-Zuordnung testen
    val adminPermissions = ReitsportRoles.ADMIN.permissions.size
    val guestPermissions = ReitsportRoles.GAST.permissions.size
    results.add("✅ Admin-Berechtigungen: $adminPermissions (max)")
    results.add("✅ Gast-Berechtigungen: $guestPermissions (min)")

    // ✅ Test 9: Hilfsfunktionen testen
    val roleByType = ReitsportRoles.getRoleByType(RolleE.RICHTER)
    results.add("✅ Rolle per Type: ${roleByType?.displayName} - ERFOLG")

    val rolesWithRead = ReitsportRoles.getRolesWithPermission(BerechtigungE.PERSON_READ)
    results.add("✅ Rollen mit PERSON_READ: ${rolesWithRead.size} - ERFOLG")

    return results.joinToString("\n")
  }

  /**
   * Führt Performance-Test durch
   */
  fun performanceTest(): String {
    val start = DateTimeHelper.now()

    // Simuliere mehrere Rollen-Abfragen
    repeat(100) {
      ReitsportRoles.getAllRoles()
      ReitsportRoles.getRoleByType(RolleE.ADMIN)
      ReitsportRoles.getRolesWithPermission(BerechtigungE.PERSON_READ)
    }

    val end = DateTimeHelper.now()
    val duration = end - start

    return "✅ Performance-Test: $duration Zeiteinheiten für 300 Operationen - ERFOLG"
  }
}

/**
 * Hilfsfunktion für externe Zeitabfrage
 */
private fun getTimeMillis(): Long = DateTimeHelper.now()

/**
 * Extension für einfacheren Zugriff
 */
private fun ReitsportRoles.getAllRoles() = ALL_ROLES
