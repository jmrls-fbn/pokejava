package xyz.tecsup.pokemon.repository;

import xyz.tecsup.pokemon.config.DatabaseConfig;
import xyz.tecsup.pokemon.entity.Move;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Repository de Movimientos. Traduce filas de la tabla moves en objetos Move.
// Es usado internamente por PokemonRepository para armar la lista de movimientos
// de cada Pokémon.
public class MoveRepository {

    // Trae un movimiento por su id. Si no existe (o falla la conexión), devuelve null.
    public Move getById(int id) {
        String sql = "SELECT id, identifier, power, pp, type_id FROM moves WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Move(
                        rs.getInt("id"),
                        rs.getString("identifier"),
                        rs.getInt("power"),
                        rs.getInt("pp"),
                        rs.getInt("type_id")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error consultando movimiento: " + e.getMessage());
        }
        return null;
    }
}
