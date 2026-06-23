package xyz.tecsup.pokemon.main;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import xyz.tecsup.pokemon.repository.PlayerRepository;
import xyz.tecsup.pokemon.repository.PokemonRepository;
import xyz.tecsup.pokemon.entity.GameSession;

import javax.swing.table.DefaultTableModel;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.util.List;

public class MenuBar extends JMenuBar {

    private final GamePanel gamePanel;

    public MenuBar(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        initializeMenu();
    }

    private void initializeMenu() {
        // 1. Pestaña PRINCIPAL: Juego
        JMenu gameMenu = getJMenu();

        // 2. Pestaña PRINCIPAL: Ayuda
        JMenu helpMenu = new JMenu("Ayuda");
        JMenuItem controlsItem = new JMenuItem("Controles");
        // TODO: falta agregar volumeItem al menú y darle una acción (control de volumen no implementado)
        JMenuItem volumeItem = new JMenuItem("Volumen");

        // Acción de Controles (Ventana emergente)
        controlsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        """
                                Controles del Emulador:

                                • Movimiento: Flechas del teclado o WASD
                                • Interactuar / Aceptar: Tecla ESPACIO
                                • Menú / Cancelar: Tecla ESCAPE""",
                        "Controles",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        helpMenu.add(controlsItem);

        // Nuevo menú "Base de Datos" con la lista de Pokémon
        JMenu databaseMenu = new JMenu("Base de Datos");
        JMenuItem pokemonListItem = new JMenuItem("Lista de Pokémon");

        JMenuItem teamItem = new JMenuItem("Mi Equipo");
        teamItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTeam();
            }
        });

        databaseMenu.add(teamItem);

        pokemonListItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPokemonList();
            }
        });

        databaseMenu.add(pokemonListItem);

        // Añadir las pestañas a la barra de menú contenedora
        this.add(gameMenu);
        this.add(helpMenu);
        this.add(databaseMenu);
    }

    // Crea y muestra una ventana con la tabla de Pokémon cargados en la BD
    private void showPokemonList() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(gamePanel),
                "Lista de Pokémon", true);
        dialog.setSize(300, 400);
        dialog.setLocationRelativeTo(gamePanel);

        String[] columns = { "ID", "Nombre" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        PokemonRepository repository = new PokemonRepository();
        List<Object[]> pokemonList = repository.getAll();
        for (Object[] row : pokemonList) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        dialog.add(scroll);

        dialog.setVisible(true);
    }

    // Muestra una ventana con el equipo actual del jugador
    private void showTeam() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(gamePanel),
                "Mi Equipo", true);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(gamePanel);

        String[] columns = { "ID", "Pokémon", "Nivel", "HP" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        PlayerRepository repository = new PlayerRepository();
        List<Object[]> team = repository.getTeam(GameSession.playerId);
        for (Object[] row : team) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        dialog.add(scroll);

        dialog.setVisible(true);
    }

    private JMenu getJMenu() {
        JMenu gameMenu = new JMenu("Juego");

        JMenuItem restartItem = new JMenuItem("Reiniciar");
        JMenuItem exitItem = new JMenuItem("Salir");

        // Acción de Reiniciar
        restartItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.restartGame(); // Le avisa al panel que reinicie
            }
        });

        // Acción de Salir
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Cierra el programa de forma segura
            }
        });

        gameMenu.add(restartItem);
        gameMenu.addSeparator(); // Línea divisoria estética
        gameMenu.add(exitItem);
        return gameMenu;
    }
}
