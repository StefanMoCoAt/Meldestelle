package at.mocode.dto

import at.mocode.dto.base.VersionedDto
import at.mocode.dto.base.Since
import at.mocode.serializers.BigDecimalSerializer
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Since("1.0")
data class ArtikelDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    @Serializable(with = BigDecimalSerializer::class)
    val preis: BigDecimal,
    val einheit: String,
    val istVerbandsabgabe: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant,
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto

@Serializable
@Since("1.0")
data class CreateArtikelDto(
    val bezeichnung: String,
    @Serializable(with = BigDecimalSerializer::class)
    val preis: BigDecimal,
    val einheit: String,
    val istVerbandsabgabe: Boolean = false,
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto

@Serializable
@Since("1.0")
data class UpdateArtikelDto(
    val bezeichnung: String,
    @Serializable(with = BigDecimalSerializer::class)
    val preis: BigDecimal,
    val einheit: String,
    val istVerbandsabgabe: Boolean = false,
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto
