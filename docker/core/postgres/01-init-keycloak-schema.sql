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

-- Gewährt dem Benutzer „meldestelle“ alle Berechtigungen für das Schema.
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO "pg-user";

-- Gewährt die Nutzung des Schemas
GRANT USAGE ON SCHEMA keycloak TO "pg-user";

-- Standardberechtigungen für zukünftige Tabellen im Keycloak-Schema festlegen
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON TABLES TO "pg-user";
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON SEQUENCES TO "pg-user";
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON FUNCTIONS TO "pg-user";

-- Log successful schema Erstellung
DO $$
BEGIN
    RAISE NOTICE 'Keycloak schema created successfully in database';
END $$;
