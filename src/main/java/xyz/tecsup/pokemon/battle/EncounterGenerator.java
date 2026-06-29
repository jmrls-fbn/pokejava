package xyz.tecsup.pokemon.battle;

import xyz.tecsup.pokemon.repository.PokemonRepository;
import xyz.tecsup.pokemon.entity.Pokemon;
import java.util.Random;

// Decide CUÁNDO aparece un Pokémon salvaje y CUÁL/QUÉ NIVEL le toca.
// Usado por GamePanel cada vez que el jugador entra a un tile de hierba alta.
public class EncounterGenerator {

    private final Random random = new Random();
    private final PokemonRepository pokemonRepository = new PokemonRepository();

    // Probabilidad de encuentro por cada tile de hierba pisado (0-100).
    // 15 significa ~15% de chance cada vez que se entra a un tile nuevo de hierba
    // (no en cada frame — GamePanel solo llama esto cuando Player.hasEnteredNewTile() es true)
    private static final int ENCOUNTER_PROBABILITY = 5;

    // Tira un dado de 0-99 y compara contra el umbral de probabilidad.
    public boolean tryEncounter() {
        int roll = random.nextInt(100);
        return roll < ENCOUNTER_PROBABILITY;
    }

    // Construye un Pokémon salvaje completamente al azar entre los 151 disponibles,
    // con un nivel cercano al nivel promedio del equipo del jugador (±5, sin bajar de 2)
    // para que la dificultad del encuentro sea coherente con el progreso del jugador.
    public Pokemon generateEnemy(int averagePlayerLevel) {
        int randomPokemonId = random.nextInt(151) + 1; // ids del 1 al 151

        int variation = random.nextInt(11) - 5; // número entre -5 y +5
        int enemyLevel = Math.max(2, averagePlayerLevel + variation);

        return pokemonRepository.getById(randomPokemonId, enemyLevel);
    }
}
