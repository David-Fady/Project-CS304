package com;

import com.sun.opengl.util.FPSAnimator;
import javax.media.opengl.GLJPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {

    private CardLayout card;
    private JPanel container;

    private Menu menu;
    private StartMenu startMenu;
    private HowToPlay howToPlay;

    private GLJPanel GLJPanel;
    private GLCanvasProject lo;
    private FPSAnimator animator;

    public GameWindow() {
        setTitle("Brick Breaker");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        card = new CardLayout();
        container = new JPanel(card);

        menu = new Menu();
        startMenu = new StartMenu();
        howToPlay = new HowToPlay();

        container.add(menu, "menu");
        container.add(startMenu, "startMenu");
        container.add(howToPlay, "how");
        add(container, BorderLayout.CENTER);

        menu.setStartAction(e -> card.show(container, "startMenu"));
        menu.setHowToPlayAction(e -> card.show(container, "how"));
        menu.setCloseAction(e -> exitGame());

        startMenu.setOnePlayerAction(e -> chooseLevelAndStart(1));
        startMenu.setTwoPlayersAction(e -> chooseLevelAndStart(2));
        startMenu.setBackAction(e -> card.show(container, "menu"));

        for (Component c : howToPlay.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component b : p.getComponents()) {
                    if (b instanceof JButton) ((JButton) b).addActionListener(ev -> card.show(container, "menu"));
                }
            }
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { cleanupGL(); }
        });

        setVisible(true);
    }

    private void chooseLevelAndStart(int players) {
        // 1. Defining dropdown options (Levels 1 to 6)
        Integer[] levels = {1, 2, 3, 4, 5, 6 , 7 , 8 , 9 , 10};

        // 2. Creating the dropdown list (JCombo Box)
        JComboBox<Integer> levelChooser = new JComboBox<>(levels);

        // 3. Creating a custom message panel containing the dropdown
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Select Starting Level:"), BorderLayout.NORTH);
        panel.add(levelChooser, BorderLayout.CENTER);

        // 4. Displaying the modal dialog (JOptionPane)
        int option = JOptionPane.showOptionDialog(
                this,
                panel,
                "Level Selection",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"Start Game", "Cancel"}, // Dialog buttons
                "Start Game"
        );

        int chosenLevel = 1;

        // 5. Extracting the selected value if the user clicked "Start Game" (OK)
        if (option == JOptionPane.OK_OPTION) {
            // Ensure the value is not null before casting it to Integer
            Object selected = levelChooser.getSelectedItem();
            if (selected instanceof Integer) {
                chosenLevel = (Integer) selected;
            }
        } else {
            // If the user clicks Cancel, return to the startMenu
            card.show(container, "startMenu");
            return;
        }

        startGame(players, chosenLevel);

    }

    private void startGame(int players, int level) {
        cleanupGL();

        lo = new GLCanvasProject();
        lo.setPlayers(players);
        lo.setLevel(level);

        GLJPanel = new GLJPanel();
        GLJPanel.addGLEventListener(lo);
        GLJPanel.addKeyListener(lo);
        GLJPanel.addMouseListener(lo);
        GLJPanel.addMouseMotionListener(lo);

        JLayeredPane layered = new JLayeredPane();
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(layered, BorderLayout.CENTER);

        final JButton pauseButton = new JButton("Pause");
        pauseButton.setFocusable(false);
        pauseButton.setMargin(new Insets(4, 8, 4, 8));
        pauseButton.setOpaque(true);
        pauseButton.setBackground(new Color(0, 0, 0, 120));
        pauseButton.setForeground(Color.WHITE);

        GLJPanel.setBounds(0, 0, 800, 520);
        pauseButton.setBounds(10, 10, 90, 34);

        layered.add(GLJPanel, JLayeredPane.DEFAULT_LAYER);
        layered.add(pauseButton, JLayeredPane.PALETTE_LAYER);

        layered.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = layered.getSize();
                GLJPanel.setBounds(0, 0, size.width, size.height);
                int margin = 10;
                pauseButton.setBounds(margin, margin, 90, 34);
            }
        });

        pauseButton.addActionListener(e -> {
            try { if (animator != null && animator.isAnimating()) animator.stop(); } catch (Exception ignored) {}
            String[] options = {"Resume", "Back to Menu"};
            int choice = JOptionPane.showOptionDialog(GameWindow.this,
                    "Game Paused",
                    "Pause",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (choice == 0) try { if (animator != null && !animator.isAnimating()) animator.start(); } catch (Exception ignored) {}
            else if (choice == 1) { cleanupGL(); card.show(container, "menu"); }
        });

        layered.setPreferredSize(new Dimension(800, 520));
        container.add(gamePanel, "game");
        card.show(container, "game");

        animator = new FPSAnimator(GLJPanel, 60);
        animator.start();
        GLJPanel.requestFocusInWindow();
    }

    private void cleanupGL() {
        try { if (animator != null) animator.stop(); } catch (Exception ignored) {}
        animator = null;
        GLJPanel = null;
        lo = null;
    }

    private void exitGame() {
        cleanupGL();
        System.exit(0);
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(GameWindow::new); }
}
