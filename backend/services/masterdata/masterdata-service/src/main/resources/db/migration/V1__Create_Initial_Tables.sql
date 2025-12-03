-- File: V1__Create_Initial_Tables.sql

-- Tabelle zur Verwaltung der Vereine (Mandanten)
CREATE TABLE IF NOT EXISTS dom_verein (
                                          verein_id UUID PRIMARY KEY,
                                          oeps_vereins_nr VARCHAR(4) UNIQUE,
    name VARCHAR(100) NOT NULL,
    kuerzel VARCHAR(20),
    bundesland_code VARCHAR(2),
    daten_quelle VARCHAR(50) NOT NULL,
    ist_aktiv BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                             );

-- Tabelle zur Verwaltung der Personen (Sportler, Funktionäre)
CREATE TABLE IF NOT EXISTS dom_person (
                                          person_id UUID PRIMARY KEY,
                                          oeps_satz_nr VARCHAR(6) UNIQUE,
    nachname VARCHAR(100) NOT NULL,
    vorname VARCHAR(100) NOT NULL,
    geburtsdatum DATE,
    geschlecht VARCHAR(10),
    nationalitaet_code VARCHAR(3),
    stamm_verein_id UUID REFERENCES dom_verein(verein_id),
    ist_gesperrt BOOLEAN NOT NULL DEFAULT false,
    daten_quelle VARCHAR(50) NOT NULL,
    ist_aktiv BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                             );

-- Weitere Tabellen können hier hinzugefügt werden ...
