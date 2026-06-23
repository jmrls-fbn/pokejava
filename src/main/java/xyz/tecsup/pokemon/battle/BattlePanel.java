package xyz.tecsup.pokemon.battle;

import xyz.tecsup.pokemon.entity.Move;
import xyz.tecsup.pokemon.entity.Pokemon;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

// Panel principal de la pantalla de batalla. Se encarga de la interfaz
// (mensaje, botones de movimientos) y de reaccionar a lo que decide BattleManager.
// Todo el dibujo de sprites/HP está delegado a SpritesPanel; toda la lógica
// de daño/turnos está delegada a BattleManager — este panel solo conecta ambos.
public class BattlePanel extends JPanel {

    private final List<Pokemon> playerTeam; // equipo completo, para poder restaurar HP al final
    private final Pokemon enemyPokemon;
    private final BattleManager battleManager;
    private SpritesPanel spritesPanel;

    private JLabel messageLabel;
    private JPanel movesButtonsPanel;
    private JPanel centerPanel; // contenedor que se vacía/reconstruye al cambiar de Pokémon activo

    // Se ejecuta al terminar la batalla (true=ganó, false=perdió), para que
    // GamePanel sepa que debe volver al mapa y desactivar el estado "inBattle"
    private Consumer<Boolean> onBattleEnd;

    public BattlePanel(List<Pokemon> playerTeam, Pokemon enemyPokemon) {
        this.playerTeam = playerTeam;
        this.enemyPokemon = enemyPokemon;
        this.battleManager = new BattleManager(playerTeam, enemyPokemon);

        this.setLayout(new BorderLayout());
        buildInterface();
    }

    public void setOnBattleEnd(Consumer<Boolean> callback) {
        this.onBattleEnd = callback;
    }

    // Arma el layout general: SpritesPanel arriba/centro, mensaje + botones abajo
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

    // SpritesPanel recibe los Pokémon en su constructor y no se puede "cambiarle"
    // el Pokémon después, así que cuando el activo cambia (se debilitó el anterior)
    // se descarta el SpritesPanel viejo y se crea uno nuevo con el Pokémon correcto.
    private void updateSpritesPanel() {
        centerPanel.removeAll();
        spritesPanel = new SpritesPanel(battleManager.getActivePokemon(), enemyPokemon);
        centerPanel.add(spritesPanel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // Reconstruye los 4 botones de movimiento según el Pokémon activo actual.
    // Se llama tanto al iniciar la batalla como después de cada turno (los PP cambian).
    private void updateMoveButtons() {
        movesButtonsPanel.removeAll();

        for (Move move : battleManager.getActivePokemon().getMoves()) {
            JButton button = new JButton(move.getName() + " (" + move.getCurrentPp() + "/" + move.getMaxPp() + ")");
            button.setEnabled(move.hasPp()); // botón deshabilitado si ya no tiene PP
            button.addActionListener(e -> executeTurn(move));
            movesButtonsPanel.add(button);
        }

        movesButtonsPanel.revalidate();
        movesButtonsPanel.repaint();
    }

    // Se llama al hacer clic en un botón de movimiento — dispara un turno completo
    // (ataque del jugador + ataque del enemigo) y refresca toda la interfaz.
    private void executeTurn(Move move) {
        Pokemon activeBefore = battleManager.getActivePokemon();

        battleManager.executeTurn(move);

        // Solo se muestra la primera línea del mensaje (puede haber varias,
        // por ejemplo "usó X" + "se debilitó" + "adelante Y")
        messageLabel.setText(battleManager.getLastMessage().split("\n")[0]);

        // Si el Pokémon activo cambió durante este turno, significa que el anterior
        // se debilitó y entró el siguiente del equipo — hay que reconstruir sprites
        if (battleManager.getActivePokemon() != activeBefore) {
            updateSpritesPanel();
        } else {
            spritesPanel.updateHpBars();
        }

        updateMoveButtons();

        if (battleManager.isBattleOver()) {
            endBattle();
        }
    }

    // Muestra el resultado, restaura el HP de todo el equipo
    // y avisa a GamePanel mediante el callback para volver al mapa.
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
