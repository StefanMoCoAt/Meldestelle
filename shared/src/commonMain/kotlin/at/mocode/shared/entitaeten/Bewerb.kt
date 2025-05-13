package at.mocode.shared.entitaeten

import at.mocode.shared.enums.BewerbStatus
import at.mocode.shared.enums.Sparte
import at.mocode.shared.serializers.BigDecimalSerializer
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Bewerb(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    var turnierId: Uuid,
    var nummer: Int,
    var bezeichnung: String,
    var klasse: String,
    var datum: LocalDate,
    var sparte: Sparte,
    var richtverfahren: String?,
    var beginnZeit: String, // TIME als String (z.B. "09:00" oder "anschließend")
    var istFixeBeginnZeit: Boolean = false,
    var laufzeitProStarter: Int?, // Dauer pro Starter in Minuten
    var maxStarter: Int?,
    @Serializable(with = BigDecimalSerializer::class)
    var nenngeld: BigDecimal?,
    var sonderpruefungReferenz: SonderpruefungReferenz?,
    var cupReferenz: List<CupReferenz> = emptyList(),
    var status: BewerbStatus = BewerbStatus.GEPLANT,
    var details: String?,
    var einteilung: String?,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant
)
