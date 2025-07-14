package at.mocode.di

import kotlin.reflect.KClass

/**
 * Service Locator interface for dependency injection.
 * Provides a centralized way to register and resolve dependencies across the application.
 */
interface ServiceLocator {

    /**
     * Register a service instance with the locator
     */
    fun <T : Any> register(serviceClass: KClass<T>, instance: T)

    /**
     * Register a service factory with the locator
     */
    fun <T : Any> register(serviceClass: KClass<T>, factory: () -> T)

    /**
     * Resolve a service instance from the locator
     */
    fun <T : Any> resolve(serviceClass: KClass<T>): T

    /**
     * Check if a service is registered
     */
    fun <T : Any> isRegistered(serviceClass: KClass<T>): Boolean

    /**
     * Clear all registered services
     */
    fun clear()
}

/**
 * Default implementation of ServiceLocator
 */
class DefaultServiceLocator : ServiceLocator {

    private val instances = mutableMapOf<KClass<*>, Any>()
    private val factories = mutableMapOf<KClass<*>, () -> Any>()

    override fun <T : Any> register(serviceClass: KClass<T>, instance: T) {
        instances[serviceClass] = instance
        factories.remove(serviceClass) // Remove factory if exists
    }

    override fun <T : Any> register(serviceClass: KClass<T>, factory: () -> T) {
        factories[serviceClass] = factory
        instances.remove(serviceClass) // Remove instance if exists
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolve(serviceClass: KClass<T>): T {
        // First check if we have a cached instance
        instances[serviceClass]?.let { return it as T }

        // Then check if we have a factory
        factories[serviceClass]?.let { factory ->
            val instance = factory() as T
            instances[serviceClass] = instance // Cache the instance
            return instance
        }

        throw IllegalArgumentException("Service ${serviceClass.simpleName} is not registered")
    }

    override fun <T : Any> isRegistered(serviceClass: KClass<T>): Boolean {
        return instances.containsKey(serviceClass) || factories.containsKey(serviceClass)
    }

    override fun clear() {
        instances.clear()
        factories.clear()
    }
}

/**
 * Global service locator instance
 */
object ServiceRegistry {
    private var _serviceLocator: ServiceLocator = DefaultServiceLocator()

    val serviceLocator: ServiceLocator
        get() = _serviceLocator

    /**
     * Set a custom service locator implementation
     */
    fun setServiceLocator(locator: ServiceLocator) {
        _serviceLocator = locator
    }

    /**
     * Reset to default service locator
     */
    fun reset() {
        _serviceLocator = DefaultServiceLocator()
    }
}

// Kotlin extension functions for easier usage
inline fun <reified T : Any> ServiceLocator.register(instance: T) {
    register(T::class, instance)
}

inline fun <reified T : Any> ServiceLocator.register(noinline factory: () -> T) {
    register(T::class, factory)
}

inline fun <reified T : Any> ServiceLocator.resolve(): T {
    return resolve(T::class)
}

inline fun <reified T : Any> ServiceLocator.isRegistered(): Boolean {
    return isRegistered(T::class)
}
