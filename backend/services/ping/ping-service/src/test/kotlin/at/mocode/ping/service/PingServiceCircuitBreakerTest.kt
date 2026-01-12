package at.mocode.ping.service

import at.mocode.ping.application.PingService
import at.mocode.ping.domain.Ping
import at.mocode.ping.domain.PingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unit tests for the actual application service (`PingService`).
 *
 * The previous `PingServiceCircuitBreakerTest` referenced an outdated component.
 */
@OptIn(ExperimentalUuidApi::class)
class PingServiceCircuitBreakerTest {

  private val repository: PingRepository = mockk()
  private val service = PingService(repository)

  @Test
  fun `executePing should persist and return ping`() {
    every { repository.save(any()) } answers { firstArg() }

    val result = service.executePing("Hello")

    assertThat(result.message).isEqualTo("Hello")
    verify { repository.save(any()) }
  }

  @Test
  fun `getPingHistory should delegate to repository`() {
    every { repository.findAll() } returns emptyList()

    val result = service.getPingHistory()

    assertThat(result).isEmpty()
    verify { repository.findAll() }
  }

  @Test
  fun `getPing should delegate to repository`() {
    val id = Uuid.generateV7()
    val ping = Ping(id = id, message = "Hi")
    every { repository.findById(id) } returns ping

    val result = service.getPing(id)

    assertThat(result).isEqualTo(ping)
    verify { repository.findById(id) }
  }
}
