package at.mocode.ping.infrastructure.persistence

import at.mocode.ping.domain.Ping
import at.mocode.ping.domain.PingRepository
import org.springframework.stereotype.Component
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

/**
 * Driven Adapter.
 * Implementiert den Domain-Port `PingRepository` mithilfe von Spring Data JPA.
 * Mappt zwischen Domain-Entity und JPA-Entity.
 */
@Component
@OptIn(ExperimentalUuidApi::class)
class PingRepositoryAdapter(
    private val jpaRepository: SpringDataPingRepository
) : PingRepository {

    override fun save(ping: Ping): Ping {
        val jpaEntity = PingJpaEntity(
            id = ping.id.toJavaUuid(),
            message = ping.message,
            timestamp = ping.timestamp
        )
        val saved = jpaRepository.save(jpaEntity)
        return mapToDomain(saved)
    }

    override fun findAll(): List<Ping> {
        return jpaRepository.findAll().map { mapToDomain(it) }
    }

    override fun findById(id: Uuid): Ping? {
        return jpaRepository.findById(id.toJavaUuid())
            .map { mapToDomain(it) }
            .orElse(null)
    }

    private fun mapToDomain(entity: PingJpaEntity): Ping {
        return Ping(
            id = entity.id.toKotlinUuid(),
            message = entity.message,
            timestamp = entity.timestamp
        )
    }
}
