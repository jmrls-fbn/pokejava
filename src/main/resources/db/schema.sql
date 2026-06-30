-- Esquema SQLite del juego. A diferencia de la versión Postgres (schema +
-- constraints en archivos separados), aquí las llaves primarias, foráneas e
-- índices van INLINE en cada CREATE TABLE: SQLite casi no soporta
-- "ALTER TABLE ... ADD CONSTRAINT". DatabaseInitializer ejecuta este script
-- una sola vez, en la primera ejecución del juego, antes de cargar los CSV.

CREATE TABLE stats (
    id         INTEGER PRIMARY KEY,
    identifier TEXT
);

CREATE TABLE types (
    id         INTEGER PRIMARY KEY,
    identifier TEXT
);

-- Tabla de efectividad de tipos: cuánto daño hace un tipo atacante contra un
-- tipo defensor, en porcentaje (200 = supereficaz, 50 = poco eficaz, 0 = no
-- afecta). Solo se guardan los pares != 100; cualquier par ausente es neutral
-- (100 = 1x), así la tabla es más chica y el código asume 1x por defecto.
CREATE TABLE type_efficacy (
    damage_type_id INTEGER NOT NULL REFERENCES types (id),
    target_type_id INTEGER NOT NULL REFERENCES types (id),
    damage_factor  INTEGER NOT NULL,
    PRIMARY KEY (damage_type_id, target_type_id)
);

CREATE TABLE moves (
    id         INTEGER PRIMARY KEY,
    identifier TEXT,
    power      INTEGER,
    pp         INTEGER,
    type_id    INTEGER REFERENCES types (id)
);

CREATE TABLE pokemon (
    id              INTEGER PRIMARY KEY,
    identifier      TEXT,
    base_experience INTEGER
);

CREATE TABLE pokemon_stat (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon (id),
    stat_id    INTEGER NOT NULL REFERENCES stats (id),
    base_stat  INTEGER,
    PRIMARY KEY (pokemon_id, stat_id)
);

CREATE TABLE pokemon_moves (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon (id),
    move_id    INTEGER NOT NULL REFERENCES moves (id),
    level      INTEGER NOT NULL,
    PRIMARY KEY (pokemon_id, move_id, level)
);

-- Tabla puente Pokémon <-> tipos. Cada especie tiene 1 o 2 tipos; slot indica
-- el orden (1 = tipo principal, 2 = secundario).
CREATE TABLE pokemon_types (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon (id),
    type_id    INTEGER NOT NULL REFERENCES types (id),
    slot       INTEGER NOT NULL,
    PRIMARY KEY (pokemon_id, slot)
);

-- Cargada con datos pero todavía sin usar desde Java: el sistema de evolución
-- no está implementado (mismo estado que en la versión Postgres).
CREATE TABLE pokemon_evolution (
    id                 INTEGER PRIMARY KEY,
    pokemon_id         INTEGER REFERENCES pokemon (id),
    evolved_species_id INTEGER REFERENCES pokemon (id),
    minimum_level      INTEGER
);

CREATE TABLE player (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    name   VARCHAR(50),
    gender VARCHAR(10)
);

CREATE TABLE player_pokemon (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id  INTEGER REFERENCES player (id),
    pokemon_id INTEGER REFERENCES pokemon (id),
    level      INTEGER,
    current_hp INTEGER,
    team_slot  INTEGER
);

CREATE INDEX idx_pokemon_moves_pokemon ON pokemon_moves (pokemon_id, level DESC);
CREATE INDEX idx_player_pokemon_player ON player_pokemon (player_id);
