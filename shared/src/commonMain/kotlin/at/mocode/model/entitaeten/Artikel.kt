package at.mocode.model.entitaeten

import at.mocode.model.serializer.BigDecimalSerializer
import at.mocode.model.serializer.JavaUUIDSerializer
import at.mocode.model.serializer.KotlinInstantSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Artikel(
    @Serializable(with = JavaUUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
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
