package xyz.tecsup.pokemon.battle;

import xyz.tecsup.pokemon.repository.TypeRepository;
import java.util.List;
import java.util.Map;

// Calcula el multiplicador de efectividad de un ataque contra los tipos del
// defensor. La tabla se carga UNA sola vez desde la BD (es pequeña y constante)
// y se reutiliza en todas las batallas, para no consultar la BD en cada golpe.
public class TypeChart {

    private static TypeChart instance;

    // [tipo atacante][tipo defensor] -> factor en % (200, 50, 0). Ausente = 100 (1x).
    private final Map<Integer, Map<Integer, Integer>> chart;

    private TypeChart(Map<Integer, Map<Integer, Integer>> chart) {
        this.chart = chart;
    }

    public static synchronized TypeChart get() {
        if (instance == null) {
            instance = new TypeChart(new TypeRepository().getEfficacyChart());
        }
        return instance;
    }

    // Multiplicador total del ataque contra el defensor: el producto del factor
    // contra cada uno de sus tipos (1 o 2). Ej: un ataque 2x contra un tipo y 2x
    // contra el otro da 4x; 2x y 0.5x da 1x. Un par sin entrada en la tabla es 1x.
    public double multiplier(int attackingType, List<Integer> defenderTypes) {
        Map<Integer, Integer> row = chart.get(attackingType);
        if (row == null) return 1.0; // ese tipo no tiene matchups especiales

        double multiplier = 1.0;
        for (int defenderType : defenderTypes) {
            Integer factor = row.get(defenderType);
            if (factor != null) {
                multiplier *= factor / 100.0;
            }
        }
        return multiplier;
    }
}
