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
    // The default constructor for JPA
    // Protected is fine for Hibernate, but the Kotlin compiler might complain about visibility.
    // We can make it private or internal if needed, but protected is standard.
    // To suppress the warning "effectively private", we can just leave it as is or make it public/internal.
    // Let's try making it internal to satisfy Kotlin while keeping it hidden from public API.
    internal constructor() : this(UUID.randomUUID(), "", Instant.now())
}
