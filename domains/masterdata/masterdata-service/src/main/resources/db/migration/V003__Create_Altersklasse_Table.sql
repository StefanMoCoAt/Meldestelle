-- Migration V003: Create Altersklasse (Age Class) table
-- This migration creates the table for age class definitions with sport and gender filters

CREATE TABLE IF NOT EXISTS altersklasse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    altersklasse_code VARCHAR(50) NOT NULL UNIQUE,
    bezeichnung VARCHAR(200) NOT NULL,
    min_alter INTEGER,
    max_alter INTEGER,
    stichtag_regel_text VARCHAR(500),
    sparte_filter VARCHAR(50),
    geschlecht_filter CHAR(1),
    oeto_regel_referenz_id UUID,
    ist_aktiv BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_altersklasse_geschlecht CHECK (geschlecht_filter IN ('M', 'W') OR geschlecht_filter IS NULL),
    CONSTRAINT chk_altersklasse_alter_range CHECK (min_alter IS NULL OR max_alter IS NULL OR min_alter <= max_alter),
    CONSTRAINT chk_altersklasse_min_alter CHECK (min_alter IS NULL OR min_alter >= 0),
    CONSTRAINT chk_altersklasse_max_alter CHECK (max_alter IS NULL OR max_alter >= 0)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_altersklasse_aktiv ON altersklasse(ist_aktiv);
CREATE INDEX IF NOT EXISTS idx_altersklasse_sparte ON altersklasse(sparte_filter);
CREATE INDEX IF NOT EXISTS idx_altersklasse_geschlecht ON altersklasse(geschlecht_filter);
CREATE INDEX IF NOT EXISTS idx_altersklasse_alter ON altersklasse(min_alter, max_alter);
CREATE INDEX IF NOT EXISTS idx_altersklasse_bezeichnung ON altersklasse(bezeichnung);
CREATE INDEX IF NOT EXISTS idx_altersklasse_code ON altersklasse(altersklasse_code);

-- Add comments for documentation
COMMENT ON TABLE altersklasse IS 'Master data table for age class definitions with eligibility rules for participants';
COMMENT ON COLUMN altersklasse.id IS 'Unique internal identifier (UUID)';
COMMENT ON COLUMN altersklasse.altersklasse_code IS 'Unique code for the age class (e.g., JGD_U16, JUN_U18, YR_U21, AK)';
COMMENT ON COLUMN altersklasse.bezeichnung IS 'Official or commonly understood designation of the age class';
COMMENT ON COLUMN altersklasse.min_alter IS 'Minimum age (years, inclusive) for this age class. NULL if no lower limit';
COMMENT ON COLUMN altersklasse.max_alter IS 'Maximum age (years, inclusive) for this age class. NULL if no upper limit';
COMMENT ON COLUMN altersklasse.stichtag_regel_text IS 'Description of the rule for the reference date for age calculation';
COMMENT ON COLUMN altersklasse.sparte_filter IS 'Optional specification if this age class definition only applies to a specific sport';
COMMENT ON COLUMN altersklasse.geschlecht_filter IS 'Optional filter for gender (M, W) if the age class is gender-specific. NULL means valid for all genders';
COMMENT ON COLUMN altersklasse.oeto_regel_referenz_id IS 'Optional link to a specific rule in the OETO rule reference table';
COMMENT ON COLUMN altersklasse.ist_aktiv IS 'Indicates if this age class definition can currently be used in the system';
COMMENT ON COLUMN altersklasse.created_at IS 'Timestamp when this record was created';
COMMENT ON COLUMN altersklasse.updated_at IS 'Timestamp when this record was last updated';

