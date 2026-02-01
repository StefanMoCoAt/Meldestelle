package at.mocode.ping.infrastructure.persistence

import at.mocode.ping.domain.Ping
import at.mocode.ping.domain.PingRepository
import org.springframework.stereotype.Repository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import java.time.Instant

@OptIn(ExperimentalUuidApi::class)
@Repository
// @Profile("!test") entfernt, damit Integrationstests den echten Adapter nutzen können.
// In Unit-Tests wird er durch Mocks (@MockBean oder @TestConfiguration) ersetzt.
class PingRepositoryAdapter(
    private val jpaRepository: SpringDataPingRepository
) : PingRepository {

    override fun save(ping: Ping): Ping {
        val entity = ping.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findAll(): List<Ping> {
        return jpaRepository.findAll().map { it.toDomain() }
    }

    override fun findById(id: Uuid): Ping? {
        return jpaRepository.findById(id.toJavaUuid()).map { it.toDomain() }.orElse(null)
    }

    override fun findByTimestampAfter(timestamp: Instant): List<Ping> {
        return jpaRepository.findByCreatedAtAfter(timestamp).map { it.toDomain() }
    }

    private fun Ping.toEntity() = PingJpaEntity(
        id = this.id.toJavaUuid(),
        message = this.message,
        createdAt = this.timestamp
    )

    private fun PingJpaEntity.toDomain() = Ping(
        id = this.id.toKotlinUuid(),
        message = this.message,
        timestamp = this.createdAt
    )
}
