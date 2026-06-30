package xyz.tecsup.pokemon.repository;

import xyz.tecsup.pokemon.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Repository de Player. Maneja todo lo relacionado al jugador y a su equipo de Pokémon
// (tablas `player` y `player_pokemon`).
public class PlayerRepository {

    // Crea un jugador nuevo junto con su Pokémon inicial.
    // Usado por StartScreen.java cuando el jugador eligió nombre/género/inicial
    // por primera vez.
    // Devuelve el id autogenerado del nuevo jugador, o -1 si falló.
    public int createPlayer(String name, String gender, int initialPokemonId) {
        String playerSql = "INSERT INTO player (name, gender) VALUES (?, ?)";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(playerSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, gender);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int playerId = rs.getInt(1);
                createInitialPokemon(con, playerId, initialPokemonId);
                return playerId;
            }
        } catch (SQLException e) {
            System.out.println("Error creando jugador: " + e.getMessage());
        }
        return -1;
    }

    // Borra un jugador y todo su equipo de Pokémon (player_pokemon primero,
    // por la foreign key, luego el jugador). Usado por StartScreen cuando
    // el usuario presiona "Borrar Partida" en una partida del desplegable.
    // Devuelve true si se borró correctamente.
    public boolean deletePlayer(int playerId) {
        String deleteTeamSql = "DELETE FROM player_pokemon WHERE player_id = ?";
        String deletePlayerSql = "DELETE FROM player WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(deleteTeamSql)) {
                ps.setInt(1, playerId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(deletePlayerSql)) {
                ps.setInt(1, playerId);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error borrando jugador: " + e.getMessage());
            return false;
        }
    }

    // Devuelve todas las partidas guardadas (id, name) para llenar el desplegable
    // de "Cargar Partida" en StartScreen. Ordenadas por id para que las más
    // antiguas aparezcan primero.
    public List<Object[]> getAllPlayers() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, name FROM player ORDER BY id";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{ rs.getInt("id"), rs.getString("name") });
            }
        } catch (SQLException e) {
            System.out.println("Error consultando partidas guardadas: " + e.getMessage());
        }
        return list;
    }

    // Devuelve el género del jugador ("chico"/"chica"), usado por GamePanel para
    // elegir el sprite del personaje en el mapa. Si no se encuentra, devuelve "chico".
    public String getGender(int playerId) {
        String sql = "SELECT gender FROM player WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("gender");
            }
        } catch (SQLException e) {
            System.out.println("Error consultando género del jugador: " + e.getMessage());
        }
        return "chico";
    }

    // Devuelve los Pokémon del equipo activo de un jugador (team_slot IS NOT NULL),
    // ordenados por su posición en el equipo (1, 2, 3...).
    // Cada fila: [id de player_pokemon, nombre de especie, nivel, hp_actual, pokemon_id]
    // Usado por MenuBar (ventana "Mi Equipo") y por GamePanel al iniciar una batalla.
    public List<Object[]> getTeam(int playerId) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT pp.id, p.identifier, pp.level, pp.current_hp, pp.pokemon_id " +
                "FROM player_pokemon pp " +
                "JOIN pokemon p ON pp.pokemon_id = p.id " +
                "WHERE pp.player_id = ? AND pp.team_slot IS NOT NULL " +
                "ORDER BY pp.team_slot";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("identifier"),
                        rs.getInt("level"),
                        rs.getInt("current_hp"),
                        rs.getInt("pokemon_id")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error consultando equipo: " + e.getMessage());
        }
        return list;
    }

    // Inserta el primer Pokémon del jugador recién creado, siempre en la posición 1 del equipo.
    // HACK: current_hp=999 es un valor sin uso real, el HP de verdad lo calcula
    // PokemonRepository desde las stats base cada vez que el Pokémon se carga para batalla.
    private void createInitialPokemon(Connection con, int playerId, int pokemonId) throws SQLException {
        String sql = "INSERT INTO player_pokemon (player_id, pokemon_id, level, current_hp, team_slot) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, pokemonId);
            ps.setInt(3, 32);
            ps.setInt(4, 999);
            ps.setInt(5, 1);
            ps.executeUpdate();
        }
    }

    // Promedio de nivel del equipo activo. Usado por EncounterGenerator para que
    // el Pokémon salvaje tenga un nivel acorde al equipo del jugador (±5).
    public int getAverageTeamLevel(int playerId) {
        String sql = "SELECT AVG(level) as average FROM player_pokemon WHERE player_id = ? AND team_slot IS NOT NULL";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return (int) rs.getDouble("average");
            }
        } catch (SQLException e) {
            System.out.println("Error calculando nivel promedio: " + e.getMessage());
        }
        return 5;
    }
}