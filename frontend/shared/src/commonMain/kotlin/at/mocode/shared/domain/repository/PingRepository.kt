package at.mocode.shared.domain.repository

import at.mocode.shared.domain.model.PingData
import at.mocode.shared.domain.model.Resource

interface PingRepository {
  suspend fun checkSystemStatus(): Resource<PingData>
}
