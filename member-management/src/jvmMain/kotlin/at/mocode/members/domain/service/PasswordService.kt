package at.mocode.members.domain.service

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.*

/**
 * Service für die sichere Verarbeitung von Passwörtern.
 * Verwendet PBKDF2 mit HMAC SHA-512 für das Password Hashing.
 */
actual class PasswordService {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA512"
        private const val ITERATIONS = 65536
        private const val KEY_LENGTH = 512
        private const val SALT_LENGTH = 32
    }

    private val secureRandom = SecureRandom()

    /**
     * Generiert einen zufälligen Salt für das Passwort-Hashing.
     *
     * @return Base64-codierter Salt als String
     */
    actual fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    /**
     * Hasht ein Passwort mit dem angegebenen Salt.
     *
     * @param password Das zu hashende Passwort
     * @param salt Der zu verwendende Salt als Base64-String
     * @return Der Passwort-Hash als Base64-String
     */
    actual fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    /**
     * Überprüft, ob ein eingegebenes Passwort mit einem gespeicherten Hash übereinstimmt.
     *
     * @param inputPassword Das eingegebene Passwort
     * @param storedHash Der gespeicherte Passwort-Hash
     * @param storedSalt Der gespeicherte Salt
     * @return true, wenn das Passwort übereinstimmt, sonst false
     */
    actual fun verifyPassword(inputPassword: String, storedHash: String, storedSalt: String): Boolean {
        val calculatedHash = hashPassword(inputPassword, storedSalt)
        return calculatedHash == storedHash
    }

    /**
     * Generiert ein zufälliges, sicheres Passwort.
     *
     * @param length Die Länge des zu generierenden Passworts
     * @return Das generierte Passwort
     */
    actual fun generateRandomPassword(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.<>?"
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    /**
     * Überprüft die Stärke eines Passworts.
     *
     * @param password Das zu überprüfende Passwort
     * @return Ein PasswordStrength-Objekt mit Informationen zur Passwortstärke
     */
    actual fun checkPasswordStrength(password: String): PasswordStrength {
        val length = password.length
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        var score = 0
        if (length >= 8) score++
        if (length >= 12) score++
        if (hasLowercase) score++
        if (hasUppercase) score++
        if (hasDigit) score++
        if (hasSpecialChar) score++

        val strength = when {
            score <= 2 -> PasswordStrength.Strength.WEAK
            score <= 4 -> PasswordStrength.Strength.MEDIUM
            else -> PasswordStrength.Strength.STRONG
        }

        return PasswordStrength(
            strength = strength,
            score = score,
            maxScore = 6,
            issues = buildList {
                if (length < 8) add("Passwort sollte mindestens 8 Zeichen haben")
                if (!hasLowercase) add("Passwort sollte Kleinbuchstaben enthalten")
                if (!hasUppercase) add("Passwort sollte Großbuchstaben enthalten")
                if (!hasDigit) add("Passwort sollte Ziffern enthalten")
                if (!hasSpecialChar) add("Passwort sollte Sonderzeichen enthalten")
            }
        )
    }
}
