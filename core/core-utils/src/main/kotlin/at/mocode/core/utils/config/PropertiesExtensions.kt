package at.mocode.core.utils.config

import java.util.Properties

/**
 * Liest eine String-Property, wobei eine Umgebungsvariable Vorrang hat.
 *
 * @param key Der Schlüssel in der '.properties-Datei'.
 * @param envVar Der Name der Umgebungsvariable.
 * @param default Der Standardwert, falls weder Property noch Env-Var existieren.
 * @return Der geladene Konfigurationswert.
 */
fun Properties.getStringProperty(key: String, envVar: String, default: String): String {
    return System.getenv(envVar) ?: this.getProperty(key, default)
}

/**
 * Liest eine Integer-Property, wobei eine Umgebungsvariable Vorrang hat.
 */
fun Properties.getIntProperty(key: String, envVar: String, default: Int): Int {
    val value = System.getenv(envVar) ?: this.getProperty(key)
    return value?.toIntOrNull() ?: default
}

/**
 * Liest eine Boolean-Property, wobei eine Umgebungsvariable Vorrang hat.
 */
fun Properties.getBooleanProperty(key: String, envVar: String, default: Boolean): Boolean {
    val value = System.getenv(envVar) ?: this.getProperty(key)
    return value?.toBoolean() ?: default
}

/**
 * Liest eine Long-Property, wobei eine Umgebungsvariable Vorrang hat.
 */
fun Properties.getLongProperty(key: String, envVar: String, default: Long): Long {
    val value = System.getenv(envVar) ?: this.getProperty(key)
    return value?.toLongOrNull() ?: default
}
