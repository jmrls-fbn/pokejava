package xyz.tecsup.pokemon.battle;

import xyz.tecsup.pokemon.sounds.AudioManager;
import xyz.tecsup.pokemon.entity.GameSession;
import xyz.tecsup.pokemon.entity.Player;
import xyz.tecsup.pokemon.entity.Pokemon;
import xyz.tecsup.pokemon.main.Main;
import xyz.tecsup.pokemon.map.CollisionChecker;
import xyz.tecsup.pokemon.repository.PlayerRepository;
import xyz.tecsup.pokemon.repository.PokemonRepository;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

// Decide cuándo iniciar una batalla (al pisar hierba alta) y prepara todo
// lo necesario para hacerlo: equipo del jugador, enemigo aleatorio, y el
// cambio de panel/música correspondiente. GamePanel solo lo consulta cada frame.
public class BattleTrigger {

    private final EncounterGenerator encounterGenerator = new EncounterGenerator();
    private final PlayerRepository playerRepository = new PlayerRepository();
    private final PokemonRepository pokemonRepository = new PokemonRepository();

    private boolean inBattle = false;

    public boolean isInBattle() {
        return inBattle;
    }

    // Revisa si el jugador entró a hierba alta en un nuevo tile y, si corresponde,
    // dispara la batalla. tileSize se necesita para consultar la capa de colisión.
    public void checkEncounter(Player player, CollisionChecker collisionChecker, int tileSize) {
        if (inBattle) return;
        if (!player.hasEnteredNewTile()) return;

        boolean inGrass = collisionChecker.isInTallGrass(player.worldX, player.worldY, tileSize);
        if (!inGrass) return;

        if (encounterGenerator.tryEncounter()) {
            startBattle();
        }
    }

    // Construye los Pokémon (equipo completo + enemigo aleatorio) y muestra
    // primero la transición de espiral; el BattlePanel real se crea cuando
    // la transición termina.
    private void startBattle() {
        inBattle = true;

        List<Object[]> team = playerRepository.getTeam(GameSession.playerId);
        if (team.isEmpty()) {
            inBattle = false;
            return;
        }

        List<Pokemon> playerTeam = new ArrayList<>();
        for (Object[] row : team) {
            int pokemonId = (int) row[4];
            int level = (int) row[2];
            playerTeam.add(pokemonRepository.getById(pokemonId, level));
        }

        int averageLevel = playerRepository.getAverageTeamLevel(GameSession.playerId);
        Pokemon enemyPokemon = encounterGenerator.generateEnemy(averageLevel);

        AudioManager.playMusic("/PokemonOST/Battle.wav", -12f);

        // Mostrar la transición de espiral encima del mapa actual usando el glassPane
        JFrame window = Main.window;
        SpiralTransition transition = new SpiralTransition(window.getWidth(), window.getHeight());

        transition.setOnComplete(() -> {
            window.setGlassPane(new JPanel()); // limpiar el glassPane
            window.getGlassPane().setVisible(false);

            BattlePanel battlePanel = new BattlePanel(playerTeam, enemyPokemon);
            battlePanel.setOnBattleEnd(won -> {
                inBattle = false;
                AudioManager.playMusic("/PokemonOST/Route.wav", -12f);
                Main.returnToMap();
            });
            Main.showBattle(battlePanel);
        });

        window.setGlassPane(transition);
        transition.setVisible(true);
    }
}