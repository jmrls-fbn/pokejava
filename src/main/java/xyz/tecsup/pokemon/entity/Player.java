package xyz.tecsup.pokemon.entity;

import xyz.tecsup.pokemon.control.KeyHandler;
import xyz.tecsup.pokemon.map.CollisionChecker;
import xyz.tecsup.pokemon.sounds.AudioManager;
import java.awt.*;

// Representa al personaje controlado por el usuario.
// Maneja posición en el mundo, movimiento, colisiones y detección de cambio de tile.
// Todo lo visual (sprites/animación) está en PlayerAnimation.
public class Player {

    // Posición del jugador en el mundo, en píxeles (no en pantalla — eso lo calcula GamePanel con la cámara)
    public int worldX, worldY;
    public int speed = 4; // píxeles que se mueve por frame

    // 0=abajo, 1=izquierda, 2=derecha, 3=arriba — usado tanto para mover como para elegir el sprite
    private int direction = 0;

    private final KeyHandler keyHandler;
    private final int TILE_SIZE;
    private final PlayerAnimation animation;

    // Gestor de colisiones inyectado desde GamePanel (puede ser null si aún no se asignó)
    private CollisionChecker collisionChecker;

    // Guarda en qué tile estaba el jugador el frame anterior, para saber
    // cuándo "entra" a un tile nuevo (útil para no recalcular encuentros en cada pixel)
    private int lastTileX = -1;
    private int lastTileY = -1;
    private boolean newTile = false;

    public Player(KeyHandler keyHandler, int tileSize, String gender) {
        this.keyHandler = keyHandler;
        this.TILE_SIZE  = tileSize;
        this.animation  = new PlayerAnimation(gender);

        // Posición inicial fija del jugador al cargar el mapa
        worldX = TILE_SIZE * 5;
        worldY = TILE_SIZE * 8;
    }

    // Se llama una vez por frame desde GamePanel.update()
    public void update() {
        boolean moving = false;
        int newX = worldX;
        int newY = worldY;

        // Leer input del teclado y calcular hacia dónde se intenta mover
        if (keyHandler.down)  { direction = 0; newY += speed; moving = true; }
        if (keyHandler.up)    { direction = 3; newY -= speed; moving = true; }
        if (keyHandler.left)  { direction = 1; newX -= speed; moving = true; }
        if (keyHandler.right) { direction = 2; newX += speed; moving = true; }

        if (moving) {
            boolean blocked = collisionChecker != null && collisionChecker.hasCollision(newX, newY, TILE_SIZE);

            if (!blocked) {
                worldX = newX;
                worldY = newY;
            } else {
                // El jugador intentó moverse pero hay un obstáculo: sonido de choque.
                // Se reproduce aquí (no en cada frame repetido) gracias al chequeo de newTile más abajo.
                playCollisionSoundOnce();
            }
        }

        detectTileChange();
        animation.update(moving);
    }

    // Evita que el sonido de colisión se repita en cada frame mientras el jugador
    // mantiene presionada la tecla contra el muro — solo suena una vez por intento.
    private boolean wasBlockedLastFrame = false;
    private void playCollisionSoundOnce() {
        if (!wasBlockedLastFrame) {
            AudioManager.playSoundEffect("/Sounds/Collision.wav");
        }
        wasBlockedLastFrame = true;
    }

    // Compara la posición actual contra la del frame anterior para saber
    // si el jugador cruzó a un tile distinto. GamePanel usa esto para
    // decidir cuándo intentar un encuentro Pokémon en hierba alta.
    private void detectTileChange() {
        int currentTileX = worldX / TILE_SIZE;
        int currentTileY = worldY / TILE_SIZE;

        if (currentTileX != lastTileX || currentTileY != lastTileY) {
            lastTileX = currentTileX;
            lastTileY = currentTileY;
            newTile = true;
            wasBlockedLastFrame = false; // se movió con éxito, resetea el flag de colisión

            // Sonido de paso al entrar a hierba alta. Se reproduce aquí en Player
            // en lugar de GamePanel porque es lo más cercano a "el jugador pisó algo".
            if (collisionChecker != null && collisionChecker.isInTallGrass(worldX, worldY, TILE_SIZE)) {
                AudioManager.playSoundEffect("/Sounds/GrassStep.wav");
            }
        } else {
            newTile = false;
        }
    }

    // Delega el dibujado a PlayerAnimation, pasándole la dirección actual
    public void draw(Graphics2D g2, int screenX, int screenY) {
        animation.draw(g2, direction, screenX, screenY, TILE_SIZE);
    }

    // Inyectado desde GamePanel una vez que el mapa ya está cargado
    public void setCollisionChecker(CollisionChecker collisionChecker) {
        this.collisionChecker = collisionChecker;
    }

    // true solo durante el frame exacto en que el jugador cruzó a un nuevo tile
    public boolean hasEnteredNewTile() {
        return newTile;
    }
}