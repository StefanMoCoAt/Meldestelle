-- ===================================================================
-- PostgreSQL Initialization Script for Keycloak
-- ===================================================================
-- This script creates a separate schema for Keycloak data within the
-- meldestelle database, providing isolation and better organization.
--
-- Execution: Automatically executed by PostgreSQL on first startup
-- via docker-entrypoint-initdb.d mechanism.
-- ===================================================================

-- Create Keycloak schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS keycloak;

-- Grant all privileges on the schema to the meldestelle user
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO meldestelle;

-- Grant usage on the schema
GRANT USAGE ON SCHEMA keycloak TO meldestelle;

-- Set default privileges for future tables in the keycloak schema
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON TABLES TO meldestelle;
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON SEQUENCES TO meldestelle;
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON FUNCTIONS TO meldestelle;

-- Log successful schema creation
DO $$
BEGIN
    RAISE NOTICE 'Keycloak schema created successfully';
    RAISE NOTICE 'Schema: keycloak';
    RAISE NOTICE 'Owner: meldestelle';
END $$;
