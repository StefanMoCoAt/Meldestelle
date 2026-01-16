CREATE TABLE ping (
    id UUID PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index für schnelle Sortierung nach Zeit (wichtig für Sync später)
CREATE INDEX idx_ping_created_at ON ping(created_at);
