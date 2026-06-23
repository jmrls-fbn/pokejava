package xyz.tecsup.pokemon.map;

// Detecta colisiones del jugador con obstáculos en el mapa
// Usa la capa de colisiones para verificar si una posición es transitable
public class CollisionChecker {

    private final MapReader map;

    // Constructor que obtiene referencia al mapa
    public CollisionChecker(MapReader map) {
        this.map = map;
    }

    // Verifica si hay colisión en una posición determinada
    // Comprueba 4 puntos de la hitbox del jugador (esquinas inferiores)
    // worldX, worldY: posición superior izquierda del jugador en el mapa
    // tileSize: tamaño del tile en píxeles
    // Retorna: true si hay colisión, false si es zona transitable
    public boolean hasCollision(int worldX, int worldY, int tileSize) {
        int[][] collisionLayer = map.getCollisionLayer();
        if (collisionLayer == null) return false;

        // Definir hitbox del jugador (más pequeña que el sprite)
        // 12 píxeles de margen a los lados, 16 píxeles de margen arriba
        int left   = worldX + 12;             // Borde izquierdo
        int right  = worldX + tileSize - 12;  // Borde derecho
        int top    = worldY + tileSize - 16;  // Borde superior (casi abajo)
        int bottom = worldY + tileSize;       // Borde inferior

        // Verificar colisión en las 4 esquinas inferiores de la hitbox
        return isTileOccupied(collisionLayer, left, top, tileSize)
                || isTileOccupied(collisionLayer, right, top, tileSize)
                || isTileOccupied(collisionLayer, left, bottom, tileSize)
                || isTileOccupied(collisionLayer, right, bottom, tileSize);
    }

    // Comprueba si un tile específico tiene colisión
    // layer: matriz de IDs de tiles de colisión
    // px, py: posición en píxeles en el mundo
    // tileSize: tamaño del tile
    // Retorna: true si el tile tiene colisión (ID != 0)
    private boolean isTileOccupied(int[][] layer, int px, int py, int tileSize) {
        // Convertir píxeles a índices de tile
        int col = px / tileSize;
        int row = py / tileSize;

        // Validar que el tile esté dentro de los límites del mapa
        if (row < 0 || col < 0 || row >= map.getMapHeight() || col >= map.getMapWidth())
            return false; // Fuera del mapa = no hay colisión

        // Retornar true si el tile tiene ID diferente de 0 (hay obstáculo)
        return layer[row][col] != 0;
    }

    // Verifica si el jugador está parado sobre un tile de hierba alta
    // Usa el centro de la hitbox del jugador (igual que en colisiones)
    public boolean isInTallGrass(int worldX, int worldY, int tileSize) {
        int[][] grassLayer = map.getGrassLayer();
        if (grassLayer == null) return false;

        // Usar los pies del jugador, igual que la hitbox de colisión
        int centerX = worldX + tileSize / 2;
        int centerY = worldY + tileSize - 8;

        int col = centerX / tileSize;
        int row = centerY / tileSize;

        if (row < 0 || col < 0 || row >= map.getMapHeight() || col >= map.getMapWidth())
            return false;

        return grassLayer[row][col] != 0;
    }

}
