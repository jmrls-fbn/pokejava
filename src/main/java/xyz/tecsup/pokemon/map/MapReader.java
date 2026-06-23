package xyz.tecsup.pokemon.map;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Clase encargada de leer y parsear archivos TMJ (Tiled Map JSON)
// Extrae capas de colisión, vegetación y visuales para renderizar el mapa
public class MapReader {

    // Capas especiales que controlan mecánicas del juego
    private int[][] collisionLayer;    // Tiles donde el jugador no puede pasar
    private int[][] grassLayer;        // Hierba alta que oculta al jugador
    private int[][] roofLayer;         // Árboles y objetos que se renderizan encima
    private int[][] roofLayer2;        // Segunda capa de objetos elevados

    // Capas visuales generales (fondos, decoración, etc)
    private final List<int[][]> visualLayers = new ArrayList<>();

    // Propiedades del mapa
    private int mapWidth, mapHeight;   // Dimensiones en tiles
    private int tileSize;               // Tamaño de cada tile en píxeles

    // Gestor que maneja la carga y acceso a texturas de tilesets
    private final TilesetManager tilesetManager = new TilesetManager();

    // Constructor que inicia la carga del mapa
    public MapReader(String tmjFile, List<String> tilesetPaths) {
        loadMap(tmjFile, tilesetPaths);
    }

    // Lee el archivo TMJ, parsea su contenido JSON y extrae todas las capas
    private void loadMap(String path, List<String> tilesetPaths) {
        try {
            // Cargar archivo desde recursos de la aplicación
            InputStream is = getClass().getResourceAsStream(path);
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(content);

            // Obtener dimensiones y tamaño de tiles del mapa
            mapWidth  = json.getInt("width");
            mapHeight = json.getInt("height");
            tileSize  = json.getInt("tilewidth");

            // Extraer IDs iniciales (firstgid) de cada tileset para mapeo correcto
            JSONArray tilesetsJson = json.getJSONArray("tilesets");
            List<Integer> gids = new ArrayList<>();
            for (int i = 0; i < tilesetsJson.length(); i++) {
                gids.add(tilesetsJson.getJSONObject(i).getInt("firstgid"));
            }

            // Cargar texturas de tilesets con sus respectivos IDs
            tilesetManager.load(tilesetPaths, gids, tileSize);

            // Procesar todas las capas del mapa
            JSONArray layers = json.getJSONArray("layers");
            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.getJSONObject(i);
                // Solo procesar capas de tiles (ignorar objeto layers)
                if (!layer.getString("type").equals("tilelayer")) continue;

                // Obtener nombre de la capa y parsear sus datos
                String name = layer.getString("name");
                int[][] data = parseLayer(layer);

                // Clasificar la capa según su nombre para asignarla a su variable
                switch (name) {
                    case "Collisions" -> collisionLayer = data;
                    case "TallGrass"  -> grassLayer      = data;
                    case "Trees"      -> roofLayer        = data;
                    case "Trees2"     -> roofLayer2       = data;
                    // Las capas no reconocidas se guardan como capas visuales
                    default           -> visualLayers.add(data);
                }
            }
            System.out.println("Mapa cargado: " + mapWidth + "x" + mapHeight);
        } catch (Exception e) {
            System.out.println("Error cargando mapa: " + e.getMessage());
        }
    }

    // Convierte el array plano de datos JSON en una matriz 2D [fila][columna]
    private int[][] parseLayer(JSONObject layer) {
        JSONArray data = layer.getJSONArray("data");
        // Crear matriz con dimensiones del mapa
        int[][] matrix = new int[mapHeight][mapWidth];
        // Iterar sobre cada posición y asignar el ID del tile
        for (int row = 0; row < mapHeight; row++)
            for (int col = 0; col < mapWidth; col++)
                // Convertir índice 2D a índice lineal (fila * ancho + columna)
                matrix[row][col] = data.getInt(row * mapWidth + col);
        return matrix;
    }

    // GETTERS
    public TilesetManager getTilesetManager() { return tilesetManager; }
    public List<int[][]> getVisualLayers()    { return visualLayers; }
    public int[][] getCollisionLayer()        { return collisionLayer; }
    public int[][] getGrassLayer()            { return grassLayer; }
    public int[][] getRoofLayer()             { return roofLayer; }
    public int[][] getRoofLayer2()            { return roofLayer2; }
    public int getMapWidth()                  { return mapWidth; }
    public int getMapHeight()                 { return mapHeight; }
}
