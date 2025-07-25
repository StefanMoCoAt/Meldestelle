-- Migration V004: Create Platz (Venue/Arena) table
-- This migration creates the table for tournament venues and arenas

CREATE TABLE IF NOT EXISTS platz (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    turnier_id UUID NOT NULL, -- Foreign key to tournament (not enforced as tournament might be in different module)
    name VARCHAR(200) NOT NULL,
    dimension VARCHAR(50),
    boden VARCHAR(100),
    typ VARCHAR(50) NOT NULL,
    ist_aktiv BOOLEAN NOT NULL DEFAULT true,
    sortier_reihenfolge INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_platz_sortier_reihenfolge CHECK (sortier_reihenfolge IS NULL OR sortier_reihenfolge >= 0)
);

-- Create unique constraint for name per tournament
CREATE UNIQUE INDEX IF NOT EXISTS uk_platz_name_turnier ON platz(name, turnier_id);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_platz_turnier ON platz(turnier_id);
CREATE INDEX IF NOT EXISTS idx_platz_aktiv ON platz(ist_aktiv);
CREATE INDEX IF NOT EXISTS idx_platz_typ ON platz(typ);
CREATE INDEX IF NOT EXISTS idx_platz_turnier_aktiv ON platz(turnier_id, ist_aktiv);
CREATE INDEX IF NOT EXISTS idx_platz_name ON platz(name);
CREATE INDEX IF NOT EXISTS idx_platz_dimension ON platz(dimension);
CREATE INDEX IF NOT EXISTS idx_platz_boden ON platz(boden);
CREATE INDEX IF NOT EXISTS idx_platz_sortierung ON platz(sortier_reihenfolge);

-- Add comments for documentation
COMMENT ON TABLE platz IS 'Master data table for tournament venues and arenas with type and dimension specifications';
COMMENT ON COLUMN platz.id IS 'Unique internal identifier (UUID)';
COMMENT ON COLUMN platz.turnier_id IS 'Foreign key reference to the tournament this venue belongs to';
COMMENT ON COLUMN platz.name IS 'Name or designation of the venue (e.g., "Hauptplatz", "Dressurplatz A")';
COMMENT ON COLUMN platz.dimension IS 'Dimensions of the venue (e.g., "20x60m", "20x40m")';
COMMENT ON COLUMN platz.boden IS 'Type of ground surface (e.g., "Sand", "Gras", "Kunststoff")';
COMMENT ON COLUMN platz.typ IS 'Type of venue (see PlatzTypE enum)';
COMMENT ON COLUMN platz.ist_aktiv IS 'Indicates if this venue can currently be used';
COMMENT ON COLUMN platz.sortier_reihenfolge IS 'Optional number for controlling sort order';
COMMENT ON COLUMN platz.created_at IS 'Timestamp when this record was created';
COMMENT ON COLUMN platz.updated_at IS 'Timestamp when this record was last updated';

-- Insert some example venue types and common configurations
-- Note: These are examples and would typically be created per tournament
-- Using a dummy tournament ID for demonstration purposes

