package at.mocode.shared.model.entitaeten

import at.mocode.shared.model.serializers.BigDecimalSerializer
import at.mocode.shared.model.serializers.KotlinInstantSerializer
import at.mocode.shared.model.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Artikel(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    var bezeichnung: String,
    @Serializable(with = BigDecimalSerializer::class) // Beispiel für Serializer
    var preis: BigDecimal,
    var einheit: String,
    var istVerbandsabgabe: Boolean = false,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
