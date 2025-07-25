-- Migration V002: Create Bundesland (Federal State) table
-- This migration creates the table for federal states/regions with OEPS and ISO codes

CREATE TABLE IF NOT EXISTS bundesland (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    land_id UUID NOT NULL REFERENCES land(id) ON DELETE CASCADE,
    oeps_code VARCHAR(10),
    iso_3166_2_code VARCHAR(10),
    name VARCHAR(100) NOT NULL,
    kuerzel VARCHAR(10),
    wappen_url VARCHAR(500),
    ist_aktiv BOOLEAN NOT NULL DEFAULT true,
    sortier_reihenfolge INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique constraints
CREATE UNIQUE INDEX IF NOT EXISTS uk_bundesland_oeps_land ON bundesland(oeps_code, land_id) WHERE oeps_code IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_bundesland_iso3166_2 ON bundesland(iso_3166_2_code) WHERE iso_3166_2_code IS NOT NULL;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_bundesland_land_id ON bundesland(land_id);
CREATE INDEX IF NOT EXISTS idx_bundesland_aktiv ON bundesland(ist_aktiv);
CREATE INDEX IF NOT EXISTS idx_bundesland_sortierung ON bundesland(sortier_reihenfolge);
CREATE INDEX IF NOT EXISTS idx_bundesland_name ON bundesland(name);
CREATE INDEX IF NOT EXISTS idx_bundesland_land_aktiv ON bundesland(land_id, ist_aktiv);

-- Add comments for documentation
COMMENT ON TABLE bundesland IS 'Master data table for federal states/regions with OEPS and ISO 3166-2 codes';
COMMENT ON COLUMN bundesland.id IS 'Unique internal identifier (UUID)';
COMMENT ON COLUMN bundesland.land_id IS 'Foreign key reference to the country this federal state belongs to';
COMMENT ON COLUMN bundesland.oeps_code IS '2-digit OEPS code for Austrian federal states (e.g., 01 for Vienna, 02 for Lower Austria)';
COMMENT ON COLUMN bundesland.iso_3166_2_code IS 'Official ISO 3166-2 code for the federal state (e.g., AT-1 for Burgenland, DE-BY for Bavaria)';
COMMENT ON COLUMN bundesland.name IS 'Official name of the federal state';
COMMENT ON COLUMN bundesland.kuerzel IS 'Common abbreviation for the federal state (e.g., NÖ, W, STMK)';
COMMENT ON COLUMN bundesland.wappen_url IS 'Optional URL path to federal state coat of arms image';
COMMENT ON COLUMN bundesland.ist_aktiv IS 'Indicates if this federal state is currently active/selectable in the system';
COMMENT ON COLUMN bundesland.sortier_reihenfolge IS 'Optional number for controlling sort order in selection lists';
COMMENT ON COLUMN bundesland.created_at IS 'Timestamp when this record was created';
COMMENT ON COLUMN bundesland.updated_at IS 'Timestamp when this record was last updated';

-- Insert Austrian federal states (Bundesländer)
-- First, get the Austria country ID
DO $$
DECLARE
    austria_id UUID;
BEGIN
    SELECT id INTO austria_id FROM land WHERE iso_alpha2_code = 'AT';

    IF austria_id IS NOT NULL THEN
        INSERT INTO bundesland (land_id, oeps_code, iso_3166_2_code, name, kuerzel, sortier_reihenfolge) VALUES
        (austria_id, '01', 'AT-1', 'Burgenland', 'BGLD', 1),
        (austria_id, '02', 'AT-2', 'Kärnten', 'KTN', 2),
        (austria_id, '03', 'AT-3', 'Niederösterreich', 'NÖ', 3),
        (austria_id, '04', 'AT-4', 'Oberösterreich', 'OÖ', 4),
        (austria_id, '05', 'AT-5', 'Salzburg', 'SBG', 5),
        (austria_id, '06', 'AT-6', 'Steiermark', 'STMK', 6),
        (austria_id, '07', 'AT-7', 'Tirol', 'T', 7),
        (austria_id, '08', 'AT-8', 'Vorarlberg', 'VBG', 8),
        (austria_id, '09', 'AT-9', 'Wien', 'W', 9)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- Insert German federal states (Bundesländer)
DO $$
DECLARE
    germany_id UUID;
BEGIN
    SELECT id INTO germany_id FROM land WHERE iso_alpha2_code = 'DE';

    IF germany_id IS NOT NULL THEN
        INSERT INTO bundesland (land_id, iso_3166_2_code, name, kuerzel, sortier_reihenfolge) VALUES
        (germany_id, 'DE-BW', 'Baden-Württemberg', 'BW', 1),
        (germany_id, 'DE-BY', 'Bayern', 'BY', 2),
        (germany_id, 'DE-BE', 'Berlin', 'BE', 3),
        (germany_id, 'DE-BB', 'Brandenburg', 'BB', 4),
        (germany_id, 'DE-HB', 'Bremen', 'HB', 5),
        (germany_id, 'DE-HH', 'Hamburg', 'HH', 6),
        (germany_id, 'DE-HE', 'Hessen', 'HE', 7),
        (germany_id, 'DE-MV', 'Mecklenburg-Vorpommern', 'MV', 8),
        (germany_id, 'DE-NI', 'Niedersachsen', 'NI', 9),
        (germany_id, 'DE-NW', 'Nordrhein-Westfalen', 'NW', 10),
        (germany_id, 'DE-RP', 'Rheinland-Pfalz', 'RP', 11),
        (germany_id, 'DE-SL', 'Saarland', 'SL', 12),
        (germany_id, 'DE-SN', 'Sachsen', 'SN', 13),
        (germany_id, 'DE-ST', 'Sachsen-Anhalt', 'ST', 14),
        (germany_id, 'DE-SH', 'Schleswig-Holstein', 'SH', 15),
        (germany_id, 'DE-TH', 'Thüringen', 'TH', 16)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- Insert Swiss cantons
DO $$
DECLARE
    switzerland_id UUID;
BEGIN
    SELECT id INTO switzerland_id FROM land WHERE iso_alpha2_code = 'CH';

    IF switzerland_id IS NOT NULL THEN
        INSERT INTO bundesland (land_id, iso_3166_2_code, name, kuerzel, sortier_reihenfolge) VALUES
        (switzerland_id, 'CH-AG', 'Aargau', 'AG', 1),
        (switzerland_id, 'CH-AI', 'Appenzell Innerrhoden', 'AI', 2),
        (switzerland_id, 'CH-AR', 'Appenzell Ausserrhoden', 'AR', 3),
        (switzerland_id, 'CH-BE', 'Bern', 'BE', 4),
        (switzerland_id, 'CH-BL', 'Basel-Landschaft', 'BL', 5),
        (switzerland_id, 'CH-BS', 'Basel-Stadt', 'BS', 6),
        (switzerland_id, 'CH-FR', 'Freiburg', 'FR', 7),
        (switzerland_id, 'CH-GE', 'Genf', 'GE', 8),
        (switzerland_id, 'CH-GL', 'Glarus', 'GL', 9),
        (switzerland_id, 'CH-GR', 'Graubünden', 'GR', 10),
        (switzerland_id, 'CH-JU', 'Jura', 'JU', 11),
        (switzerland_id, 'CH-LU', 'Luzern', 'LU', 12),
        (switzerland_id, 'CH-NE', 'Neuenburg', 'NE', 13),
        (switzerland_id, 'CH-NW', 'Nidwalden', 'NW', 14),
        (switzerland_id, 'CH-OW', 'Obwalden', 'OW', 15),
        (switzerland_id, 'CH-SG', 'St. Gallen', 'SG', 16),
        (switzerland_id, 'CH-SH', 'Schaffhausen', 'SH', 17),
        (switzerland_id, 'CH-SO', 'Solothurn', 'SO', 18),
        (switzerland_id, 'CH-SZ', 'Schwyz', 'SZ', 19),
        (switzerland_id, 'CH-TG', 'Thurgau', 'TG', 20),
        (switzerland_id, 'CH-TI', 'Tessin', 'TI', 21),
        (switzerland_id, 'CH-UR', 'Uri', 'UR', 22),
        (switzerland_id, 'CH-VD', 'Waadt', 'VD', 23),
        (switzerland_id, 'CH-VS', 'Wallis', 'VS', 24),
        (switzerland_id, 'CH-ZG', 'Zug', 'ZG', 25),
        (switzerland_id, 'CH-ZH', 'Zürich', 'ZH', 26)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;
