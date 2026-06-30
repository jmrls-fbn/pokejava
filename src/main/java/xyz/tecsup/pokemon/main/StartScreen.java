package xyz.tecsup.pokemon.main;

import xyz.tecsup.pokemon.entity.GameSession;
import xyz.tecsup.pokemon.repository.PlayerRepository;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

// Pantalla inicial: permite elegir una partida guardada del desplegable,
// o crear una nueva ingresando nombre, género e inicial.
public class StartScreen {
    // Componentes de la pantalla. Antes los instanciaba el GUI Designer de IntelliJ
    // (StartScreen.form); ahora se construyen a mano en buildUi() para que el juego
    // funcione con Swing puro, sin depender del binder de IntelliJ al empaquetar el JAR.
    private final JPanel mainPanel = new JPanel(new GridBagLayout());
    private final JTextField nameTextField = new JTextField();
    private final JButton girlButton = new JButton("Chica");
    private final JButton boyButton = new JButton("Chico");
    private final JButton bulbasaurButton = new JButton("Venasaur");
    private final JButton squirtleButton = new JButton("Blastoise");
    private final JButton charmanderButton = new JButton("Charizard");
    private final JButton playButton = new JButton("Jugar");
    private final JComboBox<String> comboBox1 = new JComboBox<>(); // desplegable de partidas guardadas
    private final JButton borrarPartidaButton = new JButton("Borrar Partida");

    private final PlayerRepository playerRepository = new PlayerRepository();

    private final List<Object[]> savedGames = new java.util.ArrayList<>();

    private String selectedGender = null;
    private int initialPokemonId = -1;

    private final Color COLOR_NORMAL = UIManager.getColor("Button.background");
    private final Color COLOR_SELECTED = new Color(100, 180, 255);

    private static final String NEW_GAME_OPTION = "-- Nueva Partida --";

    public StartScreen() {
        buildUi();
        loadSavedGames();
        initializeEvents();
        loadGenderIcons();
        loadPokemonIcons();
    }

    // Arma el layout que antes definía StartScreen.form: una grilla de 6 filas x
    // 4 columnas (GridBagLayout). Cada fila replica la disposición original:
    //   0: "Nombre"            + campo de texto
    //   1: "Elije Personaje"
    //   2: botón Chico         + botón Chica
    //   3: "Elije a tu Pokemon"
    //   4: Venasaur + Blastoise + Charizard (iniciales)
    //   5: Borrar Partida + desplegable de partidas + Jugar
    private void buildUi() {
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1, 0xD1, 0xD9)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        mainPanel.setPreferredSize(new Dimension(435, 400));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Nombre
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 0;
        mainPanel.add(new JLabel("Nombre"), c);
        c.gridx = 2; c.gridwidth = 2; c.weightx = 1;
        mainPanel.add(nameTextField, c);

        // Fila 1: Elije Personaje
        c.gridx = 0; c.gridy = 1; c.gridwidth = 4; c.weightx = 0;
        mainPanel.add(new JLabel("Elije Personaje"), c);

        // Fila 2: Chico / Chica (botones que crecen verticalmente)
        c.gridy = 2; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.gridx = 0; c.gridwidth = 2; c.weightx = 1;
        mainPanel.add(boyButton, c);
        c.gridx = 2; c.gridwidth = 2;
        mainPanel.add(girlButton, c);

        // Fila 3: Elije a tu Pokemon
        c.fill = GridBagConstraints.HORIZONTAL; c.weighty = 0;
        c.gridx = 0; c.gridy = 3; c.gridwidth = 4; c.weightx = 0;
        mainPanel.add(new JLabel("Elije a tu Pokemon"), c);

        // Fila 4: iniciales
        c.gridy = 4; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        c.gridx = 0; c.gridwidth = 2; c.weightx = 1;
        mainPanel.add(bulbasaurButton, c);
        c.gridx = 2; c.gridwidth = 1;
        mainPanel.add(squirtleButton, c);
        c.gridx = 3; c.gridwidth = 1;
        mainPanel.add(charmanderButton, c);

