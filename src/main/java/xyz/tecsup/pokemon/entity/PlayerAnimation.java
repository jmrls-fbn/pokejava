package xyz.tecsup.pokemon.entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

// Se encarga únicamente de cargar y dibujar el sprite del jugador.
// Separar esto de Player.java mantiene esa clase enfocada solo en movimiento/lógica.
//
// Hay dos modos de sprite:
//   - ESTÁTICO (chico/chica): un único retrato sin animación ni direcciones.
//     Es lo que se usa según el género elegido por el jugador.
//   - HOJA DE ANIMACIÓN (fallback): PlayerTest.png, 4 direcciones x 4 frames de
//     caminado. Se usa si no hay género válido / no carga el retrato.
public class PlayerAnimation {

    // --- Modo hoja de animación ---
    // 4 filas (direcciones: 0=abajo, 1=izquierda, 2=derecha, 3=arriba) x 4 frames
    private final BufferedImage[][] sprites = new BufferedImage[4][4];
    private final int SPRITE_WIDTH  = 64;
    private final int SPRITE_HEIGHT = 64;

    private int currentFrame  = 0;
    private int frameCounter  = 0;
    private final int ANIMATION_SPEED = 10; // cuántos ticks de juego dura cada frame

    // --- Modo sprite estático (por género) ---
    // Si no es null, se dibuja siempre esta imagen y se ignoran dirección y frame.
    private BufferedImage staticSprite;

    // gender: "chico" / "chica" => retrato estático correspondiente.
    // Cualquier otro valor (o null) cae al fallback de la hoja de animación.
    public PlayerAnimation(String gender) {
        if ("chico".equals(gender)) {
            staticSprite = loadImage("/PlayerSprites/Chico.png");
        } else if ("chica".equals(gender)) {
            staticSprite = loadImage("/PlayerSprites/Chica.png");
        }

        if (staticSprite == null) {
            loadSheet();
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error cargando sprite del jugador: " + path);
            return null;
        }
    }

    // Carga la hoja de sprites de prueba y la recorta en 16 imágenes individuales.
    private void loadSheet() {
        BufferedImage sheet = loadImage("/PlayerSprites/PlayerTest.png");
        if (sheet == null) return;

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
    }

    // Avanza el frame de animación solo si el jugador se está moviendo.
    // En modo estático no hay animación, así que no hace nada.
    public void update(boolean moving) {
        if (staticSprite != null) return;

        if (moving) {
            frameCounter++;
            if (frameCounter >= ANIMATION_SPEED) {
                currentFrame = (currentFrame + 1) % 4;
                frameCounter = 0;
            }
        } else {
            currentFrame = 0;
            frameCounter = 0;
        }
    }

    // Dibuja el sprite actual del jugador, escalado y centrado sobre su tile.
    // direction: 0=abajo, 1=izquierda, 2=derecha, 3=arriba (se ignora en modo estático).
    // screenX/Y: posición donde se dibuja en la pantalla (no en el mundo).
    public void draw(Graphics2D g2, int direction, int screenX, int screenY, int tileSize) {
        if (staticSprite != null) {
            drawStatic(g2, screenX, screenY, tileSize);
            return;
        }

        BufferedImage sprite = sprites[direction][currentFrame];
        if (sprite == null) return;

        // El sprite se dibuja más grande que el tile (1.5x) para que se vea mejor,
        // pero centrado para que sus "pies" sigan alineados con la hitbox real.
        int drawSize = (int) (tileSize * 1.5);
        int offset   = (drawSize - tileSize) / 2;
        g2.drawImage(sprite, screenX - offset, screenY - offset, drawSize, drawSize, null);
    }

    // Dibuja el retrato estático conservando su proporción 64x96 (alto = 1.5 ancho),
    // anclado por los pies al centro inferior del tile para que pise bien el mapa.
    private void drawStatic(Graphics2D g2, int screenX, int screenY, int tileSize) {
        int drawWidth  = tileSize;
        int drawHeight = (int) (tileSize * (96.0 / 64.0));
        int drawX = screenX;                                   // mismo ancho que el tile
        int drawY = screenY + tileSize - drawHeight;           // pies alineados al fondo del tile
        g2.drawImage(staticSprite, drawX, drawY, drawWidth, drawHeight, null);
    }
}
