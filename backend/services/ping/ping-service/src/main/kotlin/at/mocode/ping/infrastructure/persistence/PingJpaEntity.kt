package at.mocode.ping.infrastructure.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ping")
class PingJpaEntity(
    @Id
    val id: UUID,
    val message: String,
    val createdAt: Instant
) {
    // Default constructor for JPA
    protected constructor() : this(UUID.randomUUID(), "", Instant.now())
}
