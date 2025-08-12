package at.mocode.client.ui.features.ping

import at.mocode.client.data.api.PingApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PingViewModel {
    private val apiClient = PingApiClient()
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _responseText = MutableStateFlow("Klicke auf den Button, um das Backend zu pingen.")
    val responseText = _responseText.asStateFlow()

    fun onPingClicked() {
        _responseText.value = "Pinge Backend..."
        viewModelScope.launch {
            val response = apiClient.ping()
            _responseText.value = "Antwort vom Backend: $response"
        }
    }
}
