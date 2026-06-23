package xyz.tecsup.pokemon.entity;

public class GameSession {
    // Jugador activo durante toda la partida — fijo por ahora para pruebas
    // (id 1 = Ash en player.csv, ver db/init/04_seed.sql)
    // TODO: reemplazar por el id real una vez que StartScreen esté conectada a Main.
    public static int playerId = 1;
}