        // Fila 5: Borrar Partida / desplegable / Jugar
        c.fill = GridBagConstraints.HORIZONTAL; c.weighty = 0;
        c.gridy = 5;
        c.gridx = 0; c.gridwidth = 2; c.weightx = 1;
        mainPanel.add(borrarPartidaButton, c);
        c.gridx = 2; c.gridwidth = 1;
        mainPanel.add(comboBox1, c);
        c.gridx = 3; c.gridwidth = 1;
        mainPanel.add(playButton, c);
    }

    // Carga los sprites de chico/chica como íconos de sus botones.
    // Se hace desde código (no desde el GUI Designer) porque el Designer
    // no permitía asignar la imagen correctamente.
    private void loadGenderIcons() {
        BufferedImage boyImage = loadImage("/PlayerSprites/Chico.png");
        BufferedImage girlImage = loadImage("/PlayerSprites/Chica.png");

        if (boyImage != null) {
            boyButton.setIcon(new ImageIcon(boyImage.getScaledInstance(80, 120, Image.SCALE_SMOOTH)));
            boyButton.setText(null); // el sprite reemplaza el texto del botón
        }
        if (girlImage != null) {
            girlButton.setIcon(new ImageIcon(girlImage.getScaledInstance(80, 120, Image.SCALE_SMOOTH)));
            girlButton.setText(null);
        }
    }

    private void loadPokemonIcons(){
        BufferedImage venasaurImage = loadImage("/PokemonSprites/3.png");
        BufferedImage blastoiseImage = loadImage("/PokemonSprites/9.png");
        BufferedImage charizarImage = loadImage("/PokemonSprites/6.png");

        if (venasaurImage != null){
            bulbasaurButton.setIcon(new ImageIcon(venasaurImage.getScaledInstance(80,80, Image.SCALE_SMOOTH)));
            bulbasaurButton.setText(null);
        }
        if (blastoiseImage != null){
            squirtleButton.setIcon(new ImageIcon(blastoiseImage.getScaledInstance(80,80, Image.SCALE_SMOOTH)));
            squirtleButton.setText(null);
        }
        if (charizarImage != null){
            charmanderButton.setIcon(new ImageIcon(charizarImage.getScaledInstance(80,80, Image.SCALE_SMOOTH)));
            charmanderButton.setText(null);
        }
    }

    // Carga una imagen desde resources, devuelve null si no se encuentra
    // (en vez de lanzar excepción) para que el resto de la pantalla siga
    // funcionando aunque falte algún sprite.
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        } catch (IOException | NullPointerException e) {
            System.out.println("No se encontró la imagen: " + path);
            return null;
        }
    }

    // Llena el desplegable con las partidas existentes, más la opción de crear una nueva
    private void loadSavedGames() {
        savedGames.clear();
        comboBox1.removeAllItems();
        comboBox1.addItem(NEW_GAME_OPTION);

        for (Object[] row : playerRepository.getAllPlayers()) {
            savedGames.add(row);
            comboBox1.addItem((String) row[1]);
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

        comboBox1.addActionListener(e -> {
            boolean creatingNew = Objects.equals(comboBox1.getSelectedItem(), NEW_GAME_OPTION);
            setCreationFieldsEnabled(creatingNew);
        });

        playButton.addActionListener(e -> confirmSelection());

        borrarPartidaButton.addActionListener(e -> deleteSelectedGame());
    }

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
                    loadSavedGames();
                    comboBox1.setSelectedItem(NEW_GAME_OPTION);
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "Error al borrar la partida.");
                }
                return;
            }
        }
    }

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

    private void confirmSelection() {
        String selectedOption = (String) comboBox1.getSelectedItem();

        assert selectedOption != null;
        if (!selectedOption.equals(NEW_GAME_OPTION)) {
            loadExistingGame(selectedOption);
        } else {
            createNewGame();
        }
    }

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