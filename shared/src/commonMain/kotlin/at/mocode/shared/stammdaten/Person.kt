package at.mocode.shared.stammdaten

import at.mocode.shared.enums.FunktionaerRolle
import at.mocode.shared.enums.Geschlecht
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.KotlinLocalDateSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    var oepsSatzNr: String?,
    var nachname: String,
    var vorname: String,
    var titel: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var geburtsdatum: LocalDate?,
    var geschlecht: Geschlecht? = Geschlecht.UNBEKANNT,
    var nationalitaet: String?, // 3-Letter Code
    var email: String?,
    var telefon: String?,
    var adresse: String?,
    var plz: String?,
    var ort: String?,
    @Serializable(with = UuidSerializer::class)
    var stammVereinId: Uuid?, // FK zum Verein
    var mitgliedsNummerIntern: String?,
    var letzteZahlungJahr: Int?,
    var feiId: String?,
    var istGesperrt: Boolean = false,
    var sperrGrund: String?,
    var rollen: Set<FunktionaerRolle> = emptySet(),
    var lizenzen: List<LizenzInfo> = emptyList(),
    var qualifikationenRichter: List<String> = emptyList(),
    var qualifikationenParcoursbauer: List<String> = emptyList(),
    var istAktiv: Boolean = true,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
