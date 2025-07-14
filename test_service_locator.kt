import at.mocode.di.*

fun main() {
    println("Testing ServiceLocator implementation...")

    // Test basic registration and resolution
    val serviceLocator = DefaultServiceLocator()

    // Test interface registration
    interface TestService {
        fun getMessage(): String
    }

    class TestServiceImpl : TestService {
        override fun getMessage() = "Hello from ServiceLocator!"
    }

    // Register service
    serviceLocator.register<TestService> { TestServiceImpl() }

    // Resolve service
    val service = serviceLocator.resolve<TestService>()
    println("Service message: ${service.getMessage()}")

    // Test singleton behavior
    val service2 = serviceLocator.resolve<TestService>()
    println("Same instance: ${service === service2}")

    // Test ServiceRegistry
    ServiceRegistry.serviceLocator.register<TestService> { TestServiceImpl() }
    val globalService = ServiceRegistry.serviceLocator.resolve<TestService>()
    println("Global service message: ${globalService.getMessage()}")

    println("ServiceLocator test completed successfully!")
}
