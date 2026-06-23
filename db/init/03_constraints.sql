-- Constraints e índices, agregados DESPUÉS de cargar los datos: si una FK
-- fallara por datos inconsistentes es más fácil depurarlo así que con la carga
-- bloqueada a mitad de camino.

ALTER TABLE stats
    ADD CONSTRAINT pk_stats PRIMARY KEY (id);

ALTER TABLE moves
    ADD CONSTRAINT pk_moves PRIMARY KEY (id);

ALTER TABLE pokemon
    ADD CONSTRAINT pk_pokemon PRIMARY KEY (id);

ALTER TABLE pokemon_stat
    ADD CONSTRAINT pk_pokemon_stat PRIMARY KEY (pokemon_id, stat_id),
    ADD CONSTRAINT fk_pokemon_stat_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon (id),
    ADD CONSTRAINT fk_pokemon_stat_stat    FOREIGN KEY (stat_id)    REFERENCES stats (id);

ALTER TABLE pokemon_moves
    ADD CONSTRAINT pk_pokemon_moves PRIMARY KEY (pokemon_id, move_id, level),
    ADD CONSTRAINT fk_pokemon_moves_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon (id),
    ADD CONSTRAINT fk_pokemon_moves_move    FOREIGN KEY (move_id)    REFERENCES moves (id);
CREATE INDEX idx_pokemon_moves_pokemon ON pokemon_moves (pokemon_id, level DESC);

ALTER TABLE pokemon_evolution
    ADD CONSTRAINT pk_pokemon_evolution PRIMARY KEY (id),
    ADD CONSTRAINT fk_evolution_pokemon FOREIGN KEY (pokemon_id)         REFERENCES pokemon (id),
    ADD CONSTRAINT fk_evolution_evolved FOREIGN KEY (evolved_species_id) REFERENCES pokemon (id);

ALTER TABLE player
    ADD CONSTRAINT pk_player PRIMARY KEY (id);

ALTER TABLE player_pokemon
    ADD CONSTRAINT pk_player_pokemon PRIMARY KEY (id),
    ADD CONSTRAINT fk_player_pokemon_player FOREIGN KEY (player_id) REFERENCES player (id),
    ADD CONSTRAINT fk_player_pokemon_pokemon FOREIGN KEY (pokemon_id) REFERENCES pokemon (id);
CREATE INDEX idx_player_pokemon_player ON player_pokemon (player_id);
