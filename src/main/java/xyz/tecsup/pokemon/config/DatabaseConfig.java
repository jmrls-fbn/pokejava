package xyz.tecsup.pokemon.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

// Pool de conexiones centralizado (HikariCP) hacia Postgres.
// Reemplaza a la antigua ConexionDB (conexión única vía DriverManager, sin pool,
// con usuario/contraseña hardcodeados). Los valores salen de variables de entorno;
// si no están exportadas, caen en los mismos defaults que trae docker-compose/.env
// para que la app funcione sin pasos manuales adicionales.
public class DatabaseConfig {

    private static final String DB_URL      = env("DB_URL", "jdbc:postgresql://localhost:5432/pokemon_db");
    private static final String DB_USER     = env("DB_USER", "pokemon_user");
    private static final String DB_PASSWORD = env("DB_PASSWORD", "pokemon_pass");

    private static final HikariDataSource dataSource = createDataSource();

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(10);
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
