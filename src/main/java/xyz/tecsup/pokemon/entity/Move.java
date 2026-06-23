package xyz.tecsup.pokemon.entity;

// Representa un movimiento/ataque ya cargado en memoria.
// Se construye desde MoveRepository consultando la tabla `moves` de MySQL.
public class Move {

    // Datos fijos del movimiento — vienen de la base de datos y no cambian en toda la partida
    private final int id;
    private final String name;
    private final int power;   // daño base, usado directamente en la fórmula de BattleManager
    private final int maxPp;   // usos máximos antes de quedarse sin PP

    // PP actual — único dato que cambia, baja 1 cada vez que se usa el movimiento en batalla
    private int currentPp;

    public Move(int id, String name, int power, int maxPp) {
        this.id = id;
        this.name = name;
        this.power = power;
        this.maxPp = maxPp;
        this.currentPp = maxPp; // todo movimiento empieza con el PP al máximo
    }

    // Usado antes de atacar, para saber si este movimiento todavía se puede elegir/usar
    public boolean hasPp() {
        return currentPp > 0;
    }

    // Gasta un PP. Se llama una vez por cada vez que el movimiento se usa en batalla.
    // No baja de 0 aunque se llame de más, por seguridad.
    public void use() {
        if (currentPp > 0) {
            currentPp--;
        }
    }

    // Getters
    public int getId()        { return id; }
    public String getName()   { return name; }
    public int getPower()     { return power; }
    public int getMaxPp()     { return maxPp; }
    public int getCurrentPp() { return currentPp; }
}
