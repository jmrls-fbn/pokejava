package xyz.tecsup.pokemon.main;

import xyz.tecsup.pokemon.config.DatabaseConfig;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.sql.SQLException;

public class Main {

    // Referencias globales para poder cambiar de panel desde cualquier parte
    public static CardLayout cardLayout;
    public static JPanel panelContainer;
    public static GamePanel gamePanel;
    public static JFrame window;

    public static void main(String[] args) {

        // TODO: arranca directo en GamePanel con GameSession.playerId fijo.
        // Falta mostrar primero StartScreen para crear/elegir el jugador real
        // (quedó pausado para priorizar el sistema de combate).
        // Falla rápido y con un mensaje claro si la base de datos no está
        // disponible, en vez de abrir la ventana y recién explotar en la
        // primera batalla/consulta.
        try (var con = DatabaseConfig.getConnection()) {
            System.out.println("Conexión a la base de datos exitosa.");
        } catch (SQLException e) {
            System.out.println("Error conectando a la base de datos: " + e.getMessage());
            return;
        }

        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Pokémon - Tecsup (Java Edition)");

        // Contenedor con CardLayout para cambiar entre mapa y batalla
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

    // Cambia al panel de batalla, agregándolo dinámicamente
    public static void showBattle(javax.swing.JPanel battlePanel) {
        panelContainer.add(battlePanel, "battle");
        cardLayout.show(panelContainer, "battle");
    }

    // Vuelve al mapa
    public static void returnToMap() {
        cardLayout.show(panelContainer, "map");
        gamePanel.requestFocusInWindow();
    }
}
