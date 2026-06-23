package xyz.tecsup.pokemon.main;

import xyz.tecsup.pokemon.control.KeyHandler;
import xyz.tecsup.pokemon.battle.BattlePanel;
import xyz.tecsup.pokemon.battle.EncounterGenerator;
import xyz.tecsup.pokemon.repository.PlayerRepository;
import xyz.tecsup.pokemon.repository.PokemonRepository;
import xyz.tecsup.pokemon.entity.Player;
import xyz.tecsup.pokemon.entity.Pokemon;
import xyz.tecsup.pokemon.entity.GameSession;
import xyz.tecsup.pokemon.map.CollisionChecker;
import xyz.tecsup.pokemon.map.MapReader;
import xyz.tecsup.pokemon.map.MapRenderer;

import javax.swing.JPanel;
import java.awt.*;
import java.util.List;

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
    private CollisionChecker collisionChecker;

    // Sistema de encuentros en hierba alta
    private final EncounterGenerator encounterGenerator = new EncounterGenerator();
    private boolean inBattle = false; // evita disparar batallas mientras ya hay una activa

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(34, 139, 34));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        this.requestFocusInWindow();

        player = new Player(keyHandler, TILE_SIZE);

        mapReader = new MapReader(
                "/RouteSprites/Route.tmj",
                List.of("/RouteSprites/Mountain.png",
                        "/RouteSprites/Water.png",
                        "/RouteSprites/ForestHouse.png"));
        mapRenderer = new MapRenderer(mapReader);
        collisionChecker = new CollisionChecker(mapReader);
        player.setCollisionChecker(collisionChecker);

        startCounter();
    }

    public synchronized void startCounter() {
        gameThread = new Thread(this);
        gameThread.start();
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
        if (inBattle) return; // pausar el mapa mientras hay batalla activa

        player.update();
        checkEncounter();
    }

    // Revisa si el jugador entró a hierba alta en un nuevo tile y dispara la batalla
    private void checkEncounter() {
        if (!player.hasEnteredNewTile()) return;

        boolean inGrass = collisionChecker.isInTallGrass(player.worldX, player.worldY, TILE_SIZE);
        if (!inGrass) return;

        if (encounterGenerator.tryEncounter()) {
            startBattle();
        }
    }

    // Construye los Pokémon y cambia al panel de batalla
    private void startBattle() {
        inBattle = true;

        PlayerRepository playerRepository = new PlayerRepository();
        PokemonRepository pokemonRepository = new PokemonRepository();

        List<Object[]> team = playerRepository.getTeam(GameSession.playerId);
        if (team.isEmpty()) {
            inBattle = false;
            return;
        }

        // Cargar TODO el equipo, no solo el primero
        List<Pokemon> playerTeam = new java.util.ArrayList<>();
        for (Object[] row : team) {
            int pokemonId = getPokemonIdFromRow(row);
            int level = (int) row[2];
            Pokemon pokemon = pokemonRepository.getById(pokemonId, level);
            playerTeam.add(pokemon);
        }

        int averageLevel = playerRepository.getAverageTeamLevel(GameSession.playerId);
        Pokemon enemyPokemon = encounterGenerator.generateEnemy(averageLevel);

        BattlePanel battlePanel = new BattlePanel(playerTeam, enemyPokemon);
        battlePanel.setOnBattleEnd(won -> {
            inBattle = false;
            Main.returnToMap();
        });

        Main.showBattle(battlePanel);
    }

    // Auxiliar para obtener el pokemon_id real (la fila de getTeam lo trae en el índice 4)
    private int getPokemonIdFromRow(Object[] row) {
        return (int) row[4];
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

        mapRenderer.drawLayer(g2, mapReader.getRoofLayer(),  camX, camY, SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);
        mapRenderer.drawLayer(g2, mapReader.getRoofLayer2(), camX, camY, SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);

        g2.dispose();
    }

    public void restartGame() {
        player           = new Player(keyHandler, TILE_SIZE);
        collisionChecker = new CollisionChecker(mapReader);
        player.setCollisionChecker(collisionChecker);
    }
}