-- Insert common age class definitions for equestrian sports
INSERT INTO altersklasse (altersklasse_code, bezeichnung, min_alter, max_alter, stichtag_regel_text, sparte_filter, geschlecht_filter) VALUES
-- General age classes (all sports)
('PONY_U10', 'Pony Führzügel U10', NULL, 9, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('PONY_U12', 'Pony U12', NULL, 11, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('PONY_U14', 'Pony U14', NULL, 13, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('PONY_U16', 'Pony U16', NULL, 15, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('JGD_U16', 'Jugend U16', NULL, 15, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('JGD_U18', 'Jugend U18', NULL, 17, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('JUN_U21', 'Junioren U21', NULL, 20, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('YR_U25', 'Junge Reiter U25', NULL, 24, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('AK', 'Allgemeine Klasse', 18, NULL, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('SEN_40', 'Senioren Ü40', 40, NULL, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('SEN_50', 'Senioren Ü50', 50, NULL, '31.12. des laufenden Kalenderjahres', NULL, NULL),
('SEN_60', 'Senioren Ü60', 60, NULL, '31.12. des laufenden Kalenderjahres', NULL, NULL),

-- Dressage-specific age classes
('DR_PONY_U12', 'Dressur Pony U12', NULL, 11, '31.12. des laufenden Kalenderjahres', 'DRESSUR', NULL),
('DR_PONY_U14', 'Dressur Pony U14', NULL, 13, '31.12. des laufenden Kalenderjahres', 'DRESSUR', NULL),
('DR_PONY_U16', 'Dressur Pony U16', NULL, 15, '31.12. des laufenden Kalenderjahres', 'DRESSUR', NULL),
('DR_JGD_U18', 'Dressur Jugend U18', NULL, 17, '31.12. des laufenden Kalenderjahres', 'DRESSUR', NULL),
('DR_JUN_U21', 'Dressur Junioren U21', NULL, 20, '31.12. des laufenden Kalenderjahres', 'DRESSUR', NULL),
('DR_YR_U25', 'Dressur Junge Reiter U25', NULL, 24, '31.12. des laufenden Kalenderjahres', 'DRESSUR', NULL),

-- Jumping-specific age classes
('SP_PONY_U12', 'Springen Pony U12', NULL, 11, '31.12. des laufenden Kalenderjahres', 'SPRINGEN', NULL),
('SP_PONY_U14', 'Springen Pony U14', NULL, 13, '31.12. des laufenden Kalenderjahres', 'SPRINGEN', NULL),
('SP_PONY_U16', 'Springen Pony U16', NULL, 15, '31.12. des laufenden Kalenderjahres', 'SPRINGEN', NULL),
('SP_JGD_U18', 'Springen Jugend U18', NULL, 17, '31.12. des laufenden Kalenderjahres', 'SPRINGEN', NULL),
('SP_JUN_U21', 'Springen Junioren U21', NULL, 20, '31.12. des laufenden Kalenderjahres', 'SPRINGEN', NULL),
('SP_YR_U25', 'Springen Junge Reiter U25', NULL, 24, '31.12. des laufenden Kalenderjahres', 'SPRINGEN', NULL),

-- Eventing-specific age classes
('VK_PONY_U14', 'Vielseitigkeit Pony U14', NULL, 13, '31.12. des laufenden Kalenderjahres', 'VIELSEITIGKEIT', NULL),
('VK_PONY_U16', 'Vielseitigkeit Pony U16', NULL, 15, '31.12. des laufenden Kalenderjahres', 'VIELSEITIGKEIT', NULL),
('VK_JGD_U18', 'Vielseitigkeit Jugend U18', NULL, 17, '31.12. des laufenden Kalenderjahres', 'VIELSEITIGKEIT', NULL),
('VK_JUN_U21', 'Vielseitigkeit Junioren U21', NULL, 20, '31.12. des laufenden Kalenderjahres', 'VIELSEITIGKEIT', NULL),
('VK_YR_U25', 'Vielseitigkeit Junge Reiter U25', NULL, 24, '31.12. des laufenden Kalenderjahres', 'VIELSEITIGKEIT', NULL),

-- Driving-specific age classes
('FA_PONY_U16', 'Fahren Pony U16', NULL, 15, '31.12. des laufenden Kalenderjahres', 'FAHREN', NULL),
('FA_JGD_U18', 'Fahren Jugend U18', NULL, 17, '31.12. des laufenden Kalenderjahres', 'FAHREN', NULL),
('FA_JUN_U21', 'Fahren Junioren U21', NULL, 20, '31.12. des laufenden Kalenderjahres', 'FAHREN', NULL),
('FA_YR_U25', 'Fahren Junge Reiter U25', NULL, 24, '31.12. des laufenden Kalenderjahres', 'FAHREN', NULL),

-- Vaulting-specific age classes
('VT_U10', 'Voltigieren U10', NULL, 9, '31.12. des laufenden Kalenderjahres', 'VOLTIGIEREN', NULL),
('VT_U12', 'Voltigieren U12', NULL, 11, '31.12. des laufenden Kalenderjahres', 'VOLTIGIEREN', NULL),
('VT_U14', 'Voltigieren U14', NULL, 13, '31.12. des laufenden Kalenderjahres', 'VOLTIGIEREN', NULL),
('VT_U16', 'Voltigieren U16', NULL, 15, '31.12. des laufenden Kalenderjahres', 'VOLTIGIEREN', NULL),
('VT_U18', 'Voltigieren U18', NULL, 17, '31.12. des laufenden Kalenderjahres', 'VOLTIGIEREN', NULL),
('VT_JUN_U21', 'Voltigieren Junioren U21', NULL, 20, '31.12. des laufenden Kalenderjahres', 'VOLTIGIEREN', NULL),

-- Gender-specific examples (if needed)
('DR_DAMEN', 'Dressur Damen', 18, NULL, '31.12. des laufenden Kalenderjahres', 'DRESSUR', 'W'),
('DR_HERREN', 'Dressur Herren', 18, NULL, '31.12. des laufenden Kalenderjahres', 'DRESSUR', 'M')

ON CONFLICT (altersklasse_code) DO NOTHING;
