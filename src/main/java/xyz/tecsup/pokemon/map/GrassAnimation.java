package xyz.tecsup.pokemon.map;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

// Animación corta (4 frames) que se reproduce una vez sobre el tile donde
// el jugador acaba de entrar a hierba alta — simula el movimiento de las
// hojas al ser pisadas. Se crea una nueva instancia cada vez que el jugador
// entra a un tile de hierba distinto, y se descarta cuando termina.
public class GrassAnimation {

    private static BufferedImage[] frames;
    private static final int FRAME_SIZE = 16;
    private static final int TOTAL_FRAMES = 4;

    private final int tileX, tileY; // posición en el MUNDO (no en pantalla) donde se reproduce

    private int currentFrame = 0;
    private int frameCounter = 0;
    private static final int TICKS_PER_FRAME = 6; // velocidad de la animación

    private boolean finished = false;

    public GrassAnimation(int worldTileX, int worldTileY) {
        this.tileX = worldTileX;
        this.tileY = worldTileY;
        loadFramesIfNeeded();
    }

    // Carga el spritesheet una sola vez para todas las instancias (los frames
    // son estáticos porque la imagen nunca cambia entre animaciones)
    private static void loadFramesIfNeeded() {
        if (frames != null) return;

        frames = new BufferedImage[TOTAL_FRAMES];
        try {
            BufferedImage sheet = ImageIO.read(
                    Objects.requireNonNull(GrassAnimation.class.getResourceAsStream("/RouteSprites/GrassAnimation.png"))
            );
            for (int i = 0; i < TOTAL_FRAMES; i++) {
                frames[i] = sheet.getSubimage(i * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("No se encontró el spritesheet de animación de hierba.");
        }
    }

    // Avanza el frame actual. Se llama una vez por frame desde GamePanel mientras
    // esta animación esté activa.
    public void update() {
        if (finished) return;

        frameCounter++;
        if (frameCounter >= TICKS_PER_FRAME) {
            frameCounter = 0;
            currentFrame++;
            if (currentFrame >= TOTAL_FRAMES) {
                finished = true;
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

    // Dibuja el frame actual en la posición de pantalla correspondiente al tile.
    // camX/camY son los mismos que usa MapRenderer para todo lo demás.
    public void draw(Graphics2D g2, int camX, int camY, int tileSizeScaled) {
        if (finished || frames == null || frames[currentFrame] == null) return;

        int screenX = tileX * tileSizeScaled - camX;
        int screenY = tileY * tileSizeScaled - camY;

        g2.drawImage(frames[currentFrame], screenX, screenY, tileSizeScaled, tileSizeScaled, null);
    }
}