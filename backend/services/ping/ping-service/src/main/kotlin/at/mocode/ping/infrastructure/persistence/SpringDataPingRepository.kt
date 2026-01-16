package at.mocode.ping.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataPingRepository : JpaRepository<PingJpaEntity, UUID>
