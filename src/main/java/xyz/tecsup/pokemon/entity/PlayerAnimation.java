package xyz.tecsup.pokemon.entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

// Se encarga únicamente de cargar, animar y dibujar el sprite del jugador.
// Separar esto de Player.java mantiene esa clase enfocada solo en movimiento/lógica.
// El sprite del mapa depende del género elegido (chico/chica).
public class PlayerAnimation {

    // Hoja de sprites: 4 filas (direcciones) x 4 columnas (frames de caminado).
    // OJO: el orden de filas de las hojas GuySprites/GirlSprites es
    // 0=abajo, 1=arriba, 2=izquierda, 3=derecha, distinto del orden de
    // direcciones que usa el juego, por eso existe DIRECTION_TO_ROW abajo.
    private final BufferedImage[][] sprites = new BufferedImage[4][4];

    // Tamaño de cada sprite individual dentro de la hoja PNG original (16x24).
    // La hoja completa mide 64x96 = 4 columnas x 4 filas.
    private final int SPRITE_WIDTH  = 16;
    private final int SPRITE_HEIGHT = 24;

    // El juego maneja la dirección como 0=abajo, 1=izquierda, 2=derecha, 3=arriba
    // (ver Player.java). Este arreglo traduce esa dirección a la fila correcta
    // dentro de la hoja de sprites.
    private static final int[] DIRECTION_TO_ROW = { 0, 2, 3, 1 };

    // Control del frame de animación actual
    private int currentFrame  = 0;
    private int frameCounter  = 0;
    private final int ANIMATION_SPEED = 10; // cuántos ticks de juego dura cada frame

    public PlayerAnimation(String gender) {
        loadSprites(spriteSheetFor(gender));
    }

    // Elige la hoja de sprites según el género guardado del jugador.
    // Cualquier valor distinto de "chica" cae en el sprite de chico (incluye null).
    private String spriteSheetFor(String gender) {
        if ("chica".equalsIgnoreCase(gender)) {
            return "/PlayerSprites/GirlSprites.png";
        }
        return "/PlayerSprites/GuySprites.png";
    }

    // Carga la hoja de sprites desde resources y la recorta en 16 imágenes individuales
    private void loadSprites(String path) {
        try {
            BufferedImage sheet = ImageIO.read(
                    Objects.requireNonNull(getClass().getResourceAsStream(path))
            );
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    sprites[row][col] = sheet.getSubimage(
                            col * SPRITE_WIDTH,
                            row * SPRITE_HEIGHT,
                            SPRITE_WIDTH,
                            SPRITE_HEIGHT
                    );
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error cargando sprites: " + e.getMessage());
        }
    }

    // Avanza el frame de animación solo si el jugador se está moviendo.
    // Si está quieto, vuelve siempre al frame de reposo (frame 0).
    public void update(boolean moving) {
        if (moving) {
            frameCounter++;
            if (frameCounter >= ANIMATION_SPEED) {
                currentFrame  = (currentFrame + 1) % 4;
                frameCounter  = 0;
            }
        } else {
            currentFrame  = 0;
            frameCounter  = 0;
        }
    }

    // Dibuja el sprite actual del jugador, escalado y alineado sobre su tile.
    // direction: 0=abajo, 1=izquierda, 2=derecha, 3=arriba
    // screenX/Y: posición donde se dibuja en la pantalla (no en el mundo)
    public void draw(Graphics2D g2, int direction, int screenX, int screenY, int tileSize) {
        BufferedImage sprite = sprites[DIRECTION_TO_ROW[direction]][currentFrame];
        if (sprite == null) return;

        // El sprite (16x24) se dibuja conservando su proporción: ancho = un tile,
        // alto proporcional (1.5 tiles). Se ancla por los "pies" al borde inferior
        // del tile y se centra horizontalmente, para que la parte alta sobresalga
        // hacia arriba sin desalinear la hitbox real del jugador.
        int drawW = tileSize;
        int drawH = drawW * SPRITE_HEIGHT / SPRITE_WIDTH;
        int drawX = screenX + (tileSize - drawW) / 2;
        int drawY = screenY + tileSize - drawH;
        g2.drawImage(sprite, drawX, drawY, drawW, drawH, null);
    }
}
