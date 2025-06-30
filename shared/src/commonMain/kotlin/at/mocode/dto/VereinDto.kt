package at.mocode.dto

import at.mocode.dto.base.VersionedDto
import at.mocode.dto.base.Since
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Since("1.0")
data class VereinDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val oepsVereinsNr: String,
    val name: String,
    val kuerzel: String?,
    val bundesland: String?,
    val adresse: String?,
    val plz: String?,
    val ort: String?,
    val email: String?,
    val telefon: String?,
    val webseite: String?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant,
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto

@Serializable
@Since("1.0")
data class CreateVereinDto(
    val oepsVereinsNr: String,
    val name: String,
    val kuerzel: String? = null,
    val bundesland: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val webseite: String? = null,
    val istAktiv: Boolean = true,
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto

@Serializable
@Since("1.0")
data class UpdateVereinDto(
    val name: String,
    val kuerzel: String? = null,
    val bundesland: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val webseite: String? = null,
    val istAktiv: Boolean = true,
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto
