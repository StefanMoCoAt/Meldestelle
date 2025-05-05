package at.mocode.model.entitaeten

import at.mocode.model.serializer.JavaUUIDSerializer
import kotlinx.serialization.Serializable

@Serializable
data class MeisterschaftReferenz(
    @Serializable(with = JavaUUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = JavaUUIDSerializer::class)
    var meisterschaftId: UUID, // FK zu einer Meisterschafts-Entität
    var name: String,
    var betrifftBewerbNummern: List<Int>,
    var berechnungsstrategie: String?,
    var reglementUrl: String?
)
