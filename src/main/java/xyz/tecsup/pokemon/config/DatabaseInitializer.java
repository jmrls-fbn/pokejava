package xyz.tecsup.pokemon.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Construye la base SQLite la PRIMERA vez que se ejecuta el juego: corre el
// esquema (db/schema.sql) y carga los datos desde los CSV, todo empaquetado
// como recursos dentro del JAR. Reemplaza a los antiguos scripts SQL de Docker
// (02_load.sql usaba COPY, que SQLite no soporta). Después del primer arranque
// el archivo .db persiste en disco y esta clase ya no se vuelve a tocar.
class DatabaseInitializer {

    // Un CSV a cargar: tabla destino, columnas en orden, recurso en el classpath
    // y si la primera línea es cabecera (los CSV del dataset la traen; los del
    // jugador semilla, no).
    private record TableLoad(String table, String columns, String resource, boolean hasHeader) {}

    // El orden importa solo por claridad; las foreign keys NO se validan durante
    // la carga (PRAGMA foreign_keys queda en su default OFF en esta conexión),
    // así que no hay riesgo de fallar por dependencias entre tablas.
    private static final List<TableLoad> LOADS = List.of(
            new TableLoad("pokemon",           "id, identifier, base_experience",                        "/db/data/pokemon.csv",           true),
            new TableLoad("stats",             "id, identifier",                                         "/db/data/stats.csv",             true),
            new TableLoad("types",             "id, identifier",                                         "/db/data/types.csv",             true),
            new TableLoad("type_efficacy",     "damage_type_id, target_type_id, damage_factor",          "/db/data/type_efficacy.csv",     true),
            new TableLoad("moves",             "id, identifier, power, pp, type_id",                     "/db/data/moves.csv",             true),
            new TableLoad("pokemon_stat",      "pokemon_id, stat_id, base_stat",                         "/db/data/pokemon_stat.csv",      true),
            new TableLoad("pokemon_moves",     "pokemon_id, move_id, level",                             "/db/data/pokemon_moves.csv",     true),
            new TableLoad("pokemon_types",     "pokemon_id, type_id, slot",                              "/db/data/pokemon_types.csv",     true),
            new TableLoad("pokemon_evolution", "id, pokemon_id, evolved_species_id, minimum_level",      "/db/data/pokemon_evolution.csv", true),
            new TableLoad("player",            "id, name, gender",                                       "/db/data/player.csv",            false),
            new TableLoad("player_pokemon",    "id, player_id, pokemon_id, level, current_hp, team_slot", "/db/data/player_pokemon.csv",   false)
    );

    // Crea el esquema y carga todos los CSV en una sola transacción. Si algo
    // falla, la transacción no se confirma; DatabaseConfig se encarga de borrar
    // el archivo .db parcial para que el siguiente arranque reintente limpio.
    static void initialize(String dbUrl) throws SQLException {
        System.out.println("Primera ejecución: creando la base de datos SQLite...");

        try (Connection con = DriverManager.getConnection(dbUrl)) {
            con.setAutoCommit(false);
            createSchema(con);
            for (TableLoad load : LOADS) {
                loadCsv(con, load);
            }
            con.commit();
        }

        System.out.println("Base de datos creada correctamente.");
    }

    // Ejecuta db/schema.sql sentencia por sentencia. Primero se quitan los
    // comentarios (--) del script COMPLETO y luego se parte por ";": así un ";"
    // que aparezca dentro de un comentario no rompe el split en sentencias.
    private static void createSchema(Connection con) throws SQLException {
        String script = stripSqlComments(readResource("/db/schema.sql"));
        try (Statement st = con.createStatement()) {
            for (String statement : script.split(";")) {
                if (!statement.isBlank()) {
                    st.execute(statement);
                }
            }
        }
    }

    // Elimina las líneas que son comentarios (--). En este esquema los comentarios
    // siempre ocupan su propia línea, así que basta con descartar las que empiezan por --.
    private static String stripSqlComments(String script) {
        StringBuilder sb = new StringBuilder();
        for (String line : script.split("\n")) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private static void loadCsv(Connection con, TableLoad load) throws SQLException {
        List<String[]> rows = readCsv(load.resource(), load.hasHeader());
        if (rows.isEmpty()) return;

        int columnCount = load.columns().split(",").length;
        String placeholders = "?" + ", ?".repeat(columnCount - 1);
        String sql = "INSERT INTO " + load.table() + " (" + load.columns() + ") VALUES (" + placeholders + ")";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] row : rows) {
                for (int i = 0; i < columnCount; i++) {
                    String value = i < row.length ? row[i].trim() : "";
                    // Campo vacío en el CSV => NULL (igual que hacía COPY en Postgres,
                    // p. ej. movimientos sin "power"). Las columnas con afinidad
                    // INTEGER convierten "45" -> 45 automáticamente en SQLite.
                    ps.setString(i + 1, value.isEmpty() ? null : value);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static List<String[]> readCsv(String resource, boolean hasHeader) throws SQLException {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(openResource(resource), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    line = stripBom(line); // algunos CSV traen BOM al inicio
                    firstLine = false;
                    if (hasHeader) continue;
                }
                if (line.isBlank()) continue;
                rows.add(line.split(",", -1)); // -1 conserva campos vacíos finales
            }
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el CSV " + resource, e);
        }
        return rows;
    }

    private static String readResource(String resource) throws SQLException {
        try (InputStream in = openResource(resource)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el recurso " + resource, e);
        }
    }

    private static InputStream openResource(String resource) throws SQLException {
        InputStream in = DatabaseInitializer.class.getResourceAsStream(resource);
        if (in == null) {
            throw new SQLException("Recurso no encontrado en el classpath: " + resource);
        }
        return in;
    }

    private static String stripBom(String s) {
        return (!s.isEmpty() && s.charAt(0) == '\uFEFF') ? s.substring(1) : s;
    }
}
