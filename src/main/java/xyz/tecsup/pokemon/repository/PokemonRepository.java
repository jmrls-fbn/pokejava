package xyz.tecsup.pokemon.repository;

import xyz.tecsup.pokemon.config.DatabaseConfig;
import xyz.tecsup.pokemon.entity.Move;
import xyz.tecsup.pokemon.entity.Pokemon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Repository de Pokémon.
// Su única responsabilidad es traducir filas de SQLite en objetos Pokemon/Move.
public class PokemonRepository {

    private final MoveRepository moveRepository = new MoveRepository();

    // Construye un Pokemon completo (stats + movimientos) para entrar a batalla.
    // pokemonId: id de la especie en la tabla pokemon (1-151)
    // level: nivel con el que se quiere crear (afecta qué movimientos puede usar)
    public Pokemon getById(int pokemonId, int level) {
        String dataSql = "SELECT id, identifier FROM pokemon WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(dataSql)) {

            ps.setInt(1, pokemonId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String name = rs.getString("identifier");

                // Las 4 stats se guardan en filas separadas en pokemon_stat,
                // identificadas por stat_id (ver tabla `stats`: 1=hp, 2=attack, 3=defense, 6=speed)
                int hp      = getStat(con, pokemonId, 1);
                int attack  = getStat(con, pokemonId, 2);
                int defense = getStat(con, pokemonId, 3);
                int speed   = getStat(con, pokemonId, 6);

                // Solo trae los movimientos que el Pokémon ya puede usar a este nivel
                List<Move> moves = getMoves(con, pokemonId, level);

                return new Pokemon(pokemonId, name, hp, attack, defense,
                        speed, level, moves);
            }
        } catch (SQLException e) {
            System.out.println("Error consultando pokemon: " + e.getMessage());
        }
        return null; // no se encontró el pokemonId o falló la conexión
    }

    // Trae el valor de UNA stat específica desde pokemon_stat.
    // Se llama 4 veces desde getById
    private int getStat(Connection con, int pokemonId, int statId) throws SQLException {
        String sql = "SELECT base_stat FROM pokemon_stat WHERE pokemon_id = ? AND stat_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pokemonId);
            ps.setInt(2, statId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("base_stat");
            }
        }
        return 0; // si no existe el registro devuelve 0 en lugar de fallar
    }

    // Trae hasta 4 movimientos que el Pokémon puede usar a un nivel dado.
    // ORDER BY level DESC + LIMIT 4 prioriza los movimientos de nivel más alto
    // que aún sean <= al nivel actual (incluye también los de nivel 0).
    // Nota: como hay muchos movimientos con level=0 (MTs), normalmente esos
    // ganan el LIMIT 4 antes que movimientos aprendidos por nivel
    private List<Move> getMoves(Connection con, int pokemonId, int level) throws SQLException {
        List<Move> list = new ArrayList<>();
        String sql = "SELECT move_id FROM pokemon_moves WHERE pokemon_id = ? AND level <= ? ORDER BY level DESC LIMIT 4";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pokemonId);
            ps.setInt(2, level);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Move move = moveRepository.getById(rs.getInt("move_id"));
                if (move != null) {
                    list.add(move);
                }
            }
        }
        return list;
    }

    // Devuelve una lista simple (id, nombre) de los 151 Pokémon — usada por
    // MenuBar para mostrar la ventana "Lista de Pokémon" de la base de datos.
    public List<Object[]> getAll() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, identifier FROM pokemon ORDER BY id";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{ rs.getInt("id"), rs.getString("identifier") });
            }
        } catch (SQLException e) {
            System.out.println("Error consultando lista de pokemon: " + e.getMessage());
        }
        return list;
    }
}
