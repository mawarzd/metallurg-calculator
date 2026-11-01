CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    capacity_t REAL,
    description TEXT,
    manufacturer TEXT
);

CREATE TABLE IF NOT EXISTS ferroalloys (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    element TEXT NOT NULL,
    element_pct REAL NOT NULL,
    c_pct REAL DEFAULT 0,
    mn_pct REAL DEFAULT 0,
    si_pct REAL DEFAULT 0,
    s_pct REAL DEFAULT 0,
    p_pct REAL DEFAULT 0,
    cr_pct REAL DEFAULT 0,
    ni_pct REAL DEFAULT 0,
    mo_pct REAL DEFAULT 0,
    al_pct REAL DEFAULT 0,
    fe_pct REAL DEFAULT 0,
    burn_loss_pct REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS akos_calculations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    steel_grade TEXT NOT NULL,
    melt_mass_kg REAL NOT NULL,
    source_mode TEXT NOT NULL,
    input_json TEXT NOT NULL,
    result_json TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS mnlz_calculations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    steel_grade TEXT NOT NULL,
    converter_mass_t REAL NOT NULL,
    width_m REAL NOT NULL,
    thickness_m REAL NOT NULL,
    casting_speed_m_min REAL NOT NULL,
    casting_time_min REAL NOT NULL,
    strands INTEGER NOT NULL,
    liquid_pool_m REAL NOT NULL,
    radius_m REAL NOT NULL,
    height_m REAL NOT NULL,
    machine_type TEXT NOT NULL,
    source_mode TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_akos_grade ON akos_calculations(steel_grade);
CREATE INDEX IF NOT EXISTS idx_mnlz_grade ON mnlz_calculations(steel_grade);
CREATE INDEX IF NOT EXISTS idx_equipment_type ON equipment(type);
