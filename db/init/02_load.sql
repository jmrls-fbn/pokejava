-- Carga masiva vía COPY: miles de filas en milisegundos, sin pasos manuales.
-- Los CSV viven en ./db/data, montados como /data dentro del contenedor (solo lectura).

COPY pokemon(id, identifier, base_experience)
FROM '/data/pokemon.csv'
WITH (FORMAT csv, HEADER true);

COPY stats(id, identifier)
FROM '/data/stats.csv'
WITH (FORMAT csv, HEADER true);

COPY moves(id, identifier, power, pp)
FROM '/data/moves.csv'
WITH (FORMAT csv, HEADER true);

COPY pokemon_stat(pokemon_id, stat_id, base_stat)
FROM '/data/pokemon_stat.csv'
WITH (FORMAT csv, HEADER true);

COPY pokemon_moves(pokemon_id, move_id, level)
FROM '/data/pokemon_moves.csv'
WITH (FORMAT csv, HEADER true);

COPY pokemon_evolution(id, pokemon_id, evolved_species_id, minimum_level)
FROM '/data/pokemon_evolution.csv'
WITH (FORMAT csv, HEADER true);
