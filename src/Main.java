// Full Enhanced Sorting Visualizer with:
// - Bubble Sort & Quick Sort
// - Merge Sort & Insertion Sort
// - Pause / Resume / Reset functionality
// - Step counter
// - Execution time tracker
// - Log panel

import gui.SortingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static SortingPanel panel;
    private static int[] array;
    private static int speed = 20;
    private static boolean isPaused = false;
    private static boolean stopRequested = false;
    private static int steps = 0;
    private static JLabel stepLabel;
    private static JLabel timeLabel;
    private static JTextArea logArea;

    public static void main(String[] args) {
        JFrame frame = new JFrame("🔍 Sorting Algorithm Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(880, 880);
        frame.setLayout(null);
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        array = generateRandomArray(100, 400);
        panel = new SortingPanel(array);
        panel.setBounds(20, 20, 820, 480);
        panel.setBackground(Color.WHITE);
        frame.add(panel);

        Font buttonFont = new Font("Segoe UI", Font.PLAIN, 14);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int width = frame.getWidth() - 60;
                int height = (int) (frame.getHeight() * 0.55);
                panel.setBounds(20, 20, width, height);
                panel.repaint();
            }
        });

        String[] btnLabels = {"Bubble Sort", "Quick Sort", "Insertion Sort", "Merge Sort"};
        JButton[] sortButtons = new JButton[btnLabels.length];
        int x = 20;
        for (int i = 0; i < btnLabels.length; i++) {
            sortButtons[i] = new JButton(btnLabels[i]);
            sortButtons[i].setBounds(x, 520, 150, 35);
            sortButtons[i].setFont(buttonFont);
            sortButtons[i].setBackground(new Color(200, 230, 250));
            frame.add(sortButtons[i]);
            x += 160;
        }

        JButton genArrayBtn = new JButton("🔄 Generate Array");
        genArrayBtn.setBounds(20, 570, 150, 35);
        genArrayBtn.setFont(buttonFont);
        genArrayBtn.setBackground(new Color(180, 255, 180));
        frame.add(genArrayBtn);

        JButton inputArrayBtn = new JButton("✏️ Manual Input");
        inputArrayBtn.setBounds(180, 570, 150, 35);
        inputArrayBtn.setFont(buttonFont);
        inputArrayBtn.setBackground(new Color(230, 230, 250));
        frame.add(inputArrayBtn);

        JButton loadBtn = new JButton("📂 Load");
        loadBtn.setBounds(340, 570, 100, 35);
        loadBtn.setFont(buttonFont);
        frame.add(loadBtn);

        JButton saveBtn = new JButton("💾 Save");
        saveBtn.setBounds(450, 570, 100, 35);
        saveBtn.setFont(buttonFont);
        frame.add(saveBtn);

        JButton pauseBtn = new JButton("⏸ Pause");
        pauseBtn.setBounds(560, 570, 100, 35);
        pauseBtn.setFont(buttonFont);
        pauseBtn.setBackground(new Color(255, 235, 180));
        frame.add(pauseBtn);

        JButton resumeBtn = new JButton("▶ Resume");
        resumeBtn.setBounds(670, 570, 100, 35);
        resumeBtn.setFont(buttonFont);
        resumeBtn.setBackground(new Color(200, 255, 200));
        frame.add(resumeBtn);

        JButton resetBtn = new JButton("🔁 Reset");
        resetBtn.setBounds(780, 570, 70, 35);
        resetBtn.setFont(buttonFont);
        resetBtn.setBackground(new Color(255, 200, 200));
        frame.add(resetBtn);

        JLabel speedLabel = new JLabel("⏩ Speed:");
        speedLabel.setBounds(20, 620, 60, 30);
        speedLabel.setFont(buttonFont);
        frame.add(speedLabel);

        JSlider speedSlider = new JSlider(1, 100, speed);
        speedSlider.setBounds(90, 620, 200, 35);
        speedSlider.setMajorTickSpacing(25);
        speedSlider.setPaintTicks(true);
        speedSlider.setBackground(new Color(245, 245, 245));
        frame.add(speedSlider);

        stepLabel = new JLabel("Steps: 0");
        stepLabel.setBounds(310, 620, 150, 30);
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        frame.add(stepLabel);

        timeLabel = new JLabel("Time: 0 ms");
        timeLabel.setBounds(470, 620, 150, 30);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        frame.add(timeLabel);

        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBounds(20, 660, 820, 160);
        frame.add(scrollPane);

        speedSlider.addChangeListener(e -> speed = speedSlider.getValue());

        sortButtons[0].addActionListener(e -> startThread(() -> bubbleSort()));
        sortButtons[1].addActionListener(e -> startThread(() -> quickSort(0, array.length - 1)));
        sortButtons[2].addActionListener(e -> startThread(() -> insertionSort()));
        sortButtons[3].addActionListener(e -> startThread(() -> mergeSort(0, array.length - 1)));

        genArrayBtn.addActionListener(e -> {
            array = generateRandomArray(100, 400);
            panel.setArray(array);
            resetStats();
        });

        inputArrayBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Enter values separated by commas or spaces (e.g. 5,10 20):");
            if (input != null && !input.trim().isEmpty()) {
                try {
                    String[] tokens = input.trim().split("[ ,]+");
                    if (tokens.length < 2 || tokens.length > 200) {
                        throw new Exception("Array length must be between 2 and 200.");
                    }
                    int[] customArray = new int[tokens.length];
                    for (int i = 0; i < tokens.length; i++) {
                        customArray[i] = Integer.parseInt(tokens[i].trim());
                        if (customArray[i] < 1 || customArray[i] > 500)
                            throw new Exception("Values must be between 1 and 500.");
                    }
                    array = customArray;
                    panel.setArray(array);
                    resetStats();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try (Scanner sc = new Scanner(chooser.getSelectedFile())) {
                    String data = sc.nextLine();
                    String[] tokens = data.trim().split("[ ,]+");
                    int[] loadedArray = new int[tokens.length];
                    for (int i = 0; i < tokens.length; i++) {
                        loadedArray[i] = Integer.parseInt(tokens[i].trim());
                    }
                    array = loadedArray;
                    panel.setArray(array);
                    resetStats();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to load array.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter out = new PrintWriter(chooser.getSelectedFile())) {
                    for (int i = 0; i < array.length; i++) {
                        out.print(array[i]);
                        if (i < array.length - 1) out.print(", ");
                    }
                    JOptionPane.showMessageDialog(frame, "Array saved successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to save array.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        pauseBtn.addActionListener(e -> isPaused = true);
        resumeBtn.addActionListener(e -> isPaused = false);
        resetBtn.addActionListener(e -> stopRequested = true);

        frame.setVisible(true);
    }

    private static int[] generateRandomArray(int size, int maxVal) {
        Random rand = new Random();
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(maxVal) + 10;
        }
        return arr;
    }

    private static void startThread(Runnable algorithm) {
        stopRequested = false;
        new Thread(() -> {
            long start = System.currentTimeMillis();
            steps = 0;
            algorithm.run();
            long end = System.currentTimeMillis();
            updateTimeLabel(end - start);
            panel.clearHighlights();
            panel.repaint();
        }).start();
    }

    private static void bubbleSort() {
        for (int i = 0; i < array.length - 1 && !stopRequested; i++) {
            for (int j = 0; j < array.length - i - 1 && !stopRequested; j++) {
                highlight(j, j + 1);
                if (array[j] > array[j + 1]) swap(j, j + 1);
                unhighlight();
            }
        }
    }

    private static void quickSort(int low, int high) {
        if (low < high && !stopRequested) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private static int partition(int low, int high) {
        int pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high && !stopRequested; j++) {
            highlight(j, high);
            if (array[j] < pivot) {
                i++;
                swap(i, j);
            }
            unhighlight();
        }
        swap(i + 1, high);
        return i + 1;
    }

    private static void insertionSort() {
        for (int i = 1; i < array.length && !stopRequested; i++) {
            int key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j] > key && !stopRequested) {
                highlight(j, j + 1);
                array[j + 1] = array[j];
                j--;
                steps++;
                panel.repaint();
                sleep();
            }
            array[j + 1] = key;
        }
    }

    private static void mergeSort(int l, int r) {
        if (l < r && !stopRequested) {
            int m = (l + r) / 2;
            mergeSort(l, m);
            mergeSort(m + 1, r);
            merge(l, m, r);
        }
    }

    private static void merge(int l, int m, int r) {
        int[] left = new int[m - l + 1];
        int[] right = new int[r - m];

        System.arraycopy(array, l, left, 0, left.length);
        System.arraycopy(array, m + 1, right, 0, right.length);

        int i = 0, j = 0, k = l;
        while (i < left.length && j < right.length && !stopRequested) {
            highlight(k);
            if (left[i] <= right[j]) {
                array[k++] = left[i++];
            } else {
                array[k++] = right[j++];
            }
            steps++;
            panel.repaint();
            sleep();
        }
        while (i < left.length && !stopRequested) {
            array[k++] = left[i++];
            steps++;
            panel.repaint();
            sleep();
        }
        while (j < right.length && !stopRequested) {
            array[k++] = right[j++];
            steps++;
            panel.repaint();
            sleep();
        }
        unhighlight();
    }

    private static void highlight(int... indices) {
        if (indices.length >= 2) panel.highlightCompare(indices[0], indices[1]);
        else if (indices.length == 1) panel.highlightCompare(indices[0], indices[0]);
        panel.repaint();
        sleep();
    }

    private static void unhighlight() {
        panel.clearHighlights();
        panel.repaint();
        sleep();
    }

    private static void swap(int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        panel.highlightSwap(i, j);
        steps++;
        log("Swapped " + i + " and " + j);
        panel.repaint();
        sleep();
    }

    private static void sleep() {
        try {
            while (isPaused) Thread.sleep(50);
            Thread.sleep(101 - speed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateStepLabel();
    }

    private static void updateStepLabel() {
        stepLabel.setText("Steps: " + steps);
    }

    private static void updateTimeLabel(long time) {
        timeLabel.setText("Time: " + time + " ms");
    }

    private static void log(String msg) {
        logArea.append(msg + "\n");
    }

    private static void resetStats() {
        steps = 0;
        isPaused = false;
        stopRequested = false;
        updateStepLabel();
        timeLabel.setText("Time: 0 ms");
        logArea.setText("");
    }
}
