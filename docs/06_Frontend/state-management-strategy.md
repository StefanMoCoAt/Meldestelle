---
type: Reference
status: DRAFT
owner: Frontend Expert
last_update: 2026-01-28
---

# Frontend State-Management Strategie (UDF)

Dieses Dokument beschreibt die empfohlene Strategie für das State Management in komplexen UI-Komponenten wie Formularen und Reports, um die Skalierbarkeit und Wartbarkeit des Frontends sicherzustellen.

## Problemstellung: Grenzen des einfachen State Managements

Für einfache Screens ist die Verwendung von mehreren `StateFlow`s in einem ViewModel ein valider Ansatz.

```kotlin
// Beispiel für einen einfachen Ansatz
class SimpleViewModel : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
}
```

Bei wachsender Komplexität (viele Felder, Validierungsregeln, asynchrone Aktionen) führt dieser Ansatz zu Problemen:
*   **Inkonsistente Zustände:** Es ist leicht, einen State zu vergessen (z.B. `isLoading` auf `false` zu setzen, aber die Fehlermeldung nicht zu löschen), was zu schwer nachvollziehbaren UI-Bugs führt.
*   **Schwere Testbarkeit:** Die Logik ist über viele Funktionen verteilt, die einzelne State-Variablen mutieren.
*   **Race Conditions:** Mehrere gleichzeitige, asynchrone Updates können den State unvorhersehbar machen.

## Lösungsstrategie: Unidirectional Data Flow (UDF)

Um diese Probleme zu lösen, wird die Einführung eines **Unidirectional Data Flow (UDF)** Musters für alle neuen, komplexen Features empfohlen.

UDF erzwingt einen strikten, vorhersagbaren Kreislauf:

1.  **State:** Ein **einziges, unveränderliches (immutable) `data class`** repräsentiert den gesamten Zustand der UI. Dies ist die *Single Source of Truth*.
2.  **Event/Intent:** Die UI ändert den Zustand niemals direkt. Stattdessen sendet sie ein **Event** (z.B. `SaveButtonClicked`), um eine Benutzeraktion zu signalisieren.
3.  **Logic (Reducer):** Eine zentrale Logik-Komponente (im ViewModel) empfängt das Event, führt Geschäftslogik aus und produziert einen **neuen, kompletten State**.

### Beispiel-Implementierung

**1. Ein einziges State-Objekt:**
```kotlin
data class FormState(
    val name: String = "",
    val email: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isFormValid: Boolean = false
)
```

**2. Klare Events von der UI:**
```kotlin
sealed interface FormEvent {
    data class NameChanged(val newName: String) : FormEvent
    object SaveButtonClicked : FormEvent
    object ErrorDismissed : FormEvent
}
```

**3. Zentrale Logik im ViewModel:**
```kotlin
class FormViewModel : ViewModel() {
    private val _state = MutableStateFlow(FormState())
    val state: StateFlow<FormState> = _state

    fun onEvent(event: FormEvent) {
        when (event) {
            is FormEvent.NameChanged -> {
                _state.update { it.copy(name = event.newName, isFormValid = /*...*/) }
            }
            is FormEvent.SaveButtonClicked -> {
                _state.update { it.copy(isSaving = true) }
                // ...
            }
        }
    }
}
```

## Empfohlene Bibliotheken

Die manuelle Implementierung von UDF ist möglich, aber dedizierte Bibliotheken bieten eine bewährte Struktur und reduzieren Boilerplate.

1.  **Voyager:**
    *   **Primär eine Navigationsbibliothek**, die aber ein leichtgewichtetes und pragmatisches UDF-System (`ScreenModel`) mitbringt.
    *   **Empfehlung:** **Der empfohlene Einstiegspunkt.** Da das Projekt ohnehin eine Navigationslösung benötigt, ist dies die effizienteste Wahl.

2.  **MVIKotlin:**
    *   Eine sehr mächtige, explizite MVI-Bibliothek (eine Form von UDF).
    *   **Empfehlung:** **In der Hinterhand behalten.** Für extrem komplexe Features, bei denen erweiterte Funktionen wie Time-Travel-Debugging den Mehraufwand rechtfertigen, kann MVIKotlin gezielt eingesetzt werden.

## Nächste Schritte

Diese Strategie muss nicht sofort für bestehende Screens umgesetzt werden. Sie soll als **Blaupause für alle zukünftigen, fachlichen Features** dienen, sobald das grundlegende SQLDelight-Sync-Problem gelöst ist.
