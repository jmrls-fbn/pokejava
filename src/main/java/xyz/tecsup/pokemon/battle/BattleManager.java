package xyz.tecsup.pokemon.battle;

import xyz.tecsup.pokemon.entity.Move;
import xyz.tecsup.pokemon.entity.Pokemon;
import java.util.List;
import java.util.Random;

// El "cerebro" del combate. No dibuja nada en pantalla — solo decide qué pasa
// en cada turno: quién ataca primero, cuánto daño se hace, cuándo cambia el
// Pokémon activo del jugador, y cuándo termina la batalla.
// BattlePanel solo lo consulta y refleja visualmente lo que esta clase decide.
public class BattleManager {

    private final List<Pokemon> playerTeam; // equipo completo (para poder rotar si el activo se debilita)
    private final Pokemon enemyPokemon;      // Pokémon salvaje, único durante toda la batalla
    private final Random random = new Random();

    private int activeIndex = 0;       // qué posición del equipo está peleando ahora
    private String lastMessage = "";   // texto generado en el último turno, para mostrar en pantalla

    public BattleManager(List<Pokemon> playerTeam, Pokemon enemyPokemon) {
        this.playerTeam = playerTeam;
        this.enemyPokemon = enemyPokemon;
    }

    public Pokemon getActivePokemon() {
        return playerTeam.get(activeIndex);
    }

    public Pokemon getEnemyPokemon() {
        return enemyPokemon;
    }

    // Ejecuta un turno completo: jugador ataca con el movimiento elegido,
    // el enemigo ataca con un movimiento aleatorio. El orden depende de la
    // velocidad de cada uno — el más rápido golpea primero.
    public void executeTurn(Move playerMove) {
        StringBuilder message = new StringBuilder();
        Pokemon playerPokemon = getActivePokemon();

        boolean playerGoesFirst = playerPokemon.getSpeed() >= enemyPokemon.getSpeed();
        Move enemyMove = chooseRandomMove(enemyPokemon);

        if (playerGoesFirst) {
            attack(playerPokemon, enemyPokemon, playerMove, message);
            // El enemigo solo contraataca si sigue vivo después del primer golpe
            if (enemyPokemon.isAlive() && enemyMove != null) {
                attack(enemyPokemon, playerPokemon, enemyMove, message);
            }
        } else {
            if (enemyMove != null) {
                attack(enemyPokemon, playerPokemon, enemyMove, message);
            }
            if (playerPokemon.isAlive()) {
                attack(playerPokemon, enemyPokemon, playerMove, message);
            }
        }

        // Si el Pokémon activo del jugador se debilitó en este turno, se busca
        // automáticamente el siguiente disponible del equipo (todos se curaron
        // al inicio de la batalla, así que cualquiera vivo entra con HP completo)
        if (!getActivePokemon().isAlive()) {
            int next = findNextAlive();
            if (next != -1) {
                activeIndex = next;
                message.append("¡Adelante, ").append(getActivePokemon().getName()).append("!\n");
            }
            // Si no hay siguiente, activeIndex se queda apuntando al debilitado;
            // no importa porque isBattleOver() ya devolverá true en ese caso.
        }

        lastMessage = message.toString();
    }

    // Recorre el equipo en orden y devuelve el índice del primer Pokémon vivo.
    // -1 si absolutamente todos están debilitados (= derrota).
    private int findNextAlive() {
        for (int i = 0; i < playerTeam.size(); i++) {
            if (playerTeam.get(i).isAlive()) {
                return i;
            }
        }
        return -1;
    }

    // Aplica un ataque: gasta PP, calcula y resta daño, y registra el mensaje
    // correspondiente (incluido el aviso de efectividad por tipos).
    // Si el movimiento ya no tiene PP, no hace nada (mensaje de aviso).
    private void attack(Pokemon attacker, Pokemon defender, Move move, StringBuilder message) {
        if (!move.hasPp()) {
            message.append(attacker.getName()).append(" no tiene PP para ").append(move.getName()).append("!\n");
            return;
        }

        move.use();

        double effectiveness = TypeChart.get().multiplier(move.getTypeId(), defender.getTypeIds());
        int damage = calculateDamage(attacker, defender, move, effectiveness);
        defender.takeDamage(damage);

        message.append(attacker.getName())
                .append(" usó ").append(move.getName())
                .append("! Hizo ").append(damage).append(" de daño.\n");

        // Aviso de efectividad según el multiplicador de tipos
        if (effectiveness == 0) {
            message.append("No afecta a ").append(defender.getName()).append("...\n");
        } else if (effectiveness >= 2) {
            message.append("¡Es supereficaz!\n");
        } else if (effectiveness < 1) {
            message.append("No es muy eficaz...\n");
        }

        if (!defender.isAlive()) {
            message.append(defender.getName()).append(" se debilitó!\n");
        }
    }

    // Fórmula de daño basada en la canónica de Pokémon:
    //   base = ((2*Nivel/5 + 2) * Poder * Ataque/Defensa) / 50 + 2
    // ajustada por:
    //   - STAB: x1.5 si el tipo del movimiento coincide con un tipo del atacante.
    //   - efectividad: multiplicador por tipos del defensor (ya calculado fuera).
    //   - aleatorio: factor 0.85–1.0 para que el daño no sea siempre idéntico.
    // Si el ataque no afecta (efectividad 0) el daño es 0; en cualquier otro caso
    // es al menos 1, para que un golpe que conecta siempre haga algo.
    private int calculateDamage(Pokemon attacker, Pokemon defender, Move move, double effectiveness) {
        double base = ((2.0 * attacker.getLevel() / 5 + 2)
                * move.getPower() * attacker.getAttack() / defender.getDefense()) / 50 + 2;

        double stab = attacker.getTypeIds().contains(move.getTypeId()) ? 1.5 : 1.0;
        double randomFactor = 0.85 + random.nextDouble() * 0.15;

        if (effectiveness == 0) return 0;

        int damage = (int) Math.floor(base * stab * effectiveness * randomFactor);
        return Math.max(damage, 1);
    }

    // El enemigo "elige" su movimiento al azar entre los que aún tienen PP.
    // Devuelve null si ya se quedó sin PP en todos sus movimientos.
    private Move chooseRandomMove(Pokemon pokemon) {
        var available = pokemon.getMoves().stream()
                .filter(Move::hasPp)
                .toList();

        if (available.isEmpty()) return null;
        return available.get(random.nextInt(available.size()));
    }

    // La batalla termina si el enemigo murió, o si ya no queda ningún
    // Pokémon vivo en el equipo del jugador.
    public boolean isBattleOver() {
        return !enemyPokemon.isAlive() || findNextAlive() == -1;
    }

    // El jugador gana solo si el enemigo murió Y todavía le queda al menos
    // un Pokémon vivo (cubre el caso límite de que ambos caigan en el mismo turno)
    public boolean didPlayerWin() {
        return !enemyPokemon.isAlive() && findNextAlive() != -1;
    }

    public String getLastMessage() { return lastMessage; }
}
