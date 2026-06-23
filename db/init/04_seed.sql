-- Jugador de prueba: Ash con su equipo inicial, cargado vía COPY desde CSV
-- (igual que el dataset de Pokémon en 02_load.sql, pero estos dos archivos
-- son propios del proyecto, no del dataset externo).
-- GameSession.playerId está fijo en 1 (id del jugador en player.csv) mientras
-- la pantalla de creación de personaje (StartScreen) no está conectada a Main.

COPY player (id, name, gender)
FROM '/data/player.csv'
WITH (FORMAT csv, HEADER false);

COPY player_pokemon (id, player_id, pokemon_id, level, current_hp, team_slot)
FROM '/data/player_pokemon.csv'
WITH (FORMAT csv, HEADER false);

-- Los ids en los CSV son explícitos, así que hay que adelantar las secuencias
-- de las columnas IDENTITY para que el próximo jugador creado desde
-- StartScreen no choque con estos ids.
SELECT setval(pg_get_serial_sequence('player', 'id'), (SELECT MAX(id) FROM player));
SELECT setval(pg_get_serial_sequence('player_pokemon', 'id'), (SELECT MAX(id) FROM player_pokemon));
