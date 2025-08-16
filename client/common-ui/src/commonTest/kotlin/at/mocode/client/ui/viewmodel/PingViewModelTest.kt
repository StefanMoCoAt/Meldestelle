package at.mocode.client.ui.viewmodel

import at.mocode.client.data.service.PingResponse
import at.mocode.client.data.service.PingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

class PingViewModelTest {

    @Test
    fun `should create PingUiState sealed class instances`() {
        // When & Then
        val initial = PingUiState.Initial
        val loading = PingUiState.Loading
        val success = PingUiState.Success(PingResponse("pong"))
        val error = PingUiState.Error("Test error")

        assertNotNull(initial)
        assertNotNull(loading)
        assertNotNull(success)
        assertNotNull(error)
    }

    @Test
    fun `should have correct PingUiState Success data`() {
        // Given
        val response = PingResponse("pong")

        // When
        val successState = PingUiState.Success(response)

        // Then
        assertEquals("pong", successState.response.status)
    }

    @Test
    fun `should have correct PingUiState Error message`() {
        // Given
        val errorMessage = "Network connection failed"

        // When
        val errorState = PingUiState.Error(errorMessage)

        // Then
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun `should create ViewModel with initial state`() {
        // Given
        val pingService = PingService("http://test-server")
        val testScope = CoroutineScope(Dispatchers.Default)

        // When
        val viewModel = PingViewModel(pingService, testScope)

        // Then
        assertTrue(viewModel.uiState is PingUiState.Initial)

        // Cleanup
        testScope.cancel()
        pingService.close()
    }

    @Test
    fun `should transition to Loading state when pingBackend is called`() {
        // Given
        val pingService = PingService("http://unreachable-server")
        val testScope = CoroutineScope(Dispatchers.Default)
        val viewModel = PingViewModel(pingService, testScope)

        // When
        viewModel.pingBackend()

        // Then - Should immediately transition to Loading
        assertTrue(viewModel.uiState is PingUiState.Loading)

        // Cleanup
        testScope.cancel()
        pingService.close()
    }

    @Test
    fun `should dispose without throwing exceptions`() {
        // Given
        val pingService = PingService("http://test")
        val testScope = CoroutineScope(Dispatchers.Default)
        val viewModel = PingViewModel(pingService, testScope)

        // When & Then - Should complete without exceptions
        assertDoesNotThrow { viewModel.dispose() }
    }

    @Test
    fun `should preserve uiState immutability`() {
        // Given
        val pingService = PingService("http://test")
        val testScope = CoroutineScope(Dispatchers.Default)
        val viewModel = PingViewModel(pingService, testScope)

        // When
        val initialState = viewModel.uiState

        // Then - uiState should be immutable (no setter accessible from outside)
        assertTrue(initialState is PingUiState.Initial)
        // The uiState property should be read-only from external access
        // This is enforced by the private setter in the ViewModel

        // Cleanup
        testScope.cancel()
        pingService.close()
    }

    @Test
    fun `should handle different service configurations`() {
        // Given - Different service configurations
        val service1 = PingService("http://server1")
        val service2 = PingService("https://server2:8443")
        val testScope1 = CoroutineScope(Dispatchers.Default)
        val testScope2 = CoroutineScope(Dispatchers.Default)

        // When
        val viewModel1 = PingViewModel(service1, testScope1)
        val viewModel2 = PingViewModel(service2, testScope2)

        // Then
        assertTrue(viewModel1.uiState is PingUiState.Initial)
        assertTrue(viewModel2.uiState is PingUiState.Initial)

        // Cleanup
        testScope1.cancel()
        testScope2.cancel()
        service1.close()
        service2.close()
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Expected no exception, but got: ${e.message}")
        }
    }
}
