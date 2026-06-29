package xyz.tecsup.pokemon.battle;

import xyz.tecsup.pokemon.sounds.AudioManager;
import xyz.tecsup.pokemon.entity.Move;
import xyz.tecsup.pokemon.entity.Pokemon;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class BattlePanel extends JPanel {

    private final List<Pokemon> playerTeam;
    private final Pokemon enemyPokemon;
    private final BattleManager battleManager;
    private SpritesPanel spritesPanel;

    private JLabel messageLabel;
    private JPanel movesButtonsPanel;
    private JPanel centerPanel;

    private Consumer<Boolean> onBattleEnd;

    public BattlePanel(List<Pokemon> playerTeam, Pokemon enemyPokemon) {
        this.playerTeam = playerTeam;
        this.enemyPokemon = enemyPokemon;
        this.battleManager = new BattleManager(playerTeam, enemyPokemon);

        this.setLayout(new BorderLayout());
        buildInterface();

        // Cries al iniciar la batalla: primero el enemigo (que es quien aparece
        // primero visualmente), luego el del jugador
        AudioManager.playPokemonCry(enemyPokemon.getId());
        AudioManager.playPokemonCry(battleManager.getActivePokemon().getId());
    }

    public void setOnBattleEnd(Consumer<Boolean> callback) {
        this.onBattleEnd = callback;
    }

    private void buildInterface() {
        centerPanel = new JPanel(new BorderLayout());
        updateSpritesPanel();
        this.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(700, 150));
        bottomPanel.setBackground(new Color(230, 230, 230));

        messageLabel = new JLabel("¡Un " + enemyPokemon.getName() + " salvaje apareció!");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(messageLabel, BorderLayout.NORTH);

        movesButtonsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        updateMoveButtons();
        bottomPanel.add(movesButtonsPanel, BorderLayout.CENTER);

        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateSpritesPanel() {
        centerPanel.removeAll();
        spritesPanel = new SpritesPanel(battleManager.getActivePokemon(), enemyPokemon);
        centerPanel.add(spritesPanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void updateMoveButtons() {
        movesButtonsPanel.removeAll();

        for (Move move : battleManager.getActivePokemon().getMoves()) {
            JButton button = new JButton(move.getName() + " (" + move.getCurrentPp() + "/" + move.getMaxPp() + ")");
            button.setEnabled(move.hasPp());
            button.addActionListener(e -> executeTurn(move));
            movesButtonsPanel.add(button);
        }

        movesButtonsPanel.revalidate();
        movesButtonsPanel.repaint();
    }

    private void executeTurn(Move move) {
        Pokemon activeBefore = battleManager.getActivePokemon();

        battleManager.executeTurn(move);

        messageLabel.setText(battleManager.getLastMessage().split("\n")[0]);

        // Si entró un Pokémon nuevo (el anterior se debilitó), reproducir su cry
        if (battleManager.getActivePokemon() != activeBefore) {
            updateSpritesPanel();
            AudioManager.playPokemonCry(battleManager.getActivePokemon().getId());
        } else {
            spritesPanel.updateHpBars();
        }

        updateMoveButtons();

        if (battleManager.isBattleOver()) {
            endBattle();
        }
    }

    private void endBattle() {
        boolean won = battleManager.didPlayerWin();
        String result = won ? "¡Ganaste la batalla!" : "Has perdido...";

        JOptionPane.showMessageDialog(this, result);

        for (Pokemon p : playerTeam) {
            p.restoreHp();
        }
        enemyPokemon.restoreHp();

        if (onBattleEnd != null) {
            onBattleEnd.accept(won);
        }
    }
}