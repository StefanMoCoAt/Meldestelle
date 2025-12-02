package at.mocode.shared.di

import at.mocode.shared.core.devConfig
import at.mocode.frontend.core.network.networkModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// Das Modul für die Config
val configModule = module {
  single { devConfig } // Später können wir hier PROD/DEV umschalten
}

// Basismodule, die immer geladen werden sollen (ohne Feature/Core-Cross-Imports)
val baseSharedModules = listOf(
  configModule,
  // Network module provides DI-only HttpClient (safe to be shared across features)
  networkModule
)

// Helper zum Starten von Koin (wird von der App aufgerufen)
// Weitere Module (z. B. networkModule) können über appDeclaration hinzugefügt werden.
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
  modules(baseSharedModules)
  appDeclaration()
}
