package xyz.tecsup.pokemon.main;

import xyz.tecsup.pokemon.entity.GameSession;
import xyz.tecsup.pokemon.repository.PlayerRepository;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

// Pantalla inicial: permite elegir una partida guardada del desplegable,
// o crear una nueva ingresando nombre, género e inicial.
public class StartScreen {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JButton girlButton;
    private JButton boyButton;
    private JButton bulbasaurButton;
    private JButton squirtleButton;
    private JButton charmanderButton;
    private JButton playButton;
    private JComboBox<String> comboBox1; // desplegable de partidas guardadas
    private JButton borrarPartidaButton;

    private final PlayerRepository playerRepository = new PlayerRepository();

    // Mapea cada texto del desplegable a su id real de jugador en la base de datos
    private final List<Object[]> savedGames = new java.util.ArrayList<>();

    private String selectedGender = null;
    private int initialPokemonId = -1;

    private final Color COLOR_NORMAL = UIManager.getColor("Button.background");
    private final Color COLOR_SELECTED = new Color(100, 180, 255);

    private static final String NEW_GAME_OPTION = "-- Nueva Partida --";

    public StartScreen() {
        loadSavedGames();
        initializeEvents();
    }

    // Llena el desplegable con las partidas existentes, más la opción de crear una nueva
    private void loadSavedGames() {
        savedGames.clear();
        comboBox1.removeAllItems();
        comboBox1.addItem(NEW_GAME_OPTION);

        for (Object[] row : playerRepository.getAllPlayers()) {
            savedGames.add(row);
            comboBox1.addItem((String) row[1]); // nombre del jugador
        }
    }

    private void initializeEvents() {
        boyButton.addActionListener(e -> {
            selectedGender = "chico";
            markSelected(boyButton, girlButton);
        });

        girlButton.addActionListener(e -> {
            selectedGender = "chica";
            markSelected(girlButton, boyButton);
        });

        bulbasaurButton.addActionListener(e -> {
            initialPokemonId = 3;
            markSelected(bulbasaurButton, squirtleButton, charmanderButton);
        });

        squirtleButton.addActionListener(e -> {
            initialPokemonId = 9;
            markSelected(squirtleButton, bulbasaurButton, charmanderButton);
        });

        charmanderButton.addActionListener(e -> {
            initialPokemonId = 6;
            markSelected(charmanderButton, bulbasaurButton, squirtleButton);
        });

        // Si el jugador elige una partida del desplegable, deshabilitar los
        // campos de creación (ya no aplican)
        comboBox1.addActionListener(e -> {
            boolean creatingNew = Objects.equals(comboBox1.getSelectedItem(), NEW_GAME_OPTION);
            setCreationFieldsEnabled(creatingNew);
        });

        playButton.addActionListener(e -> confirmSelection());

        borrarPartidaButton.addActionListener(e -> deleteSelectedGame());
    }

    // Borra la partida actualmente elegida en el desplegable, pidiendo
    // confirmación antes para evitar borrados accidentales.
    private void deleteSelectedGame() {
        String selectedOption = (String) comboBox1.getSelectedItem();

        if (Objects.equals(selectedOption, NEW_GAME_OPTION)) {
            JOptionPane.showMessageDialog(mainPanel, "Elige una partida del desplegable para borrarla.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Seguro que quieres borrar la partida \"" + selectedOption + "\"? Esto no se puede deshacer.",
                "Confirmar borrado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        for (Object[] row : savedGames) {
            if (row[1].equals(selectedOption)) {
                boolean deleted = playerRepository.deletePlayer((int) row[0]);
                if (deleted) {
                    loadSavedGames(); // refresca el desplegable sin la partida borrada
                    comboBox1.setSelectedItem(NEW_GAME_OPTION);
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Error al borrar la partida.");
                }
                return;
            }
        }
    }

    // Habilita/deshabilita los campos de creación según si se eligió una partida existente
    private void setCreationFieldsEnabled(boolean enabled) {
        nameTextField.setEnabled(enabled);
        boyButton.setEnabled(enabled);
        girlButton.setEnabled(enabled);
        bulbasaurButton.setEnabled(enabled);
        squirtleButton.setEnabled(enabled);
        charmanderButton.setEnabled(enabled);
    }

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

    // Decide si cargar una partida existente o crear una nueva, según el desplegable
    private void confirmSelection() {
        String selectedOption = (String) comboBox1.getSelectedItem();

        assert selectedOption != null;
        if (!selectedOption.equals(NEW_GAME_OPTION)) {
            loadExistingGame(selectedOption);
        } else {
            createNewGame();
        }
    }

    // Busca el id correspondiente al nombre elegido en el desplegable y lo
    // asigna como jugador activo de la sesión, ignorando nombre/género/inicial
    private void loadExistingGame(String selectedName) {
        for (Object[] row : savedGames) {
            if (row[1].equals(selectedName)) {
                GameSession.playerId = (int) row[0];
                closeAndStartGame();
                return;
            }
        }
        JOptionPane.showMessageDialog(mainPanel, "No se pudo cargar esa partida.");
    }

    // Valida los campos y crea un jugador nuevo en la base de datos
    private void createNewGame() {
        String name = nameTextField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Escribe un nombre para tu personaje.");
            return;
        }
        if (selectedGender == null) {
            JOptionPane.showMessageDialog(mainPanel, "Elige Personaje.");
            return;
        }
        if (initialPokemonId == -1) {
            JOptionPane.showMessageDialog(mainPanel, "Elige tu Pokémon inicial.");
            return;
        }

        int playerId = playerRepository.createPlayer(name, selectedGender, initialPokemonId);

        if (playerId > 0) {
            GameSession.playerId = playerId;
            closeAndStartGame();
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Error al crear el jugador.");
        }
    }

    private void closeAndStartGame() {
        JFrame currentWindow = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        currentWindow.dispose();
        Main.startMainWindow();
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}