package at.mocode.model.entitaeten

import at.mocode.model.enums.PlatzTyp
import at.mocode.model.serializer.JavaUUIDSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Platz(
    @Serializable(with = JavaUUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    var name: String,
    var dimension: String?,
    var boden: String?,
    var typ: PlatzTyp
)
