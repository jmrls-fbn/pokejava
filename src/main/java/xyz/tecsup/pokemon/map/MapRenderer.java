package xyz.tecsup.pokemon.map;

import java.awt.*;
import java.awt.image.BufferedImage;

// Renderiza las capas del mapa en pantalla
// Maneja escalado, volteo de tiles y culling (no dibujar lo que no se ve)
public class MapRenderer {

    private final MapReader mapReader;
    private final TilesetManager tilesetManager;

    // Constructor que obtiene referencias al lector y gestor de tilesets
    public MapRenderer(MapReader mapReader) {
        this.mapReader      = mapReader;
        this.tilesetManager = mapReader.getTilesetManager();
    }

    // Dibuja una capa completa del mapa, optimizando para solo renderizar tiles visibles
    // g2: contexto gráfico para dibujar
    // layer: matriz de IDs de tiles a renderizar
    // camX, camY: posición de la cámara (en píxeles)
    // screenWidth, screenHeight: dimensiones de la ventana
    // scaledTileSize: tamaño del tile escalado
    public void drawLayer(Graphics2D g2, int[][] layer,
                            int camX, int camY,
                            int screenWidth, int screenHeight,
                            int scaledTileSize) {
        if (layer == null) return;

        // Calcular rango de tiles visibles en pantalla (culling)
        int startCol = Math.max(0, camX / scaledTileSize);
        int startRow = Math.max(0, camY / scaledTileSize);
        int endCol   = Math.min(mapReader.getMapWidth(),  startCol + screenWidth  / scaledTileSize + 2);
        int endRow   = Math.min(mapReader.getMapHeight(), startRow + screenHeight / scaledTileSize + 2);

        // Iterar solo sobre tiles visibles
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                int gidRaw = layer[row][col];
                if (gidRaw == 0) continue; // Tile vacío, no dibujar

                // Extraer flags de volteo del ID del tile
                boolean flipH = (gidRaw & 0x80000000) != 0; // Volteo horizontal
                boolean flipV = (gidRaw & 0x40000000) != 0; // Volteo vertical

                // Obtener tileset e índices del tile
                int[] ts = tilesetManager.resolve(gidRaw);
                if (ts == null) continue;

                BufferedImage img = tilesetManager.getImage(ts[0]);
                int tileSize       = tilesetManager.getTileSize();

                // Convertir posición en mapa a posición en pantalla
                int destX = col * scaledTileSize - camX;
                int destY = row * scaledTileSize - camY;

                // Crear contexto gráfico temporal para transformaciones
                Graphics2D g2c = (Graphics2D) g2.create();

                // Aplicar volteos si el tile fue girado en el editor Tiled
                if (flipH || flipV) {
                    // Centro del tile en pantalla
                    int cx = destX + scaledTileSize / 2;
                    int cy = destY + scaledTileSize / 2;

                    // Trasladar al centro, escalar (voltear), y trasladar de vuelta
                    g2c.translate(cx, cy);
                    g2c.scale(flipH ? -1 : 1, flipV ? -1 : 1);
                    g2c.translate(-cx, -cy);
                }

                // Dibujar porción del tileset en pantalla escalada
                // drawImage(imagen, destX1, destY1, destX2, destY2,
                //           srcX1, srcY1, srcX2, srcY2, observer)
                g2c.drawImage(img,
                        destX, destY,
                        destX + scaledTileSize,
                        destY + scaledTileSize,
                        ts[1], ts[2],
                        ts[1] + tileSize,
                        ts[2] + tileSize,
                        null);

                // Liberar recursos del contexto temporal
                g2c.dispose();
            }
        }
    }

    // Dibuja la capa de hierba alta solo sobre el jugador
    // Crea el efecto de que el jugador se oculta en la hierba
    // playerX, playerY: posición del jugador en el mapa
    // camX, camY: posición de la cámara
    // scaledTileSize: tamaño escalado del tile
    public void drawGrassTile(Graphics2D g2, int playerX, int playerY,
                                  int camX, int camY, int scaledTileSize) {
        int[][] grassLayer = mapReader.getGrassLayer();
        if (grassLayer == null) return;

        // Determinar en qué tile está el jugador
        int col = (playerX + scaledTileSize / 2) / scaledTileSize;
        int row = (playerY + scaledTileSize - 8) / scaledTileSize;

        // Validar que el tile esté dentro del mapa
        if (row < 0 || col < 0 || row >= mapReader.getMapHeight() || col >= mapReader.getMapWidth()) return;

        // Limites del tile actual en píxeles
        int tileLeft   = col * scaledTileSize;
        int tileRight  = tileLeft + scaledTileSize;
        int tileTop    = row * scaledTileSize;
        int tileBottom = tileTop + scaledTileSize;

        // Limites del jugador (hitbox)
        int playerLeft   = playerX + 8;
        int playerRight  = playerX + scaledTileSize - 8;
        int playerTop    = playerY + scaledTileSize / 2;
        int playerBottom = playerY + scaledTileSize - 8;

        // Solo dibujar hierba si el jugador está completamente dentro del tile
        if (playerLeft < tileLeft || playerRight > tileRight || playerTop < tileTop || playerBottom > tileBottom) return;

        int gid = grassLayer[row][col];
        if (gid == 0) return;

        // Obtener tileset e índices
        int[] ts = tilesetManager.resolve(gid);
        if (ts == null) return;

        BufferedImage img = tilesetManager.getImage(ts[0]);
        int tileSize       = tilesetManager.getTileSize();
        int half           = (int)(tileSize * 0.50); // Solo dibujar mitad inferior

        // Posición en pantalla
        int destX = col * scaledTileSize - camX;
        int destY = row * scaledTileSize - camY;

        // Dibujar solo la mitad inferior del tile de hierba
        g2.drawImage(img,
                destX, destY + scaledTileSize / 2,
                destX + scaledTileSize, destY + scaledTileSize,
                ts[1], ts[2] + half,
                ts[1] + tileSize, ts[2] + tileSize,
                null);
    }
}
