package xyz.tecsup.pokemon.main;

import xyz.tecsup.pokemon.config.DatabaseConfig;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.sql.SQLException;

public class Main {

    public static CardLayout cardLayout;
    public static JPanel panelContainer;
    public static GamePanel gamePanel;
    public static JFrame window;

    public static void main(String[] args) {

        // Falla rápido y con un mensaje claro si la base de datos no está
        // disponible, en vez de abrir la ventana y recién explotar en la
        // primera consulta (StartScreen necesita la BD para listar partidas).
        try (var con = DatabaseConfig.getConnection()) {
            System.out.println("Conexión a la base de datos exitosa.");
        } catch (SQLException e) {
            System.out.println("Error conectando a la base de datos: " + e.getMessage());
            return;
        }

        showStartScreen();
    }

    // Crea y muestra la ventana de StartScreen (elegir/crear partida).
    // Usado tanto al arrancar el programa como al presionar "Reiniciar".
    public static void showStartScreen() {
        JFrame startWindow = new JFrame();
        startWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startWindow.setResizable(false);
        startWindow.setTitle("Pokémon - Tecsup (Java Edition)");

        StartScreen startScreen = new StartScreen();
        startWindow.add(startScreen.getPanel());

        startWindow.pack();
        startWindow.setLocationRelativeTo(null);
        startWindow.setVisible(true);
    }

    // Construye y muestra la ventana principal del juego (mapa + batalla).
    // Llamado por StartScreen una vez que ya existe un GameSession.playerId válido.
    public static void startMainWindow() {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Pokémon - Tecsup (Java Edition)");

        cardLayout = new CardLayout();
        panelContainer = new JPanel(cardLayout);

        gamePanel = new GamePanel();
        panelContainer.add(gamePanel, "map");

        window.add(panelContainer);

        MenuBar menuBar = new MenuBar(gamePanel);
        window.setJMenuBar(menuBar);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    public static void showBattle(javax.swing.JPanel battlePanel) {
        panelContainer.add(battlePanel, "battle");
        cardLayout.show(panelContainer, "battle");
    }

    public static void returnToMap() {
        cardLayout.show(panelContainer, "map");
        gamePanel.requestFocusInWindow();
    }
}