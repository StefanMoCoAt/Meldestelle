package at.mocode.infrastructure.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Konvertiert Keycloak-Rollen aus dem JWT (Realm Access & Resource Access)
 * in Spring Security GrantedAuthorities.
 *
 * Erwartetes Format im Token:
 * "realm_access": { "roles": ["admin", "user"] }
 * "resource_access": { "my-client": { "roles": ["client-role"] } }
 */
class KeycloakRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val roles = mutableSetOf<String>()

        // 1. Realm Roles extrahieren
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        if (realmAccess != null) {
            (realmAccess["roles"] as? List<*>)?.forEach { role ->
                if (role is String) {
                    roles.add(role)
                }
            }
        }

        // 2. Resource (Client) Roles extrahieren
        // Optional: Falls wir Client-spezifische Rollen brauchen.
        // Hier mappen wir vorerst nur Realm-Rollen global.

        // 3. Mapping zu GrantedAuthority (Prefix "ROLE_" ist Standard in Spring Security)
        return roles.map { role ->
            SimpleGrantedAuthority("ROLE_${role.uppercase()}")
        }
    }
}
