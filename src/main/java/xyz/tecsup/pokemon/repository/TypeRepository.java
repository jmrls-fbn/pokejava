package xyz.tecsup.pokemon.repository;

import xyz.tecsup.pokemon.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// Repository de la tabla de efectividad de tipos (type_efficacy).
public class TypeRepository {

    // Devuelve el factor de daño (en %) indexado por [tipo atacante][tipo defensor].
    // Solo contiene los pares != 100 que están en la BD; los pares ausentes son
    // neutrales (1x) y los resuelve TypeChart por defecto.
    public Map<Integer, Map<Integer, Integer>> getEfficacyChart() {
        Map<Integer, Map<Integer, Integer>> chart = new HashMap<>();
        String sql = "SELECT damage_type_id, target_type_id, damage_factor FROM type_efficacy";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int attackingType = rs.getInt("damage_type_id");
                int defendingType = rs.getInt("target_type_id");
                int factor        = rs.getInt("damage_factor");
                chart.computeIfAbsent(attackingType, k -> new HashMap<>())
                        .put(defendingType, factor);
            }
        } catch (SQLException e) {
            System.out.println("Error cargando tabla de tipos: " + e.getMessage());
        }
        return chart;
    }
}
