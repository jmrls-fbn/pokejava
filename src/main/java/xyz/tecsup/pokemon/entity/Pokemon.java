package xyz.tecsup.pokemon.entity;

import java.util.List;

// Representa un Pokémon ya cargado en memoria, listo para usarse en batalla.
// Se construye normalmente desde PokemonRepository, que llena estos datos consultando SQLite.
public class Pokemon {

    // Datos fijos de la especie: vienen de la base de datos y nunca cambian
    // una vez creado el objeto (son las stats base, no las del Pokémon individual del jugador)
    private final int id;                  // id de la especie en la tabla `pokemon`
    private final String name;
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseSpeed;
    private final List<Move> moves;        // hasta 4 movimientos disponibles a este nivel

    //Datos que sí cambian durante la partida/batalla
    private int level;
    private int currentHp; // baja al recibir daño, se restaura al terminar la batalla

    public Pokemon(int id, String name, int baseHp, int baseAttack,
                   int baseDefense, int baseSpeed, int level,
                   List<Move> moves) {
        this.id = id;
        this.name = name;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.level = level;
        this.moves = moves;
        this.currentHp = baseHp; // todo Pokémon empieza con la vida al máximo
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

    // Suma una cantidad específica de HP, sin pasar el máximo (baseHp).
    // Pensado para usarse con movimientos que curan HP durante la batalla.
    public void heal(int amount) {
        currentHp += amount;
        if (currentHp > baseHp) {
            currentHp = baseHp;
        }
    }

    // Restaura el HP al máximo de una sola vez — se llama al finalizar cada batalla,
    // el equipo se cura completamente entre combates.
    // TODO: reemplazar por un Centro Pokémon real (curación manual/opcional) más adelante.
    public void restoreHp() {
        currentHp = baseHp;
    }

    //Getters: solo lectura, ningún dato fijo se puede modificar desde afuera
    public int getId()             { return id; }
    public String getName()        { return name; }
    public int getBaseHp()         { return baseHp; }
    public int getBaseAttack()     { return baseAttack; }
    public int getBaseDefense()    { return baseDefense; }
    public int getBaseSpeed()      { return baseSpeed; }
    public int getLevel()          { return level; }
    public int getCurrentHp()      { return currentHp; }
    public List<Move> getMoves()   { return moves; }
}
