package at.mocode.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.members.application.usecase.CreatePersonUseCase
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.members.domain.repository.VereinRepository
import at.mocode.members.domain.service.MasterDataService
import at.mocode.enums.GeschlechtE
import at.mocode.enums.DatenQuelleE
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class CreatePersonViewModel(
    private val createPersonUseCase: CreatePersonUseCase
) : ViewModel() {

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
    var geschlecht by mutableStateOf<GeschlechtE?>(null)
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
    fun updateGeschlecht(value: GeschlechtE?) { geschlecht = value }
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

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Parse birth date if provided
                val parsedGeburtsdatum = if (geburtsdatum.isNotBlank()) {
                    try {
                        val parts = geburtsdatum.split("-")
                        if (parts.size == 3) {
                            LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                        } else null
                    } catch (e: Exception) {
                        errorMessage = "Ungültiges Datumsformat. Verwenden Sie YYYY-MM-DD"
                        isLoading = false
                        return@launch
                    }
                } else null

                val request = CreatePersonUseCase.CreatePersonRequest(
                    oepsSatzNr = oepsSatzNr.takeIf { it.isNotBlank() },
                    nachname = nachname,
                    vorname = vorname,
                    titel = titel.takeIf { it.isNotBlank() },
                    geburtsdatum = parsedGeburtsdatum,
                    geschlechtE = geschlecht,
                    telefon = telefon.takeIf { it.isNotBlank() },
                    email = email.takeIf { it.isNotBlank() },
                    strasse = strasse.takeIf { it.isNotBlank() },
                    plz = plz.takeIf { it.isNotBlank() },
                    ort = ort.takeIf { it.isNotBlank() },
                    adresszusatzZusatzinfo = adresszusatz.takeIf { it.isNotBlank() },
                    feiId = feiId.takeIf { it.isNotBlank() },
                    mitgliedsNummerBeiStammVerein = mitgliedsNummer.takeIf { it.isNotBlank() },
                    istGesperrt = istGesperrt,
                    sperrGrund = sperrGrund.takeIf { it.isNotBlank() },
                    datenQuelle = DatenQuelleE.MANUELL,
                    notizenIntern = notizen.takeIf { it.isNotBlank() }
                )

                val response = createPersonUseCase.execute(request)

                if (response.success) {
                    isSuccess = true
                } else {
                    errorMessage = response.error?.message ?: "Unbekannter Fehler beim Erstellen der Person"
                }
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
        geschlecht = null
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
