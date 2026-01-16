-- Database Schema Draft for Meldestelle (Offline-First)
-- Dialect: SQLite (compatible with SQLDelight)
-- Status: Draft / Proposal
-- Based on: OEPS Legacy Spec V2.4 & Domain Analysis

-- ==================================================================
-- 1. CORE INFRASTRUCTURE (Sync & Audit)
-- ==================================================================
-- Every table should ideally have these fields, but for brevity
-- they are implied or added where critical.
-- id: TEXT NOT NULL PRIMARY KEY (UUID)
-- created_at: INTEGER NOT NULL (Epoch Millis)
-- updated_at: INTEGER NOT NULL (Epoch Millis)
-- version: INTEGER NOT NULL (Optimistic Locking / Sync Counter)
-- is_deleted: INTEGER NOT NULL DEFAULT 0 (Soft Delete)

-- ==================================================================
-- 2. MASTER DATA (Stammdaten)
-- ==================================================================

-- Akteure: Personen und Organisationen
-- Covers: Reiter, Richter, Besitzer, Vereine
CREATE TABLE actor (
    id TEXT NOT NULL PRIMARY KEY,
    type TEXT NOT NULL, -- 'PERSON', 'ORGANIZATION'

    -- Display Data
    first_name TEXT, -- NULL for Organizations
    last_name TEXT NOT NULL, -- Name or Org-Name

    -- OEPS Specifics (Legacy Spec)
    oeps_id TEXT, -- 'Satznummer' (6 digits for Person, 4 for Club)
    oeps_category TEXT, -- 'Verein', 'Reiter', 'Richter'

    -- Licenses & Status
    license_code TEXT, -- e.g. 'R1', 'RD3'
    has_start_card INTEGER NOT NULL DEFAULT 0, -- Boolean: Paid annual fee?
    is_locked INTEGER NOT NULL DEFAULT 0, -- Boolean: Sperrliste?

    -- Contact & Meta
    nationality TEXT NOT NULL DEFAULT 'AUT', -- ISO 3-Letter
    contact_json TEXT, -- Address, Phone, Email

    -- Sync Meta
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_actor_oeps_id ON actor(oeps_id);
CREATE INDEX idx_actor_name ON actor(last_name, first_name);


-- Pferde
CREATE TABLE horse (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,

    -- Identification
    oeps_id TEXT, -- 'Satznummer' (10 digits) - CRITICAL for Export
    head_number_permanent TEXT, -- 'Kopfnummer' (e.g. A123)
    life_number TEXT, -- 'Lebensnummer' (Zucht)
    fei_id TEXT,

    -- Details
    birth_year INTEGER,
    gender TEXT, -- 'M', 'W', 'G' (Gelding/Wallach)
    color TEXT,
    sire_name TEXT, -- Vater (Denormalized for search)
    dam_name TEXT, -- Mutter

    -- Owner Link
    owner_id TEXT, -- FK to actor.id

    -- Status
    is_locked INTEGER NOT NULL DEFAULT 0, -- Sperrliste

    -- Sync Meta
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_horse_oeps_id ON horse(oeps_id);
CREATE INDEX idx_horse_head_num ON horse(head_number_permanent);
CREATE INDEX idx_horse_name ON horse(name);

-- ==================================================================
-- 3. EVENT STRUCTURE
-- ==================================================================

CREATE TABLE event (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    start_date INTEGER NOT NULL, -- Epoch Day
    end_date INTEGER NOT NULL,
    location TEXT,
    organizer_id TEXT NOT NULL, -- FK to actor.id

    status TEXT NOT NULL DEFAULT 'PLANNING' -- PLANNING, ACTIVE, ARCHIVED
);

CREATE TABLE tournament (
    id TEXT NOT NULL PRIMARY KEY,
    event_id TEXT NOT NULL REFERENCES event(id),

    -- OEPS Spec
    oeps_number TEXT NOT NULL, -- 5 digits (e.g. 21001)
    category TEXT, -- e.g. 'CSN-A'
    ruleset TEXT NOT NULL DEFAULT 'OETO', -- 'OETO', 'FEI'

    -- Sync Meta
    updated_at INTEGER NOT NULL
);

-- Bewerbe (Competitions)
-- Note: If a competition is split into 2 departments (Abteilungen),
-- we create 2 rows here to match the OEPS 'B-Satz' logic.
CREATE TABLE competition (
    id TEXT NOT NULL PRIMARY KEY,
    tournament_id TEXT NOT NULL REFERENCES tournament(id),

    -- Identification
    code_internal TEXT NOT NULL, -- '01', '02' (2 digits)
    code_official TEXT, -- '001' (3 digits, optional)
    division_id INTEGER NOT NULL DEFAULT 0, -- 'Abteilung' (0=None, 1=1st, 2=2nd)

    -- Description
    title TEXT NOT NULL,
    category TEXT, -- e.g. 'LM', 'S*'
    discipline TEXT NOT NULL, -- 'D', 'S', 'C' (Dressage, Jumping, Combined)

    -- Rules & Scoring
    scoring_method TEXT NOT NULL, -- 'A0', 'C', 'DRESSAGE_PERCENT'
    start_fee INTEGER NOT NULL DEFAULT 0, -- In Cents

    -- State
    status TEXT NOT NULL DEFAULT 'OPEN', -- OPEN, CLOSED_FOR_ENTRIES, RUNNING, FINISHED, SIGNED_OFF

    -- Sync Meta
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_comp_tournament ON competition(tournament_id);

-- ==================================================================
-- 4. SPORT & PROCESS
-- ==================================================================

-- Nennungen (Entries)
-- Represents the intent to start.
CREATE TABLE entry (
    id TEXT NOT NULL PRIMARY KEY,
    competition_id TEXT NOT NULL REFERENCES competition(id),

    -- The Pair
    horse_id TEXT NOT NULL REFERENCES horse(id),
    rider_id TEXT NOT NULL REFERENCES actor(id),

    -- Financials
    responsible_person_id TEXT REFERENCES actor(id), -- Who pays?
    fee_agreed INTEGER NOT NULL, -- In Cents (Snapshot of price at entry time)
    payment_status TEXT NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, WAIVED

    -- Validation Override (The "Human Factor")
    validation_status TEXT NOT NULL DEFAULT 'OK', -- OK, WARNING, BLOCKED
    override_comment TEXT, -- Why was this allowed despite warning?

    -- Sync Meta
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_entry_comp ON entry(competition_id);
CREATE INDEX idx_entry_rider ON entry(rider_id);

-- Startliste (Start Order)
-- Subset of entries that actually start.
CREATE TABLE start_list_entry (
    id TEXT NOT NULL PRIMARY KEY,
    entry_id TEXT NOT NULL REFERENCES entry(id),

    -- Ordering
    start_order INTEGER, -- 1, 2, 3...
    start_time_planned INTEGER, -- Epoch Millis (optional)

    -- Tournament Specifics
    head_number_event TEXT, -- Startnummer am Turnier (kann von A123 abweichen)

    -- Status
    status TEXT NOT NULL DEFAULT 'READY', -- READY, STARTED, DNS (Did Not Start)

    UNIQUE(entry_id)
);

-- Ergebnisse (Results)
CREATE TABLE result (
    id TEXT NOT NULL PRIMARY KEY,
    start_list_entry_id TEXT NOT NULL REFERENCES start_list_entry(id),

    -- The Outcome
    rank INTEGER, -- 1, 2, 3... (NULL if eliminated)

    -- Scoring Details (Polymorphic based on Competition Type)
    points_jump_faults DECIMAL(5,2), -- Springfehler
    time_taken_ms INTEGER, -- Zeit in Millisekunden
    score_dressage_percent DECIMAL(5,3), -- 72.500
    score_dressage_total DECIMAL(6,2), -- Summe Punkte

    -- Status Flags
    classification TEXT NOT NULL DEFAULT 'OK', -- OK, EL (Elim), RET (Retired), DIS (Disq)

    -- Detailed Marks (JSON)
    -- e.g. { "judge_C": 7.5, "judge_H": 7.2, "obstacles": [...] }
    details_json TEXT,

    -- Money
    prize_money INTEGER DEFAULT 0, -- In Cents

    -- Audit
    updated_by_user_id TEXT,
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_result_starter ON result(start_list_entry_id);

-- ==================================================================
-- 5. AUDIT LOG (NFR-07)
-- ==================================================================

CREATE TABLE audit_log (
    id TEXT NOT NULL PRIMARY KEY,
    entity_type TEXT NOT NULL, -- 'RESULT', 'ENTRY'
    entity_id TEXT NOT NULL,
    action TEXT NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE'

    user_id TEXT,
    timestamp INTEGER NOT NULL,

    changes_json TEXT -- { "score_old": 7.0, "score_new": 7.5 }
);
