-- Testcontainers an init script for Keycloak schema
-- Creates the schema and basic privileges for the test DB user

CREATE SCHEMA IF NOT EXISTS keycloak;

GRANT USAGE ON SCHEMA keycloak TO meldestelle;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA keycloak TO meldestelle;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA keycloak TO meldestelle;

ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak
    GRANT ALL PRIVILEGES ON TABLES TO meldestelle;

ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak
    GRANT ALL PRIVILEGES ON SEQUENCES TO meldestelle;

DO $$
BEGIN
    RAISE NOTICE 'Test Keycloak schema initialized';
END $$;
