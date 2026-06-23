package xyz.tecsup.pokemon.map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Gestor centralizado de tilesets
// Carga imágenes de tilesets y resuelve las coordenadas de cada tile dentro de ellas
public class TilesetManager {

    // Lista de imágenes de tilesets cargadas en memoria
    private final List<BufferedImage> images     = new ArrayList<>();

    // ID inicial (firstgid) de cada tileset para mapeo de IDs globales a locales
    private final List<Integer>       firstGids  = new ArrayList<>();

    // Número de columnas (tiles horizontales) en cada tileset
    private final List<Integer>       columns    = new ArrayList<>();

    // Tamaño en píxeles de cada tile (generalmente 16, 32, 64, etc)
    private int tileSize;

    // Carga todas las imágenes de tilesets desde las rutas proporcionadas
    // paths: lista de rutas a archivos de tileset
    // gids: lista de IDs iniciales (firstgid) para cada tileset
    // tileSize: tamaño en píxeles de cada tile individual
    public void load(List<String> paths, List<Integer> gids, int tileSize) {
        this.tileSize = tileSize;

        // Procesar cada tileset
        for (int i = 0; i < paths.size(); i++) {
            try {
                // Cargar imagen desde recursos de la aplicación
                BufferedImage image = ImageIO.read(
                        Objects.requireNonNull(getClass().getResourceAsStream(paths.get(i)))
                );

                // Almacenar imagen cargada
                images.add(image);

                // Almacenar ID inicial de este tileset
                firstGids.add(gids.get(i));

                // Calcular número de columnas (ancho total / tamaño de tile)
                columns.add(image.getWidth() / tileSize);
            } catch (Exception e) {
                System.out.println("Error cargando tileset: " + paths.get(i));
            }
        }
    }

    // Encuentra el tileset y las coordenadas del tile dentro de la imagen
    // gid: ID global del tile (incluye flags de volteo)
    // Retorna: {índice_tileset, coordX, coordY}
    public int[] resolve(int gid) {
        // Eliminar bits de volteo, dejar solo el ID del tile
        gid = gid & 0x1FFFFFFF;

        // Buscar cuál tileset contiene este tile
        for (int i = firstGids.size() - 1; i >= 0; i--) {
            if (gid >= firstGids.get(i)) {
                // Convertir ID global a ID local dentro del tileset
                int local = gid - firstGids.get(i);
                int cols  = columns.get(i);

                // Calcular posición X,Y del tile en la imagen
                int x = (local % cols) * tileSize;
                int y = (local / cols) * tileSize;

                return new int[]{i, x, y};
            }
        }
        return null;
    }

    // Retorna la imagen del tileset por su índice
    public BufferedImage getImage(int index) {
        return images.get(index);
    }

    // Retorna el tamaño en píxeles de cada tile
    public int getTileSize() { return tileSize; }
}
