package at.mocode.model.entitaeten

import at.mocode.model.serializer.JavaUUIDSerializer
import at.mocode.model.serializer.KotlinInstantSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Veranstaltung(
    @Serializable(with = JavaUUIDSerializer::class) // Beispiel für Serializer, falls nötig
    val id: UUID = UUID.randomUUID(),
    var name: String,
    @Serializable(with = KotlinLocalDateSerializer::class) // Beispiel für Serializer
    var datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumBis: LocalDate,
    var veranstalterName: String,
    var veranstalterOepsNummer: String?,
    var veranstalterTyp: VeranstalterTyp = VeranstalterTyp.UNBEKANNT,
    var veranstaltungsortName: String,
    var veranstaltungsortAdresse: String,
    var kontaktpersonName: String?,
    var kontaktTelefon: String?,
    var kontaktEmail: String?,
    var webseite: String?,
    var logoUrl: String?,
    var anfahrtsplanInfo: String?,
    var sponsorInfos: List<String> = emptyList(),
    var dsgvoText: String?,
    var haftungsText: String?,
    var sonstigeBesondereBestimmungen: String?,
    @Serializable(with = KotlinInstantSerializer::class) // Beispiel für Serializer
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
