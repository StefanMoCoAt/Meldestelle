package at.mocode.client.web.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import at.mocode.client.common.repository.Person
import at.mocode.client.common.repository.PersonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * ViewModel for creating a person.
 * This is a simplified version that doesn't depend on androidx.lifecycle.
 * It uses Compose for Desktop's own state management.
 */
class CreatePersonViewModel(
    private val personRepository: PersonRepository
) {
    // Coroutine scope for launching background tasks
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Form state
    var nachname by mutableStateOf("")
        private set
    var vorname by mutableStateOf("")
        private set
    var titel by mutableStateOf("")
        private set
    var oepsSatzNr by mutableStateOf("")
        private set
    var geburtsdatum by mutableStateOf("")
        private set
    var telefon by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var strasse by mutableStateOf("")
        private set
    var plz by mutableStateOf("")
        private set
    var ort by mutableStateOf("")
        private set
    var adresszusatz by mutableStateOf("")
        private set
    var feiId by mutableStateOf("")
        private set
    var mitgliedsNummer by mutableStateOf("")
        private set
    var notizen by mutableStateOf("")
        private set
    var istGesperrt by mutableStateOf(false)
        private set
    var sperrGrund by mutableStateOf("")
        private set

    // UI state
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isSuccess by mutableStateOf(false)
        private set

    // Update methods
    fun updateNachname(value: String) { nachname = value }
    fun updateVorname(value: String) { vorname = value }
    fun updateTitel(value: String) { titel = value }
    fun updateOepsSatzNr(value: String) { oepsSatzNr = value }
    fun updateGeburtsdatum(value: String) { geburtsdatum = value }
    fun updateTelefon(value: String) { telefon = value }
    fun updateEmail(value: String) { email = value }
    fun updateStrasse(value: String) { strasse = value }
    fun updatePlz(value: String) { plz = value }
    fun updateOrt(value: String) { ort = value }
    fun updateAdresszusatz(value: String) { adresszusatz = value }
    fun updateFeiId(value: String) { feiId = value }
    fun updateMitgliedsNummer(value: String) { mitgliedsNummer = value }
    fun updateNotizen(value: String) { notizen = value }
    fun updateIstGesperrt(value: Boolean) { istGesperrt = value }
    fun updateSperrGrund(value: String) { sperrGrund = value }

    fun clearError() {
        errorMessage = null
    }

    fun createPerson() {
        // Basic validation
        when {
            nachname.isBlank() -> {
                errorMessage = "Nachname ist erforderlich"
                return
            }
            vorname.isBlank() -> {
                errorMessage = "Vorname ist erforderlich"
                return
            }
        }

        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Parse birthdate if provided
                val parsedGeburtsdatum = if (geburtsdatum.isNotBlank()) {
                    try {
                        val parts = geburtsdatum.split("-")
                        if (parts.size == 3) {
                            LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                        } else {
                            errorMessage = "Ungültiges Datumsformat. Verwenden Sie YYYY-MM-DD"
                            isLoading = false
                            isSuccess = false
                            return@launch
                        }
                    } catch (_: Exception) {
                        errorMessage = "Ungültiges Datumsformat. Verwenden Sie YYYY-MM-DD"
                        isLoading = false
                        isSuccess = false
                        return@launch
                    }
                } else null

                // Create a Person object from form data
                val person = Person(
                    nachname = nachname,
                    vorname = vorname,
                    titel = titel.takeIf { it.isNotBlank() },
                    oepsSatzNr = oepsSatzNr.takeIf { it.isNotBlank() },
                    geburtsdatum = parsedGeburtsdatum,
                    telefon = telefon.takeIf { it.isNotBlank() },
                    email = email.takeIf { it.isNotBlank() },
                    strasse = strasse.takeIf { it.isNotBlank() },
                    plz = plz.takeIf { it.isNotBlank() },
                    ort = ort.takeIf { it.isNotBlank() },
                    adresszusatz = adresszusatz.takeIf { it.isNotBlank() },
                    feiId = feiId.takeIf { it.isNotBlank() },
                    mitgliedsNummer = mitgliedsNummer.takeIf { it.isNotBlank() },
                    notizen = notizen.takeIf { it.isNotBlank() },
                    istGesperrt = istGesperrt,
                    sperrGrund = sperrGrund.takeIf { it.isNotBlank() },
                    datenQuelle = "MANUELL"
                )

                // Save the person using the repository
                personRepository.save(person)

                // Set success state
                isSuccess = true
            } catch (e: Exception) {
                errorMessage = "Fehler beim Erstellen der Person: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetForm() {
        nachname = ""
        vorname = ""
        titel = ""
        oepsSatzNr = ""
        geburtsdatum = ""
        telefon = ""
        email = ""
        strasse = ""
        plz = ""
        ort = ""
        adresszusatz = ""
        feiId = ""
        mitgliedsNummer = ""
        notizen = ""
        istGesperrt = false
        sperrGrund = ""
        isLoading = false
        errorMessage = null
        isSuccess = false
    }
}
