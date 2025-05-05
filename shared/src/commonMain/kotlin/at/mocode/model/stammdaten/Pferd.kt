package at.mocode.model.stammdaten

import at.mocode.model.enums.GeschlechtPferd
import at.mocode.model.serializer.JavaUUIDSerializer
import at.mocode.model.serializer.KotlinInstantSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Pferd(
    @Serializable(with = JavaUUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    var oepsKopfNr: String?,
    var oepsSatzNr: String?,
    var name: String,
    var lebensnummer: String?,
    var feiPassNr: String?,
    var geschlecht: GeschlechtPferd?,
    var geburtsjahr: Int?,
    var rasse: String?,
    var farbe: String?,
    var vaterName: String?,
    var mutterName: String?,
    var mutterVaterName: String?,
    @Serializable(with = JavaUUIDSerializer::class)
    var besitzerId: UUID?, // FK Person
    @Serializable(with = JavaUUIDSerializer::class)
    var verantwortlichePersonId: UUID?, // FK Person
    @Serializable(with = JavaUUIDSerializer::class)
    var heimatVereinId: UUID?, // FK Verein
    var letzteZahlungJahrOeps: Int?,
    var stockmassCm: Int?,
    var istAktiv: Boolean = true,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
