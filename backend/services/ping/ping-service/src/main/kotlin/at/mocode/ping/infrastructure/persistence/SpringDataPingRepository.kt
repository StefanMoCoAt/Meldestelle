package at.mocode.ping.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import java.time.Instant

interface SpringDataPingRepository : JpaRepository<PingJpaEntity, UUID> {
    fun findByCreatedAtAfter(createdAt: Instant): List<PingJpaEntity>
}
