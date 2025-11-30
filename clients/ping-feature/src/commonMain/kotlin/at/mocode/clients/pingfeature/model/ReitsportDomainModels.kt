package at.mocode.clients.pingfeature.model

import kotlinx.serialization.Serializable

/**
 * Reitsport-spezifische Domain-Modelle für Authentication-Testing
 * basiert auf der österreichischen Turnierordnung (ÖTO) und echten Geschäftsprozessen
 */

/**
 * Definition einer Benutzerrolle im Reitsport-Kontext.
 * Kombiniert die RolleE mit konkreten Berechtigungen und UI-Informationen
 */
@Serializable
data class ReitsportRole(
  val roleType: RolleE,
  val displayName: String,
  val description: String,
  val icon: String,
  val permissions: List<BerechtigungE>,
  val priority: Int, // Für Sortierung in UI (1 = höchste Priorität)
  val category: RoleCategory
) {
  /**
   * Hilfsfunktion: Prüft, ob diese Rolle eine bestimmte Berechtigung hat
   */
  fun hasPermission(permission: BerechtigungE): Boolean {
    return permissions.contains(permission)
  }

  /**
   * Hilfsfunktion: Gibt alle fehlenden Berechtigungen für eine Liste zurück
   */
  fun getMissingPermissions(requiredPermissions: List<BerechtigungE>): List<BerechtigungE> {
    return requiredPermissions.filter { !permissions.contains(it) }
  }
}

/**
 * Kategorisierung der Rollen für bessere UI-Organisation
 */
@Serializable
enum class RoleCategory(val displayName: String, val color: String) {
  SYSTEM("System-Verwaltung", "#FF5722"),        // Rot
  OFFICIAL("Offizielle Funktionen", "#3F51B5"),  // Indigo
  ACTIVE("Aktive Teilnahme", "#4CAF50"),         // Grün
  PASSIVE("Information & Zugang", "#9E9E9E")     // Grau
}

/**
 * Test-Szenario für einen konkreten Geschäftsprozess
 */
@Serializable
data class AuthTestScenario(
  val id: String,
  val name: String,
  val businessProcess: String,
  val description: String,
  val expectedBehavior: String,
  val requiredRole: RolleE,
  val requiredPermissions: List<BerechtigungE>,
  val testEndpoint: String,
  val testMethod: String = "GET",
  val priority: TestPriority = TestPriority.NORMAL,
  val category: ScenarioCategory
)

/**
 * Realistische Kategorisierung der Test-Szenarien basierend auf echten Geschäftsprozessen
 */
@Serializable
enum class ScenarioCategory(val displayName: String, val icon: String) {
  // Kern-Geschäftsprozesse
  VERANSTALTUNG_SETUP("Veranstaltungs-Einrichtung", "🏟️"),
  TURNIER_MANAGEMENT("Turnier-Verwaltung", "🎪"),
  BEWERB_KONFIGURATION("Bewerb-Konfiguration", "🏇"),

  // Finanzen
  KASSABUCH("Kassabuch-Führung", "💰"),
  ABRECHNUNG("Turnier-Abrechnung", "🧾"),

  // Nennsystem
  NENNUNG_WEBFORMULAR("Nenn-Web-Formular", "📝"),
  NENNUNG_MOBILE("Mobile Nennung", "📱"),
  NENNTAUSCH("Nenntausch-System", "🔄"),

  // Startlisten & Zeitplan
  ZEITPLAN_ERSTELLUNG("Zeitplan-Erstellung", "⏰"),
  STARTERLISTE_FLEXIBEL("Flexible Starterlisten", "📋"),
  RICHTER_VALIDATION("Richter-Lizenz-Validierung", "⚖️"),

  // Ergebnisse
  ERGEBNIS_DRESSUR("Ergebnis-Erfassung Dressur", "🎭"),
  ERGEBNIS_SPRINGEN("Ergebnis-Erfassung Springen", "🚀"),
  ERGEBNIS_VIELSEITIGKEIT("Ergebnis-Erfassung Vielseitigkeit", "🎯"),

  // OEPS Integration
  OEPS_SYNC("OEPS-Synchronisation", "🔗"),
  TURNIER_NUMMER("Turnier-Nummer-Verwaltung", "🔢"),

  // System
  SYSTEM_ADMIN("System-Administration", "🔧"),
  BENUTZER_VERWALTUNG("Benutzer-Verwaltung", "👥")
}

/**
 * Erweiterte Test-Szenarien für realistische Geschäftsprozesse
 */
