package at.mocode.model.stammdaten

import at.mocode.model.enums.FunktionaerRolle
import at.mocode.model.enums.Geschlecht
import at.mocode.model.serializer.JavaUUIDSerializer
import at.mocode.model.serializer.KotlinInstantSerializer
import at.mocode.model.serializer.KotlinLocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @Serializable(with = JavaUUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
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
    @Serializable(with = JavaUUIDSerializer::class)
    var stammVereinId: UUID?, // FK zum Verein
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
