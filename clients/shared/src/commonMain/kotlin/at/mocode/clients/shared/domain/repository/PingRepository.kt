package at.mocode.clients.shared.domain.repository

import at.mocode.clients.shared.domain.model.PingData
import at.mocode.clients.shared.domain.model.Resource

interface PingRepository {
  suspend fun checkSystemStatus(): Resource<PingData>
}
