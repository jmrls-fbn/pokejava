package xyz.tecsup.pokemon.battle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// Transición de tiles negros que se cierran en espiral desde el borde
// exterior hacia el centro, cubriendo toda la pantalla antes de pasar
// al combate. Replica el efecto clásico de transición a batalla del juego original.
public class SpiralTransition extends JPanel implements ActionListener {

    private static final int TILE_SIZE = 32; // tamaño de cada cuadro de la cuadrícula
    private int cols, rows;
    private boolean[][] filled; // qué tiles ya están pintados de negro

    private List<Point> spiralOrder; // orden en que se van llenando los tiles
    private int currentIndex = 0;
    private static final int TILES_PER_TICK = 4; // cuántos tiles se llenan por frame (velocidad)

    private final Timer timer;
    private Runnable onComplete;

    public SpiralTransition(int panelWidth, int panelHeight) {
        this.setPreferredSize(new Dimension(panelWidth, panelHeight));
        this.setOpaque(false); // dejamos ver el panel de atrás (el mapa) donde no hay tiles negros aún

        cols = (panelWidth  + TILE_SIZE - 1) / TILE_SIZE;
        rows = (panelHeight + TILE_SIZE - 1) / TILE_SIZE;
        filled = new boolean[rows][cols];

        buildSpiralOrder();

        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }

    // Calcula el orden de las celdas siguiendo un anillo desde el borde
    // exterior hacia el centro (cada vuelta completa un anillo más interno).
    private void buildSpiralOrder() {
        spiralOrder = new ArrayList<>();

        int top = 0, bottom = rows - 1;
        int left = 0, right = cols - 1;

        while (top <= bottom && left <= right) {
            // Borde superior, de izquierda a derecha
            for (int c = left; c <= right; c++) spiralOrder.add(new Point(c, top));
            // Borde derecho, de arriba a abajo (sin repetir la esquina ya puesta)
            for (int r = top + 1; r <= bottom; r++) spiralOrder.add(new Point(right, r));
            // Borde inferior, de derecha a izquierda
            if (bottom > top) {
                for (int c = right - 1; c >= left; c--) spiralOrder.add(new Point(c, bottom));
            }
            // Borde izquierdo, de abajo hacia arriba
            if (right > left) {
                for (int r = bottom - 1; r > top; r--) spiralOrder.add(new Point(left, r));
            }

            top++; bottom--; left++; right--;
        }
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        for (int i = 0; i < TILES_PER_TICK && currentIndex < spiralOrder.size(); i++) {
            Point p = spiralOrder.get(currentIndex);
            filled[p.y][p.x] = true;
            currentIndex++;
        }

        repaint();

        if (currentIndex >= spiralOrder.size()) {
            timer.stop();
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (filled[row][col]) {
                    g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }
}