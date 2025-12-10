-- ===================================================================
-- Keycloak Schema Init (No-Op)
-- ===================================================================
-- DEPRECATED: Schema-initialization erfolgt über die Datei 01-init-keycloak-schema.sql.
-- Diese Datei dient lediglich der Sicherstellung der Ausführungsreihenfolge, führt aber keine Aktionen aus.
-- ===================================================================

DO $$
BEGIN
    RAISE NOTICE '02-init-keycloak-schema.sql is a no-op (handled by 01-init-keycloak-schema.sql)';
END $$;
