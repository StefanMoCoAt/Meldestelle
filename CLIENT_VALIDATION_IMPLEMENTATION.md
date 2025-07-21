# Client-Side Validation Implementation

## Übersicht

Dieses Dokument beschreibt die Implementierung der Client-seitigen Validierung für die Meldestelle-Anwendung. Die Validierung wurde gemäß der Anforderung "Implementiere Client-seitige Validierung" umgesetzt.

## Implementierungsansatz

Die Client-seitige Validierung nutzt die bereits vorhandenen Validierungsklassen aus dem `shared-kernel`-Modul, die in `commonMain` definiert sind und somit sowohl auf dem Server (JVM) als auch im Client (JS) verfügbar sind:

1. **ApiValidationUtils**: Enthält spezifische Validierungsmethoden für API-Anfragen
2. **ValidationUtils**: Enthält allgemeine Validierungsmethoden
3. **ValidationError**: Repräsentiert einen einzelnen Validierungsfehler
4. **ValidationResult**: Repräsentiert das Ergebnis einer Validierung

Durch die Nutzung dieser gemeinsamen Klassen wird sichergestellt, dass die Validierungslogik auf Client- und Serverseite konsistent ist.

## Beispielimplementierung

Als Beispiel wurde eine `LoginForm`-Komponente implementiert, die Client-seitige Validierung für Benutzername und Passwort durchführt, bevor die Daten an den Server gesendet werden.

### Validierungsablauf

1. Benutzer gibt Daten in das Formular ein
2. Bei Klick auf den Login-Button werden die Eingaben mit `ApiValidationUtils.validateLoginRequest()` validiert
3. Bei Validierungsfehlern werden diese angezeigt, ohne dass eine Serveranfrage gesendet wird
4. Nur bei erfolgreicher Validierung wird die Anfrage an den Server gesendet

### Code-Beispiel

```kotlin
// Perform client-side validation
val errors = ApiValidationUtils.validateLoginRequest(username, password)

if (errors.isNotEmpty()) {
    // If validation fails, update the validationErrors state
    validationErrors = errors
} else {
    // If validation passes, submit the form
    // ... API call code ...
}
```

### Fehleranzeige

Validierungsfehler werden benutzerfreundlich angezeigt:

```kotlin
// Display validation error for username if any
getFieldError("username")?.let {
    p {
        css {
            "color" to "#e74c3c"
            "fontSize" to "12px"
            "margin" to "5px 0 0 0"
        }
        +it
    }
}
```

## Vorteile der Client-seitigen Validierung

1. **Bessere Benutzererfahrung**: Sofortiges Feedback ohne Wartezeit auf Serverantwort
2. **Reduzierte Serverlast**: Ungültige Anfragen werden bereits im Client abgefangen
3. **Konsistente Validierung**: Gleiche Validierungslogik auf Client und Server
4. **Offline-Fähigkeit**: Grundlegende Validierung funktioniert auch ohne Serververbindung
5. **Erhöhte Sicherheit**: Doppelte Validierungsschicht (Client und Server)

## Implementierte Komponenten

### LoginForm

Eine React-Komponente, die als Web-Komponente registriert ist und in HTML verwendet werden kann:

```html
<login-form onloginsuccess="handleLoginSuccess"></login-form>
```

Die Komponente validiert:
- Benutzername/E-Mail (Pflichtfeld, Länge, E-Mail-Format wenn @ enthalten)
- Passwort (Pflichtfeld, Mindestlänge)

## Anleitung zur Implementierung weiterer Validierungen

### 1. Nutzung vorhandener Validierungsmethoden

Für die meisten Anwendungsfälle können die vorhandenen Methoden in `ApiValidationUtils` verwendet werden:

```kotlin
// Validierung von Login-Daten
ApiValidationUtils.validateLoginRequest(username, password)

// Validierung von Länder-Daten
ApiValidationUtils.validateCountryRequest(isoAlpha2Code, isoAlpha3Code, nameDeutsch, nameEnglisch)

// Validierung von Pferde-Daten
ApiValidationUtils.validateHorseRequest(pferdeName, lebensnummer, chipNummer, oepsNummer, feiNummer)

// Validierung von Veranstaltungs-Daten
ApiValidationUtils.validateEventRequest(name, ort, startDatum, endDatum, maxTeilnehmer)

// Validierung von Query-Parametern
ApiValidationUtils.validateQueryParameters(limit, offset, startDate, endDate, search, q)

// Validierung von UUID-Strings
ApiValidationUtils.validateUuidString(uuidString)
```

### 2. Implementierung eigener Validierungen

Für spezifische Validierungen können die Basismethoden in `ValidationUtils` verwendet werden:

```kotlin
// Prüfung auf nicht-leere Eingabe
ValidationUtils.validateNotBlank(value, fieldName)

// Längenvalidierung
ValidationUtils.validateLength(value, fieldName, maxLength, minLength)

// E-Mail-Validierung
ValidationUtils.validateEmail(email, fieldName)

// Telefonnummer-Validierung
ValidationUtils.validatePhoneNumber(phone, fieldName)

// Postleitzahl-Validierung
ValidationUtils.validatePostalCode(postalCode, fieldName)

// Ländercode-Validierung
ValidationUtils.validateCountryCode(countryCode, fieldName)

// Geburtsdatum-Validierung
ValidationUtils.validateBirthDate(birthDate, fieldName)

// Jahres-Validierung
ValidationUtils.validateYear(year, fieldName, minYear)
```

### 3. Anzeige von Validierungsfehlern

Validierungsfehler sollten benutzerfreundlich angezeigt werden:

```kotlin
// Hilfsfunktion zum Abrufen eines Fehlers für ein bestimmtes Feld
val getFieldError = { fieldName: String ->
    validationErrors.find { it.field == fieldName }?.message
}

// Anzeige des Fehlers
getFieldError("fieldName")?.let {
    // Fehleranzeige-Code
}
```

## Fazit

Die Client-seitige Validierung wurde erfolgreich implementiert und kann als Grundlage für weitere Formularvalidierungen dienen. Durch die Nutzung der gemeinsamen Validierungsklassen wird eine konsistente Benutzererfahrung und erhöhte Datensicherheit gewährleistet.

---

**Implementiert am**: 2025-07-21
**Status**: ✅ Vollständig implementiert
**Dokumentation**: ✅ Vollständig