@Serializable
data class ComplexAuthTestScenario(
  val id: String,
  val name: String,
  val businessProcess: String,
  val description: String,
  val subProcesses: List<String>, // Multi-Step-Prozesse
  val requiredRole: RolleE,
  val requiredPermissions: List<BerechtigungE>,
  val testEndpoints: List<TestEndpoint>, // Mehrere API-Calls
  val mockData: Map<String, String> = emptyMap(),
  val expectedOutcome: String,
  val priority: TestPriority = TestPriority.NORMAL,
  val category: ScenarioCategory,
  val oepsIntegrationRequired: Boolean = false
)

@Serializable
data class TestEndpoint(
  val name: String,
  val url: String,
  val method: String = "GET",
  val payload: String? = null,
  val expectedResponseCode: Int = 200,
  val description: String
)

/**
 * Priorität von Test-Szenarien
 */
@Serializable
enum class TestPriority(val displayName: String, val level: Int) {
  CRITICAL("Kritisch", 1),
  HIGH("Hoch", 2),
  NORMAL("Normal", 3),
  LOW("Niedrig", 4)
}

/**
 * Ergebnis eines einzelnen API-Tests
 */
@Serializable
data class ApiTestResult(
  val scenarioId: String,
  val scenarioName: String,
  val endpoint: String,
  val method: String,
  val expectedResult: String,
  val actualResult: String,
  val success: Boolean,
  val responseCode: Int? = null,
  val duration: Long, // in Millisekunden
  val timestamp: Long = getTimeMillis(),
  val token: String? = null, // Gekürzte Token-Info für Debugging
  val errorMessage: String? = null,
  val responseData: String? = null
) {
  /**
   * Hilfsfunktion: Formatiert die Dauer für UI-Anzeige
   */
  fun formatDuration(): String = "${duration}ms"

  /**
   * Hilfsfunktion: Status-Icon für UI
   */
  fun getStatusIcon(): String = if (success) "✅" else "❌"
}

/**
 * Komplettes Ergebnis eines Rollen-basierten Tests
 */
@Serializable
data class ReitsportTestResult(
  val testId: String = getTimeMillis().toString(),
  val role: ReitsportRole,
  val scenarios: List<AuthTestScenario>,
  val apiResults: List<ApiTestResult>,
  val startTime: Long,
  val endTime: Long? = null,
  val overallSuccess: Boolean = false,
  val summary: TestSummary? = null
) {
  /**
   * Berechnet die Gesamtdauer des Tests
   */
  fun getTotalDuration(): Long = (endTime ?: getTimeMillis()) - startTime

  /**
   * Berechnet Erfolgsrate in Prozent
   */
  fun getSuccessRate(): Double {
    if (apiResults.isEmpty()) return 0.0
    val successful = apiResults.count { it.success }
    return (successful.toDouble() / apiResults.size) * 100
  }

  /**
   * Gibt alle fehlgeschlagenen Tests zurück
   */
  fun getFailedTests(): List<ApiTestResult> = apiResults.filter { !it.success }
}

/**
 * Zusammenfassung eines Test-Durchlaufs
 */
@Serializable
data class TestSummary(
  val totalTests: Int,
  val successfulTests: Int,
  val failedTests: Int,
  val averageDuration: Long,
  val criticalFailures: List<String> = emptyList(),
  val recommendations: List<String> = emptyList()
) {
  val successRate: Double
    get() = if (totalTests > 0) (successfulTests.toDouble() / totalTests) * 100 else 0.0
}

/**
 * Mock-Daten für Testfälle
 */
@Serializable
data class TestNennung(
  val reiterId: String,
  val pferdId: String,
  val bewerbId: String,
  val nennungsDatum: Long = getTimeMillis()
)

@Serializable
data class TestStartbereitschaft(
  val nennungId: String,
  val confirmed: Boolean = true,
  val confirmationTime: Long = getTimeMillis()
)

/**
 * Hilfsfunktionen für DateTime (KMP-kompatibel)
 * Temporäre Lösung für Phase 1 mit incrementellem Counter
 */
object DateTimeHelper {
  private var counter = 1000000000L // Start mit einer realistischen Timestamp

  fun now(): Long = counter++

  fun formatDateTime(timestamp: Long): String {
    // Einfache ISO-ähnliche Formatierung ohne kotlinx-datetime
    return "Timestamp: $timestamp" // Temporäre Lösung für Phase 1
  }
}

/**
 * KMP-kompatible Zeitfunktion für Phase 1
 */
private fun getTimeMillis(): Long = DateTimeHelper.now()