-- Create a function to generate example venues for a tournament
CREATE OR REPLACE FUNCTION create_example_venues(tournament_id UUID) RETURNS VOID AS $$
BEGIN
    INSERT INTO platz (turnier_id, name, dimension, boden, typ, sortier_reihenfolge) VALUES
    -- Dressage arenas
    (tournament_id, 'Dressurplatz A', '20x60m', 'Sand', 'DRESSURPLATZ', 10),
    (tournament_id, 'Dressurplatz B', '20x40m', 'Sand', 'DRESSURPLATZ', 20),
    (tournament_id, 'Abreiteplatz Dressur', '20x40m', 'Sand', 'ABREITEPLATZ', 30),

    -- Jumping arenas
    (tournament_id, 'Springplatz Hauptring', '40x80m', 'Sand', 'SPRINGPLATZ', 40),
    (tournament_id, 'Springplatz Ring 2', '35x70m', 'Sand', 'SPRINGPLATZ', 50),
    (tournament_id, 'Abreiteplatz Springen', '30x60m', 'Sand', 'ABREITEPLATZ', 60),

    -- Cross-country and eventing
    (tournament_id, 'Geländestrecke', 'variabel', 'Gras', 'GELAENDESTRECKE', 70),
    (tournament_id, 'Vielseitigkeitsplatz', '25x65m', 'Sand', 'VIELSEITIGKEITSPLATZ', 80),

    -- Driving arenas
    (tournament_id, 'Fahrplatz', '40x100m', 'Sand', 'FAHRPLATZ', 90),
    (tournament_id, 'Hindernisfahren', '40x80m', 'Sand', 'FAHRPLATZ', 100),

    -- Vaulting
    (tournament_id, 'Voltigierplatz', '20m Durchmesser', 'Sand', 'VOLTIGIERPLATZ', 110),

    -- Training and warm-up areas
    (tournament_id, 'Führanlage', '20m Durchmesser', 'Sand', 'FUEHRANLAGE', 120),
    (tournament_id, 'Longierplatz', '20m Durchmesser', 'Sand', 'LONGIERPLATZ', 130),
    (tournament_id, 'Trainingsplatz 1', '20x40m', 'Sand', 'TRAININGSPLATZ', 140),
    (tournament_id, 'Trainingsplatz 2', '20x40m', 'Gras', 'TRAININGSPLATZ', 150),

    -- Indoor arenas
    (tournament_id, 'Reithalle A', '20x60m', 'Sand', 'REITHALLE', 160),
    (tournament_id, 'Reithalle B', '20x40m', 'Sand', 'REITHALLE', 170),

    -- Outdoor areas
    (tournament_id, 'Außenplatz 1', '25x50m', 'Gras', 'AUSSENPLATZ', 180),
    (tournament_id, 'Außenplatz 2', '20x40m', 'Sand', 'AUSSENPLATZ', 190),

    -- Special purpose areas
    (tournament_id, 'Siegerehrungsplatz', '15x25m', 'Gras', 'SONDERPLATZ', 200),
    (tournament_id, 'Vorführplatz', '20x30m', 'Sand', 'SONDERPLATZ', 210)

    ON CONFLICT (name, turnier_id) DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- Add some venue type validation comments
COMMENT ON FUNCTION create_example_venues(UUID) IS 'Helper function to create example venues for a tournament. Call with tournament UUID.';

-- Create a view for venue statistics
CREATE OR REPLACE VIEW platz_statistics AS
SELECT
    typ,
    COUNT(*) as total_count,
    COUNT(CASE WHEN ist_aktiv THEN 1 END) as active_count,
    COUNT(CASE WHEN NOT ist_aktiv THEN 1 END) as inactive_count,
    COUNT(DISTINCT turnier_id) as tournament_count,
    COUNT(DISTINCT dimension) as dimension_variants,
    COUNT(DISTINCT boden) as ground_type_variants
FROM platz
GROUP BY typ
ORDER BY typ;

COMMENT ON VIEW platz_statistics IS 'Statistical overview of venues by type, showing counts and variants';

-- Create a view for tournament venue overview
CREATE OR REPLACE VIEW tournament_venue_overview AS
SELECT
    turnier_id,
    COUNT(*) as total_venues,
    COUNT(CASE WHEN ist_aktiv THEN 1 END) as active_venues,
    COUNT(DISTINCT typ) as venue_types,
    COUNT(DISTINCT dimension) as dimension_variants,
    COUNT(DISTINCT boden) as ground_types,
    STRING_AGG(DISTINCT typ, ', ' ORDER BY typ) as available_types
FROM platz
GROUP BY turnier_id
ORDER BY turnier_id;

COMMENT ON VIEW tournament_venue_overview IS 'Overview of venues per tournament with summary statistics';

-- Example of how to use the function (commented out as it requires actual tournament IDs)
-- SELECT create_example_venues('550e8400-e29b-41d4-a716-446655440000'::UUID);

-- Add some helpful indexes for the views
CREATE INDEX IF NOT EXISTS idx_platz_typ_aktiv ON platz(typ, ist_aktiv);
CREATE INDEX IF NOT EXISTS idx_platz_turnier_typ ON platz(turnier_id, typ);
