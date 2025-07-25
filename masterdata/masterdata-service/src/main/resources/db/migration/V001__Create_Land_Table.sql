-- Migration V001: Create Land (Country) table
-- This migration creates the base table for country master data

CREATE TABLE IF NOT EXISTS land (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    iso_alpha2_code VARCHAR(2) NOT NULL,
    iso_alpha3_code VARCHAR(3) NOT NULL,
    iso_numerischer_code VARCHAR(3),
    name_deutsch VARCHAR(100) NOT NULL,
    name_englisch VARCHAR(100),
    wappen_url VARCHAR(500),
    ist_eu_mitglied BOOLEAN,
    ist_ewr_mitglied BOOLEAN,
    ist_aktiv BOOLEAN NOT NULL DEFAULT true,
    sortier_reihenfolge INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique indexes for ISO codes
CREATE UNIQUE INDEX IF NOT EXISTS uk_land_iso_alpha2 ON land(iso_alpha2_code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_land_iso_alpha3 ON land(iso_alpha3_code);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_land_aktiv ON land(ist_aktiv);
CREATE INDEX IF NOT EXISTS idx_land_sortierung ON land(sortier_reihenfolge);
CREATE INDEX IF NOT EXISTS idx_land_eu_mitglied ON land(ist_eu_mitglied);
CREATE INDEX IF NOT EXISTS idx_land_ewr_mitglied ON land(ist_ewr_mitglied);

-- Create index for name searches
CREATE INDEX IF NOT EXISTS idx_land_name_deutsch ON land(name_deutsch);
CREATE INDEX IF NOT EXISTS idx_land_name_englisch ON land(name_englisch);

-- Add comments for documentation
COMMENT ON TABLE land IS 'Master data table for countries/nations with ISO codes and EU/EWR membership information';
COMMENT ON COLUMN land.id IS 'Unique internal identifier (UUID)';
COMMENT ON COLUMN land.iso_alpha2_code IS '2-letter ISO 3166-1 Alpha-2 code (e.g., AT, DE)';
COMMENT ON COLUMN land.iso_alpha3_code IS '3-letter ISO 3166-1 Alpha-3 code (e.g., AUT, DEU)';
COMMENT ON COLUMN land.iso_numerischer_code IS '3-digit ISO 3166-1 numeric code (e.g., 040 for Austria)';
COMMENT ON COLUMN land.name_deutsch IS 'Official German name of the country';
COMMENT ON COLUMN land.name_englisch IS 'Official English name of the country';
COMMENT ON COLUMN land.wappen_url IS 'Optional URL path to country coat of arms or flag image';
COMMENT ON COLUMN land.ist_eu_mitglied IS 'Indicates if the country is a member of the European Union';
COMMENT ON COLUMN land.ist_ewr_mitglied IS 'Indicates if the country is a member of the European Economic Area';
COMMENT ON COLUMN land.ist_aktiv IS 'Indicates if this country is currently active/selectable in the system';
COMMENT ON COLUMN land.sortier_reihenfolge IS 'Optional number for controlling sort order in selection lists';
COMMENT ON COLUMN land.created_at IS 'Timestamp when this record was created';
COMMENT ON COLUMN land.updated_at IS 'Timestamp when this record was last updated';

-- Insert some initial data for common countries
INSERT INTO land (iso_alpha2_code, iso_alpha3_code, iso_numerischer_code, name_deutsch, name_englisch, ist_eu_mitglied, ist_ewr_mitglied, sortier_reihenfolge) VALUES
('AT', 'AUT', '040', 'Österreich', 'Austria', true, true, 1),
('DE', 'DEU', '276', 'Deutschland', 'Germany', true, true, 2),
('CH', 'CHE', '756', 'Schweiz', 'Switzerland', false, false, 3),
('IT', 'ITA', '380', 'Italien', 'Italy', true, true, 4),
('FR', 'FRA', '250', 'Frankreich', 'France', true, true, 5),
('CZ', 'CZE', '203', 'Tschechien', 'Czech Republic', true, true, 6),
('SK', 'SVK', '703', 'Slowakei', 'Slovakia', true, true, 7),
('SI', 'SVN', '705', 'Slowenien', 'Slovenia', true, true, 8),
('HU', 'HUN', '348', 'Ungarn', 'Hungary', true, true, 9),
('PL', 'POL', '616', 'Polen', 'Poland', true, true, 10)
ON CONFLICT (iso_alpha2_code) DO NOTHING;
