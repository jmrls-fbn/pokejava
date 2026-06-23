package xyz.tecsup.pokemon.battle;

import xyz.tecsup.pokemon.entity.Pokemon;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Panel encargado únicamente de lo VISUAL de la zona de batalla:
// fondo, sprites de ambos Pokémon, y sus barras de HP.
// No contiene lógica de combate — solo refleja el estado actual de los
// objetos Pokemon que recibe (BattlePanel es quien decide cuándo actualizarlo).
public class SpritesPanel extends JPanel {

    private final Pokemon playerPokemon;
    private final Pokemon enemyPokemon;

    // Sprites cargados una sola vez al crear el panel (si cambia el Pokémon
    // activo, BattlePanel descarta este SpritesPanel y crea uno nuevo)
    private BufferedImage enemyFrontSprite;
    private BufferedImage playerBackSprite;
    private BufferedImage battleBackground;

    // Referencias a las barras para poder actualizarlas sin reconstruir todo el panel
    private JProgressBar playerHpBar;
    private JProgressBar enemyHpBar;

    public SpritesPanel(Pokemon playerPokemon, Pokemon enemyPokemon) {
        this.playerPokemon = playerPokemon;
        this.enemyPokemon = enemyPokemon;

        this.setPreferredSize(new Dimension(700, 400));
        this.setBackground(new Color(150, 220, 150)); // color de respaldo si el fondo no carga
        this.setLayout(null); // posicionamiento absoluto, igual que el resto del proyecto

        loadSprites();
        buildHpBars();
    }

    // Carga las 3 imágenes necesarias usando el id del Pokémon como nombre de archivo
    // (ej: 1.png = bulbasaur). Si algún sprite no existe, simplemente no se dibuja
    // (no rompe el programa, solo se imprime un aviso en consola).
    private void loadSprites() {
        try {
            enemyFrontSprite = ImageIO.read(
                    getClass().getResourceAsStream("/PokemonSprites/" + enemyPokemon.getId() + ".png")
            );
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("No se encontró sprite frontal de: " + enemyPokemon.getName());
        }

        try {
            playerBackSprite = ImageIO.read(
                    getClass().getResourceAsStream("/PokemonSprites/Back/" + playerPokemon.getId() + ".png")
            );
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("No se encontró sprite de espalda de: " + playerPokemon.getName());
        }

        try {
            battleBackground = ImageIO.read(
                    getClass().getResourceAsStream("/BattleBackground/BattleBackground.png")
            );
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("No se encontró el fondo de batalla.");
        }
    }

    // Coloca los paneles de HP en posiciones fijas: enemigo arriba-izquierda,
    // jugador abajo-derecha — el mismo acomodo visual que usa el juego original.
    private void buildHpBars() {
        JPanel enemyHpPanel = createHpPanel(enemyPokemon);
        enemyHpPanel.setBounds(20, 10, 250, 50);
        this.add(enemyHpPanel);

        JPanel playerHpPanel = createHpPanel(playerPokemon);
        playerHpPanel.setBounds(420, 280, 250, 50);
        this.add(playerHpPanel);
    }

    // Construye el panel de HP de un Pokémon: nombre+nivel arriba, barra abajo.
    // Guarda la referencia a la barra según a quién pertenece, para poder
    // actualizarla después desde updateHpBars().
    private JPanel createHpPanel(Pokemon pokemon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false); // deja ver el fondo de batalla detrás

        JLabel nameLabel = new JLabel(pokemon.getName() + "  Nv." + pokemon.getLevel());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JProgressBar bar = new JProgressBar(0, pokemon.getBaseHp());
        bar.setValue(pokemon.getCurrentHp());
        bar.setStringPainted(true);
        bar.setString(pokemon.getCurrentHp() + "/" + pokemon.getBaseHp());
        bar.setForeground(new Color(80, 200, 80));

        if (pokemon == playerPokemon) {
            playerHpBar = bar;
        } else {
            enemyHpBar = bar;
        }

        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(bar, BorderLayout.CENTER);
        return panel;
    }

    // Dibuja en orden: fondo (estirado a todo el panel), luego enemigo,
    // luego jugador. para que los sprites caigan dentro de los círculos de hierba del fondo.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (battleBackground != null) {
            g2.drawImage(battleBackground, 0, 0, getWidth(), getHeight(), null);
        }

        if (enemyFrontSprite != null) {
            g2.drawImage(enemyFrontSprite, 430, 60, 220, 220, null);
        }
        if (playerBackSprite != null) {
            g2.drawImage(playerBackSprite, 80, 220, 250, 250, null);
        }
    }

    // Llamado por BattlePanel después de cada turno (cuando el Pokémon activo
    // NO cambió) para refrescar las barras con el HP actualizado tras el ataque.
    public void updateHpBars() {
        playerHpBar.setValue(playerPokemon.getCurrentHp());
        playerHpBar.setString(playerPokemon.getCurrentHp() + "/" + playerPokemon.getBaseHp());

        enemyHpBar.setValue(enemyPokemon.getCurrentHp());
        enemyHpBar.setString(enemyPokemon.getCurrentHp() + "/" + enemyPokemon.getBaseHp());
    }
}
