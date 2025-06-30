package at.mocode.stammdaten

import at.mocode.enums.GeschlechtPferdE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Pferd(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    var oepsKopfNr: String?,
    var oepsSatzNr: String?,
    var name: String,
    var lebensnummer: String?,
    var feiPassNr: String?,
    var geschlecht: GeschlechtPferdE?,
    var geburtsjahr: Int?,
    var rasse: String?,
    var farbe: String?,
    var vaterName: String?,
    var mutterName: String?,
    var mutterVaterName: String?,
    @Serializable(with = UuidSerializer::class)
    var besitzerId: Uuid?, // FK Person
    @Serializable(with = UuidSerializer::class)
    var verantwortlichePersonId: Uuid?, // FK Person
    @Serializable(with = UuidSerializer::class)
    var heimatVereinId: Uuid?, // FK Verein
    var letzteZahlungJahrOeps: Int?,
    var stockmassCm: Int?,
    var istAktiv: Boolean = true,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
