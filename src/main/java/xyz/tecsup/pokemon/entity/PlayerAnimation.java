package xyz.tecsup.pokemon.entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

// Se encarga únicamente de cargar, animar y dibujar el sprite del jugador.
// Separar esto de Player.java mantiene esa clase enfocada solo en movimiento/lógica.
public class PlayerAnimation {

    // Hoja de sprites recortada: 4 filas (direcciones) x 4 columnas (frames)
    // Orden de filas: 0=abajo, 1=izquierda, 2=derecha, 3=arriba
    private final BufferedImage[][] sprites = new BufferedImage[4][4];

    // Tamaño de cada sprite individual dentro de la hoja PNG original
    private final int SPRITE_WIDTH  = 64;
    private final int SPRITE_HEIGHT = 64;

    // Control del frame de animación actual
    private int currentFrame  = 0;
    private int frameCounter  = 0;
    private final int ANIMATION_SPEED = 10; // cuántos ticks de juego dura cada frame

    public PlayerAnimation() {
        loadSprites();
    }

    // Carga la hoja de sprites desde resources y la recorta en 16 imágenes individuales
    private void loadSprites() {
        try {
            BufferedImage sheet = ImageIO.read(
                    // TODO: "PlayerTest.png" es arte de prueba — reemplazar por la hoja de sprites definitiva.
                    Objects.requireNonNull(getClass().getResourceAsStream("/PlayerSprites/PlayerTest.png"))
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
            System.out.println("Sprites cargados correctamente.");
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

    // Dibuja el sprite actual del jugador, escalado y centrado sobre su tile.
    // direction: 0=abajo, 1=izquierda, 2=derecha, 3=arriba
    // screenX/Y: posición donde se dibuja en la pantalla (no en el mundo)
    public void draw(Graphics2D g2, int direction, int screenX, int screenY, int tileSize) {
        BufferedImage sprite = sprites[direction][currentFrame];
        if (sprite == null) return;

        // El sprite se dibuja más grande que el tile (1.5x) para que se vea mejor,
        // pero centrado para que sus "pies" sigan alineados con la hitbox real.
        int drawWidth  = (int)(tileSize * 1.5);
        int drawHeight = (int)(tileSize * 1.5);
        int offsetX    = (drawWidth  - tileSize) / 2;
        int offsetY    = (drawHeight - tileSize) / 2;

        g2.drawImage(sprite,
                screenX - offsetX,
                screenY - offsetY,
                drawWidth, drawHeight, null);

        // TODO: Hitbox visual temporal para depuración — pegada a los pies del sprite.
        // Quitar este bloque cuando ya no se necesite ver la hitbox.
        g2.setColor(new Color(255, 0, 0, 100));
        g2.fillRect(screenX + 12, screenY + tileSize - 16, tileSize - 24, 16);
        g2.setColor(Color.RED);
        g2.drawRect(screenX + 12, screenY + tileSize - 16, tileSize - 24, 16);
    }
}
