-- ===================================================================
-- PostgreSQL Initialization Script for Keycloak
-- ===================================================================
-- Dieses Skript erstellt ein separates Schema für Keycloak-Daten innerhalb der
-- meldestelle-Datenbank und sorgt so für Isolation und bessere Organisation.
--
-- Ausführung: Wird automatisch von PostgreSQL beim ersten Start ausgeführt
-- über den docker-entrypoint-initdb.d-Mechanismus.
-- ===================================================================

-- Erstellt das Keycloak-Schema, falls es noch nicht existiert.
CREATE SCHEMA IF NOT EXISTS keycloak;

-- Der User, der dieses Skript ausführt (definiert durch POSTGRES_USER),
-- ist automatisch Owner. Wir stellen sicher, dass er alle Rechte hat.
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO current_user;

-- Wir setzen die Default-Privilegien für zukünftige Tabellen,
-- damit der aktuelle User (der auch der App-User ist) Zugriff hat.
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON TABLES TO current_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON SEQUENCES TO current_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON FUNCTIONS TO current_user;

-- Log successful schema Erstellung
DO $$
BEGIN
    RAISE NOTICE 'Keycloak schema created successfully in database';
END $$;
