package gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SortingPanel extends JPanel {
    private int[] array;
    private int barWidth;
    private Set<Integer> comparingIndices = new HashSet<>();
    private Set<Integer> swappingIndices = new HashSet<>();

    public SortingPanel(int[] array) {
        this.array = array;
        this.barWidth = Math.max(1, 800 / array.length);
        setBackground(Color.WHITE);
    }

    public void setArray(int[] newArray) {
        this.array = newArray;
        this.barWidth = Math.max(1, 800 / array.length);
        repaint();
    }

    public void highlightCompare(int i, int j) {
        comparingIndices.clear();
        comparingIndices.add(i);
        comparingIndices.add(j);
    }

    public void highlightSwap(int i, int j) {
        swappingIndices.clear();
        swappingIndices.add(i);
        swappingIndices.add(j);
    }

    public void clearHighlights() {
        comparingIndices.clear();
        swappingIndices.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (array == null) return;

        for (int i = 0; i < array.length; i++) {
            if (swappingIndices.contains(i)) {
                g.setColor(Color.GREEN);
            } else if (comparingIndices.contains(i)) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.BLUE);
            }

            int barHeight = array[i];
            int x = i * barWidth;
            int y = getHeight() - barHeight;
            g.fillRect(x, y, barWidth, barHeight);
        }
    }
}
