package xyz.tecsup.pokemon.entity;

import java.util.List;

// Representa un Pokémon ya cargado en memoria, listo para usarse en batalla.
// Se construye normalmente desde PokemonRepository, que llena estos datos consultando SQLite.
public class Pokemon {

    // Datos fijos de la especie: stats BASE (de la tabla pokemon_stat) y tipos.
    // No cambian una vez creado el objeto.
    private final int id;
    private final String name;
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseSpeed;
    private final List<Move> moves;          // hasta 4 movimientos disponibles a este nivel
    private final List<Integer> typeIds;     // 1 o 2 tipos (FK a types): definen STAB y efectividad

    private final int level;

    // Stats REALES a este nivel, calculadas a partir de las base con la fórmula
    // canónica de Pokémon (sin IVs/EVs). Antes se usaban las base directamente,
    // por lo que el nivel casi no influía; ahora un nivel más alto sube todas las stats.
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;

    private int currentHp; // baja al recibir daño, se restaura al terminar la batalla

    public Pokemon(int id, String name, int baseHp, int baseAttack,
                   int baseDefense, int baseSpeed, int level,
                   List<Move> moves, List<Integer> typeIds) {
        this.id = id;
        this.name = name;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.level = level;
        this.moves = moves;
        this.typeIds = typeIds;

        // Fórmula canónica (IV/EV = 0):
        //   HP    = floor(2*Base*Nivel/100) + Nivel + 10
        //   resto = floor(2*Base*Nivel/100) + 5
        this.maxHp   = (2 * baseHp * level) / 100 + level + 10;
        this.attack  = (2 * baseAttack * level) / 100 + 5;
        this.defense = (2 * baseDefense * level) / 100 + 5;
        this.speed   = (2 * baseSpeed * level) / 100 + 5;

        this.currentHp = maxHp; // todo Pokémon empieza con la vida al máximo
    }

    // Usado por BattleManager para saber si este Pokémon puede seguir peleando
    public boolean isAlive() {
        return currentHp > 0;
    }

    // Resta daño al HP actual, sin permitir que baje de 0 (evita HP negativo)
    public void takeDamage(int damage) {
        currentHp -= damage;
        if (currentHp < 0) {
            currentHp = 0;
        }
    }

    // Suma una cantidad específica de HP, sin pasar el máximo.
    // Pensado para usarse con movimientos que curan HP durante la batalla.
    public void heal(int amount) {
        currentHp += amount;
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
    }

    // Restaura el HP al máximo de una sola vez — se llama al finalizar cada batalla,
    // el equipo se cura completamente entre combates.
    // TODO: reemplazar por un Centro Pokémon real (curación manual/opcional) más adelante.
    public void restoreHp() {
        currentHp = maxHp;
    }

    //Getters: solo lectura, ningún dato fijo se puede modificar desde afuera
    public int getId()                 { return id; }
    public String getName()            { return name; }
    public int getLevel()              { return level; }
    public int getCurrentHp()          { return currentHp; }
    public List<Move> getMoves()       { return moves; }
    public List<Integer> getTypeIds()  { return typeIds; }

    // Stats reales a este nivel (las que usan la fórmula de daño y la barra de HP)
    public int getMaxHp()   { return maxHp; }
    public int getAttack()  { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed()   { return speed; }
}
