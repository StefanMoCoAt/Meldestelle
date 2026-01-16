package at.mocode.ping.application

import at.mocode.ping.domain.Ping
import at.mocode.ping.domain.PingRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Application Service.
 * Implementiert den Use Case und orchestriert Domain & Repository.
 * Hier darf Spring (@Service, @Transactional) verwendet werden, da es "Application Logic" ist.
 */
@Service
@Profile("!test") // Nicht im Test-Profil laden, damit wir Mocks nutzen können
@OptIn(ExperimentalUuidApi::class)
class PingService(
    private val repository: PingRepository
) : PingUseCase {

    private val logger = LoggerFactory.getLogger(PingService::class.java)

    @Transactional
    override fun executePing(message: String): Ping {
        logger.info("Executing ping with message: {}", message)

        // Domain Logic: Erstelle neue Entity (generiert UUID v7 automatisch)
        val ping = Ping(message = message)

        // Persistence
        return repository.save(ping)
    }

    @Transactional(readOnly = true)
    override fun getPingHistory(): List<Ping> {
        return repository.findAll()
    }

    @Transactional(readOnly = true)
    override fun getPing(id: Uuid): Ping? {
        return repository.findById(id)
    }
}
