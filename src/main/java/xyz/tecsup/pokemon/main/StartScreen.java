package xyz.tecsup.pokemon.main;

import xyz.tecsup.pokemon.repository.PlayerRepository;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// TODO: esta pantalla todavía no está conectada a Main (ver nota en Main.java)
// — Main arranca directo en GamePanel usando el jugador fijo de GameSession.
public class StartScreen {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JButton girlButton;
    private JButton boyButton;
    private JButton bulbasaurButton;
    private JButton squirtleButton;
    private JButton charmanderButton;
    private JButton playButton;

    // Selecciones del jugador
    private String selectedGender = null;
    private int initialPokemonId = -1; // -1 = nada seleccionado aún

    // Colores para marcar selección
    private final Color COLOR_NORMAL = UIManager.getColor("Button.background");
    private final Color COLOR_SELECTED = new Color(100, 180, 255);

    public StartScreen() {
        initializeEvents();
    }

    private void initializeEvents() {
        // Botones de género
        boyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedGender = "chico";
                markSelected(boyButton, girlButton);
            }
        });

        girlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedGender = "chica";
                markSelected(girlButton, boyButton);
            }
        });

        // Botones de Pokémon inicial
        bulbasaurButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialPokemonId = 1; // bulbasaur
                markSelected(bulbasaurButton, squirtleButton, charmanderButton);
            }
        });

        squirtleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialPokemonId = 7; // squirtle
                markSelected(squirtleButton, bulbasaurButton, charmanderButton);
            }
        });

        charmanderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initialPokemonId = 4; // charmander
                markSelected(charmanderButton, bulbasaurButton, squirtleButton);
            }
        });

        // Botón Jugar
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmSelection();
            }
        });
    }

    // Marca el botón elegido y desmarca los demás del mismo grupo
    private void markSelected(JButton selected, JButton... others) {
        selected.setBackground(COLOR_SELECTED);
        selected.setOpaque(true);
        selected.setBorderPainted(false);

        for (JButton other : others) {
            other.setBackground(COLOR_NORMAL);
            other.setOpaque(false);
            other.setBorderPainted(true);
        }
    }

    // Valida los datos y guarda en la base de datos
    private void confirmSelection() {
        String name = nameTextField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Escribe un nombre para tu personaje.");
            return;
        }
        if (selectedGender == null) {
            JOptionPane.showMessageDialog(mainPanel, "Elige si eres Chico o Chica.");
            return;
        }
        if (initialPokemonId == -1) {
            JOptionPane.showMessageDialog(mainPanel, "Elige tu Pokémon inicial.");
            return;
        }

        // Guardar en base de datos
        PlayerRepository playerRepository = new PlayerRepository();
        int playerId = playerRepository.createPlayer(name, selectedGender, initialPokemonId);

        if (playerId > 0) {
            // Cerrar esta ventana y abrir el juego
            JFrame currentWindow = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
            currentWindow.dispose();
            startGame();
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Error al crear el jugador.");
        }
    }

    // Abre la ventana principal del juego
    private void startGame() {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Pokémon - Tecsup (Java Edition)");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        MenuBar menuBar = new MenuBar(gamePanel);
        window.setJMenuBar(menuBar);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
