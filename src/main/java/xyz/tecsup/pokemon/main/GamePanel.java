package xyz.tecsup.pokemon.main;

import xyz.tecsup.pokemon.sounds.AudioManager;
import xyz.tecsup.pokemon.battle.BattleTrigger;
import xyz.tecsup.pokemon.control.KeyHandler;
import xyz.tecsup.pokemon.entity.GameSession;
import xyz.tecsup.pokemon.entity.Player;
import xyz.tecsup.pokemon.map.CollisionChecker;
import xyz.tecsup.pokemon.repository.PlayerRepository;
import xyz.tecsup.pokemon.map.GrassAnimation;
import xyz.tecsup.pokemon.map.MapReader;
import xyz.tecsup.pokemon.map.MapRenderer;

import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Panel del mapa. Coordina el game loop (60 FPS), el dibujado por capas,
// la cámara que sigue al jugador con límites, y delega a BattleTrigger
// la decisión de cuándo iniciar un combate.
public class GamePanel extends JPanel implements Runnable {

    private final int TILE_ORIGINAL_SIZE = 16;
    private final int SCALE              = 3;
    public  final int TILE_SIZE          = TILE_ORIGINAL_SIZE * SCALE;

    private final int MAX_SCREEN_COL  = 16;
    private final int MAX_SCREEN_ROW  = 12;
    public  final int SCREEN_WIDTH    = TILE_SIZE * MAX_SCREEN_COL;
    public  final int SCREEN_HEIGHT   = TILE_SIZE * MAX_SCREEN_ROW;

    public final int PLAYER_SCREEN_X = SCREEN_WIDTH  / 2 - TILE_SIZE / 2;
    public final int PLAYER_SCREEN_Y = SCREEN_HEIGHT / 2 - TILE_SIZE / 2;

    private Thread gameThread;

    private final KeyHandler keyHandler = new KeyHandler();
    private Player player;

    private final MapReader mapReader;
    private final MapRenderer mapRenderer;
    private final CollisionChecker collisionChecker;

    private final BattleTrigger battleTrigger = new BattleTrigger();

    // Animaciones de hierba activas en este momento (normalmente 0 o 1,
    // pero se permite una lista por si en el futuro hay varias a la vez)
    private final List<GrassAnimation> grassAnimations = new ArrayList<>();

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(34, 139, 34));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        this.requestFocusInWindow();

        String gender = new PlayerRepository().getGender(GameSession.playerId);
        player = new Player(keyHandler, TILE_SIZE, gender);

        mapReader = new MapReader(
                "/RouteSprites/Route.tmj",
                List.of("/RouteSprites/Mountain.png",
                        "/RouteSprites/Water.png",
                        "/RouteSprites/ForestHouse.png"));
        mapRenderer = new MapRenderer(mapReader);
        collisionChecker = new CollisionChecker(mapReader);
        player.setCollisionChecker(collisionChecker);

        AudioManager.playMusic("/PokemonOST/Route.wav", -12f);

        startCounter();
    }

    public synchronized void startCounter() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Detiene el hilo del game loop y la música de fondo. Se llama antes de
    // cerrar la ventana del mapa (botón "Reiniciar" o al volver a StartScreen),
    // para que no sigan corriendo en segundo plano sobre una ventana ya cerrada.
    public void stopGame() {
        gameThread = null; // el bucle en run() termina solo en su próxima vuelta
        AudioManager.stopMusic();
    }

    @Override
    public void run() {
        int FPS = 60;
        double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();
            try {
                double remaining = (nextDrawTime - System.nanoTime()) / 1_000_000;
                if (remaining < 0) remaining = 0;
                Thread.sleep((long) remaining);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (battleTrigger.isInBattle()) return; // pausar el mapa mientras hay batalla activa

        player.update();
        battleTrigger.checkEncounter(player, collisionChecker, TILE_SIZE);
        checkGrassAnimation();
        updateGrassAnimations();
    }

    // Si el jugador acaba de entrar a un tile nuevo y ese tile es hierba alta,
    // inicia una animación de hojas sobre ese tile específico.
    private void checkGrassAnimation() {
        if (!player.hasEnteredNewTile()) return;
        if (!collisionChecker.isInTallGrass(player.worldX, player.worldY, TILE_SIZE)) return;

        int tileX = (player.worldX + TILE_SIZE / 2) / TILE_SIZE;
        int tileY = (player.worldY + TILE_SIZE - 8) / TILE_SIZE;
        grassAnimations.add(new GrassAnimation(tileX, tileY));
    }

    // Avanza todas las animaciones activas y elimina las que ya terminaron
    private void updateGrassAnimations() {
        grassAnimations.forEach(GrassAnimation::update);
        grassAnimations.removeIf(GrassAnimation::isFinished);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int camX = player.worldX - PLAYER_SCREEN_X;
        int camY = player.worldY - PLAYER_SCREEN_Y;

        int maxCamX = mapReader.getMapWidth()  * TILE_SIZE - SCREEN_WIDTH;
        int maxCamY = mapReader.getMapHeight() * TILE_SIZE - SCREEN_HEIGHT;
        camX = Math.clamp(camX, 0, maxCamX);
        camY = Math.clamp(camY, 0, maxCamY);

        int playerScreenX = player.worldX - camX;
        int playerScreenY = player.worldY - camY;

        for (int[][] layer : mapReader.getVisualLayers()) {
            mapRenderer.drawLayer(g2, layer, camX, camY, SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);
        }

        mapRenderer.drawLayer(g2, mapReader.getGrassLayer(), camX, camY, SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);

        player.draw(g2, playerScreenX, playerScreenY);

        mapRenderer.drawGrassTile(g2, player.worldX, player.worldY, camX, camY, TILE_SIZE);

        // Animaciones de hierba encima del jugador (mismo nivel visual que la hierba alta)
        for (GrassAnimation anim : grassAnimations) {
            anim.draw(g2, camX, camY, TILE_SIZE);
        }

        mapRenderer.drawLayer(g2, mapReader.getRoofLayer(),  camX, camY, SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);
        mapRenderer.drawLayer(g2, mapReader.getRoofLayer2(), camX, camY, SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);

        g2.dispose();
    }
}