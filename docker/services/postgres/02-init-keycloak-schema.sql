-- ===================================================================
-- Keycloak Schema Init (No-Op)
-- ===================================================================
-- DEPRECATED: Schema initialization is handled by 01-init-keycloak-schema.sql.
-- This file remains to preserve execution order but performs no actions.
-- ===================================================================

DO $$
BEGIN
    RAISE NOTICE '02-init-keycloak-schema.sql is a no-op (handled by 01-init-keycloak-schema.sql)';
END $$;
