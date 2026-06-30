package xyz.tecsup.pokemon.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Acceso a la base de datos SQLite del juego.
// Reemplaza al pool HikariCP sobre Postgres: para un juego single-player que se
// distribuye como un JAR, una sola base local en archivo es más que suficiente
// y elimina la dependencia de Docker/Postgres. SQLite es de archivo y abrir una
// conexión es barato, así que cada repository sigue pidiendo getConnection()
// igual que antes (misma firma) y la cierra con try-with-resources.
public class DatabaseConfig {

    // El archivo vive en el directorio de trabajo (junto al JAR). Es visible y
    // trivial de resetear: para empezar de cero basta con borrar este archivo.
    private static final String DB_FILE = "pokemon.db";
    private static final String DB_URL  = "jdbc:sqlite:" + DB_FILE;

    // La base se construye primero en este archivo temporal y recién se renombra
    // a DB_FILE cuando quedó 100% lista (ver ensureInitialized).
    private static final String TEMP_DB_FILE = "pokemon.db.tmp";

    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        ensureInitialized();

        Connection con = DriverManager.getConnection(DB_URL);
        // SQLite no valida foreign keys por defecto; se activa por conexión para
        // que las relaciones del esquema se respeten (p. ej. al borrar partidas).
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    // En el primer arranque el archivo .db no existe: se crea el esquema y se
    // cargan los CSV empaquetados en el JAR.
    //
    // La construcción es ATÓMICA: se arma todo en un archivo temporal y solo se
    // renombra a DB_FILE cuando terminó con éxito. Así DB_FILE nunca existe a
    // medio construir. Esto importa porque la decisión de inicializar se basa en
    // si DB_FILE existe: si construyéramos directamente sobre él y el proceso se
    // interrumpiera (crash, kill) a mitad de la carga, el siguiente arranque
    // vería el archivo, lo daría por listo, y el juego abriría con una base
    // incompleta y consultas fallando en silencio. Con el renombrado, una
    // interrupción solo deja el .tmp (que se descarta), nunca un DB_FILE a medias.
    private static synchronized void ensureInitialized() throws SQLException {
        if (initialized) return;

        File dbFile = new File(DB_FILE);
        if (!dbFile.exists()) {
            File tempFile = new File(TEMP_DB_FILE);
            tempFile.delete(); // descartar restos de un intento previo interrumpido
            try {
                DatabaseInitializer.initialize("jdbc:sqlite:" + tempFile.getPath());
                promoteTempToFinal(tempFile, dbFile);
            } catch (SQLException | RuntimeException | IOException e) {
                tempFile.delete();
                throw new SQLException("No se pudo inicializar la base de datos: " + e.getMessage(), e);
            }
        }
        initialized = true;
    }

    // Renombra el archivo temporal ya construido al nombre definitivo. Se intenta
    // un move atómico (instantáneo, sin estado intermedio); si la plataforma no lo
    // soporta, se cae a un move normal, que para este caso (primer arranque, el
    // destino aún no existe) es suficientemente seguro.
    private static void promoteTempToFinal(File tempFile, File dbFile) throws IOException {
        try {
            Files.move(tempFile.toPath(), dbFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
