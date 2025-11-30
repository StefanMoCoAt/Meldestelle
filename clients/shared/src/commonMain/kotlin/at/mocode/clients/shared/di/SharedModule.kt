package at.mocode.clients.shared.di

import at.mocode.clients.shared.core.devConfig
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// Das Modul für die Config
val configModule = module {
  single { devConfig } // Später können wir hier PROD/DEV umschalten
}

// Alle Module zusammen
val sharedModules = listOf(
  configModule,
  networkModule
)

// Helper zum Starten von Koin (wird von der App aufgerufen)
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
  appDeclaration()
  modules(sharedModules)
}
