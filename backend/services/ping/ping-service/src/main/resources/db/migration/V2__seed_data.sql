-- Initiale Testdaten für den Ping-Service (damit Sync direkt was liefert)
-- IDs sind UUIDv7 kompatibel generiert

INSERT INTO ping (id, message, created_at) VALUES
('01948852-6d00-7000-8000-000000000001', 'System Start Ping', '2026-01-20 08:00:00+00'),
('01948852-6d00-7000-8000-000000000002', 'Database Migration Successful', '2026-01-20 08:05:00+00');
